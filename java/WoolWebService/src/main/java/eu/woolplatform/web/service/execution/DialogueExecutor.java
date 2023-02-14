/*
 * Copyright 2019-2022 WOOL Foundation - Licensed under the MIT License:
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package eu.woolplatform.web.service.execution;

import eu.woolplatform.web.service.storage.LoggedDialogue;
import eu.woolplatform.wool.exception.WoolException;
import eu.woolplatform.wool.execution.ActiveWoolDialogue;
import eu.woolplatform.wool.execution.ExecuteNodeResult;
import eu.woolplatform.wool.model.*;
import eu.woolplatform.wool.model.nodepointer.WoolNodePointer;
import eu.woolplatform.wool.model.nodepointer.WoolNodePointerExternal;
import eu.woolplatform.wool.model.nodepointer.WoolNodePointerInternal;
import nl.rrd.utils.AppComponents;
import nl.rrd.utils.datetime.DateTimeUtils;
import nl.rrd.utils.exception.DatabaseException;
import nl.rrd.utils.expressions.EvaluationException;
import org.slf4j.Logger;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;

/**
 * A {@link DialogueExecutor} holds a set of functions for executing WOOL Dialogue for a given
 * {@link UserService}.
 * 
 * @author Tessa Beinema
 * @author Harm op den Akker
 */
public class DialogueExecutor {

	private final Logger logger = AppComponents.getLogger(getClass().getSimpleName());
	protected UserService userService;

	// --------------------------------------------------------
	// -------------------- Constructor(s) --------------------
	// --------------------------------------------------------

	/**
	 * Creates an instance of a {@link DialogueExecutor} for a given {@code userService}.
	 * @param userService the {@link UserService} for which dialogues can be executed.
	 */
	public DialogueExecutor(UserService userService) {
		this.userService = userService;
	}

	// -------------------------------------------------------
	// -------------------- Other Methods --------------------
	// -------------------------------------------------------

	/**
	 * Starts the dialogue for the specified dialogue definition. If you specify a node ID, it will
	 * start at that node. Otherwise, it starts at the "Start" node.
	 *
	 * @param dialogueDescription the dialogue description.
	 * @param dialogueDefinition the dialogue definition.
	 * @param nodeId the node ID or {@code null}.
	 * @param sessionId the unique session identifier to be added to the logs.
	 * @param sessionStartTime the utc timestamp of when this session was started.
	 * @return the start node or specified node.
	 * @throws DatabaseException if a database error occurs.
	 * @throws IOException if a communication error occurs.
	 * @throws WoolException if the request is invalid.
	 */
	public ExecuteNodeResult startDialogue(WoolDialogueDescription dialogueDescription,
			WoolDialogue dialogueDefinition, String nodeId, String sessionId,
										   long sessionStartTime)
			throws DatabaseException, IOException, WoolException {

		ActiveWoolDialogue dialogue = new ActiveWoolDialogue(dialogueDescription,
				dialogueDefinition);
		dialogue.setWoolVariableStore(userService.getVariableStore());

		// Collects all the WOOL Variables needed to execute this file and update from an external
		// variable service (if enabled).
		Set<String> variablesNeeded = dialogueDefinition.getVariablesNeeded();
		logger.info("Dialogue '" + dialogue.getDialogueName() +
				"' uses the following set of WOOL Variables: "+variablesNeeded);
		if(!variablesNeeded.isEmpty())
			userService.updateVariablesFromExternalService(variablesNeeded);

		// The timestamp of this "start dialogue" trigger will be passed on and used for logging
		ZonedDateTime eventTime = DateTimeUtils.nowMs(userService.getWoolUser().getTimeZone());

		WoolNode startNode;
		try {
			startNode = dialogue.startDialogue(nodeId,eventTime);
		} catch (EvaluationException e) {
			throw new RuntimeException("Expression evaluation error: " + e.getMessage(), e);
		}

		LoggedDialogue loggedDialogue = new LoggedDialogue(userService.getWoolUser().getId(),
				eventTime, sessionId, sessionStartTime);
		loggedDialogue.setDialogueName(dialogueDefinition.getDialogueName());
		loggedDialogue.setLanguage(dialogueDescription.getLanguage());
		loggedDialogue = updateLoggedDialogue(startNode, loggedDialogue, -1);
		userService.getLoggedDialogueStore().saveToSession(loggedDialogue);
		return new ExecuteNodeResult(dialogueDefinition, startNode,
				loggedDialogue, loggedDialogue.getInteractionList().size() - 1);
	}
	
	/**
	 * Continues the dialogue after the user selected the specified reply. This method stores the
	 * reply as a user action in the database, and it performs any "set" actions associated with the
	 * reply. Then it determines the next node, if any.
	 * 
	 * <p>If there is no next node, this method will complete the current dialogue, and this method
	 * returns null.</p>
	 * 
	 * <p>If the reply points to another dialogue, this method will complete the current dialogue
	 * and start the other dialogue.</p>
	 * 
	 * <p>For the returned node, this method executes the agent statement and reply statements using
	 * the variable store. It executes ("if" and "set") commands and resolves variables. The
	 * returned node contains any content that should be sent to the client. This content can be
	 * text or client commands, with all variables resolved.</p>
	 *
	 * @param state a collection of objects defining the state of the currently ongoing dialogue.
	 * @param replyId the reply ID
	 * @return the next node or null
	 * @throws DatabaseException if a database error occurs
	 * @throws IOException if a communication error occurs
	 * @throws WoolException if the request is invalid
	 */
	public ExecuteNodeResult progressDialogue(DialogueState state, int replyId)
			throws DatabaseException, IOException, WoolException {

		// Define the event time that is passed along and used for logging
		ZonedDateTime progressDialogueEventTime =
				DateTimeUtils.nowMs(userService.getWoolUser().getTimeZone());

		LoggedDialogue loggedDialogue = (LoggedDialogue)state.getLoggedDialogue();
		ActiveWoolDialogue dialogue = state.getActiveDialogue();
		String userStatement = dialogue.getUserStatementFromReplyId(replyId);

		// Update the loggedDialogue with this interaction
		loggedDialogue.getInteractionList().add(new WoolLoggedInteraction(
				System.currentTimeMillis(), WoolMessageSource.USER, "USER",
				loggedDialogue.getDialogueName(), dialogue.getCurrentNode().getTitle(), state.getLoggedInteractionIndex(),
				userStatement, replyId));

		int userActionIndex = loggedDialogue.getInteractionList().size() - 1;

		// Find next WoolNode:
		WoolNodePointer nodePointer;
		try {
			nodePointer = dialogue.processReplyAndGetNodePointer(replyId,progressDialogueEventTime);
		} catch (EvaluationException ex) {
			userService.getLoggedDialogueStore().saveToSession(loggedDialogue);
			throw new RuntimeException("Expression evaluation error: " + ex.getMessage(), ex);
		}
		WoolDialogue dialogueDefinition = state.getDialogueDefinition();
		WoolNode nextWoolNode;
		if (nodePointer instanceof WoolNodePointerInternal) {
			try {
				nextWoolNode = dialogue.progressDialogue((WoolNodePointerInternal)nodePointer,
								progressDialogueEventTime);
			} catch (EvaluationException e) {
				throw new RuntimeException("Expression evaluation error: " + e.getMessage(), e);
			}
			loggedDialogue = updateLoggedDialogue(nextWoolNode, loggedDialogue, userActionIndex);
			userService.getLoggedDialogueStore().saveToSession(loggedDialogue);
			if (nextWoolNode == null)
				return null;
			return new ExecuteNodeResult(dialogueDefinition, nextWoolNode, loggedDialogue,
					loggedDialogue.getInteractionList().size() - 1);

		} else { // The dialogue continues with a pointer to another .wool script
			loggedDialogue.setCompleted(true);
			userService.getLoggedDialogueStore().saveToSession(loggedDialogue);
			String language = dialogue.getDialogueDescription().getLanguage();
			WoolNodePointerExternal externalNodePointer = (WoolNodePointerExternal)nodePointer;
			String dialogueId = externalNodePointer.getDialogueId();
			String nodeId = externalNodePointer.getNodeId();

			WoolDialogueDescription dialogueDescription =
					userService.getDialogueDescriptionFromId(dialogueId, language);
			if (dialogueDescription == null) {
				throw new WoolException(WoolException.Type.DIALOGUE_NOT_FOUND,
						"Dialogue not found: " + dialogueId);
			}
			WoolDialogue newDialogue = userService.getDialogueDefinition(dialogueDescription);

			return this.startDialogue(dialogueDescription, newDialogue, nodeId,
					loggedDialogue.getSessionId(), loggedDialogue.getSessionStartTime());
		}
	}

	public ExecuteNodeResult backDialogue(DialogueState state, ZonedDateTime eventTime)
			throws WoolException {
		LoggedDialogue loggedDialogue = (LoggedDialogue)state.getLoggedDialogue();
		List<WoolLoggedInteraction> interactions = loggedDialogue.getInteractionList();
		int prevIndex = findPreviousAgentInteractionIndex(interactions,
				state.getLoggedInteractionIndex());
		DialogueState backState = userService.getDialogueState(loggedDialogue,
				prevIndex);
		return executeCurrentNode(backState, eventTime);
	}

	private int findPreviousAgentInteractionIndex(List<WoolLoggedInteraction> interactions,
												  int start) {
		WoolLoggedInteraction interaction = interactions.get(start);
		while (interaction.getPreviousIndex() != -1) {
			int index = interaction.getPreviousIndex();
			interaction = interactions.get(index);
			if (interaction.getMessageSource() == WoolMessageSource.AGENT)
				return index;
		}
		return start;
	}

	public ExecuteNodeResult executeCurrentNode(DialogueState state, ZonedDateTime eventTime) {
		LoggedDialogue loggedDialogue = (LoggedDialogue)state.getLoggedDialogue();
		ActiveWoolDialogue dialogue = state.getActiveDialogue();
		dialogue.setWoolVariableStore(userService.getVariableStore());
		WoolNode node = dialogue.getCurrentNode();
		try {
			node = dialogue.executeWoolNode(node,eventTime);
		} catch (EvaluationException e) {
			throw new RuntimeException("Expression evaluation error: " + e.getMessage(), e);
		}
		return new ExecuteNodeResult(state.getDialogueDefinition(),
				node, loggedDialogue, state.getLoggedInteractionIndex());
	}

	/**
	 * This method is called before the current node is returned from startDialogue() or
	 * progressDialogue(). The node can be null as a result of progressDialogue() with an end reply.
	 * 
	 * <p>If the node is not null, this method adds a logged agent interaction for it.</p>
	 * 
	 * <p>If the node is null or it has no replies, the dialogue is marked as completed.</p>
	 * 
	 * @param woolNode the current node or null
	 * @param loggedDialogue the {@link LoggedDialogue} to update.
	 * @param previousIndex the previous interaction index
	 */
	private LoggedDialogue updateLoggedDialogue(WoolNode woolNode, LoggedDialogue loggedDialogue,
												int previousIndex) {
		if (woolNode != null) {
			StringBuilder agentStatement = new StringBuilder();
			for (WoolNodeBody.Segment segment : woolNode.getBody().getSegments()) {
				agentStatement.append(segment.toString());
			}
			String loggableAgentStatement = agentStatement.toString();

			loggedDialogue.getInteractionList().add(new WoolLoggedInteraction(
				System.currentTimeMillis(),
				WoolMessageSource.AGENT,
				woolNode.getHeader().getSpeaker(),
				loggedDialogue.getDialogueName(),
				woolNode.getTitle(),
				previousIndex,
				loggableAgentStatement,
				-1)
			);

		}
		if (woolNode == null || woolNode.getBody().getReplies().isEmpty()) {
			loggedDialogue.setCompleted(true);
		}

		return loggedDialogue;
	}

}
