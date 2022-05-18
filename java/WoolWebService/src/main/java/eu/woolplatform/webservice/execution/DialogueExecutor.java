/*
 * Copyright 2019-2022 WOOL Foundation.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */
package eu.woolplatform.webservice.execution;

import eu.woolplatform.utils.exception.DatabaseException;
import eu.woolplatform.utils.expressions.EvaluationException;
import eu.woolplatform.webservice.model.LoggedDialogue;
import eu.woolplatform.webservice.model.LoggedDialogueStoreIO;
import eu.woolplatform.wool.exception.WoolException;
import eu.woolplatform.wool.execution.ActiveWoolDialogue;
import eu.woolplatform.wool.execution.ExecuteNodeResult;
import eu.woolplatform.wool.model.*;
import eu.woolplatform.wool.model.nodepointer.WoolNodePointer;
import eu.woolplatform.wool.model.nodepointer.WoolNodePointerExternal;
import eu.woolplatform.wool.model.nodepointer.WoolNodePointerInternal;
import org.joda.time.DateTime;

import java.io.IOException;
import java.util.List;

/**
 * A {@link DialogueExecutor} holds a set of functions for executing WOOL
 * Dialogue for a given {@link UserService}.
 * 
 * @author Tessa Beinema
 * @author Harm op den Akker
 */
public class DialogueExecutor {
	
	protected UserService userService;

	public DialogueExecutor(UserService userService) {
		this.userService = userService;
	}

	/**
	 * Starts the dialogue for the specified dialogue definition. It starts at
	 * the start node.
	 *
	 * @param dialogueDescription the dialogue description
	 * @param dialogueDefinition the dialogue definition
	 * @param time the time in the time zone of the user
	 * @return the start node
	 * @throws DatabaseException if a database error occurs
	 * @throws IOException if a communication error occurs
	 * @throws WoolException if the request is invalid
	 */
	public ExecuteNodeResult startDialogue(
			WoolDialogueDescription dialogueDescription,
			WoolDialogue dialogueDefinition, DateTime time)
			throws DatabaseException, IOException, WoolException {
		return startDialogue(dialogueDescription, dialogueDefinition, null,
				time);
	}

	/**
	 * Starts the dialogue for the specified dialogue definition. If you specify
	 * a node ID, it will start at that node. Otherwise, it starts at the start
	 * node.
	 *
	 * @param dialogueDescription the dialogue description
	 * @param dialogueDefinition the dialogue definition
	 * @param nodeId the node ID or null
	 * @param time the time in the time zone of the user
	 * @return the start node or specified node
	 * @throws DatabaseException if a database error occurs
	 * @throws IOException if a communication error occurs
	 * @throws WoolException if the request is invalid
	 */
	public ExecuteNodeResult startDialogue(
			WoolDialogueDescription dialogueDescription,
			WoolDialogue dialogueDefinition, String nodeId, DateTime time)
			throws DatabaseException, IOException, WoolException {
		ActiveWoolDialogue dialogue = new ActiveWoolDialogue(
				dialogueDescription, dialogueDefinition);
		dialogue.setWoolVariableStore(userService.getVariableStore());
		WoolNode startNode;
		try {
			startNode = dialogue.startDialogue(nodeId, time);
		} catch (EvaluationException e) {
			throw new RuntimeException("Expression evaluation error: " +
					e.getMessage(), e);
		}
		LoggedDialogue loggedDialogue = new LoggedDialogue(
				userService.getUserId(), time);
		loggedDialogue.setDialogueName(dialogueDefinition.getDialogueName());
		loggedDialogue.setLanguage(dialogueDescription.getLanguage());
		LoggedDialogueStoreIO.createLoggedDialogue(loggedDialogue);
		executeNextNode(startNode, loggedDialogue, -1);
		return new ExecuteNodeResult(dialogueDefinition, startNode,
				loggedDialogue, loggedDialogue.getInteractionList().size() - 1);
	}
	
	/**
	 * Continues the dialogue after the user selected the specified reply. This
	 * method stores the reply as a user action in the database, and it performs
	 * any "set" actions associated with the reply. Then it determines the next
	 * node, if any.
	 * 
	 * <p>If there is no next node, this method will complete the current
	 * dialogue, and this method returns null.</p>
	 * 
	 * <p>If the reply points to another dialogue, this method will complete the
	 * current dialogue and start the other dialogue.</p>
	 * 
	 * <p>For the returned node, this method executes the agent statement and
	 * reply statements using the variable store. It executes ("if" and "set")
	 * commands and resolves variables. The returned node contains any content
	 * that should be sent to the client. This content can be text or client
	 * commands, with all variables resolved.</p>
	 *
	 * @param state the state from which the dialogue should progress
	 * @param replyId the reply ID
	 * @param time the time in the time zone of the user
	 * @return the next node or null
	 * @throws DatabaseException if a database error occurs
	 * @throws IOException if a communication error occurs
	 * @throws WoolException if the request is invalid
	 */
	public ExecuteNodeResult progressDialogue(DialogueState state, int replyId,
			DateTime time) throws DatabaseException, IOException,
			WoolException {
		LoggedDialogue loggedDialogue =
				(LoggedDialogue)state.getLoggedDialogue();
		ActiveWoolDialogue dialogue = state.getActiveDialogue();
		String userStatement = dialogue.getUserStatementFromReplyId(replyId);
		LoggedDialogueStoreIO.addLoggedUserInteraction(System.currentTimeMillis(),
				"USER", loggedDialogue, dialogue.getCurrentNode().getTitle(),
				state.getLoggedInteractionIndex(), userStatement, replyId);
		int userActionIndex = loggedDialogue.getInteractionList().size() - 1;
		// Find next WoolNode:
		WoolNodePointer nodePointer;
		try {
			nodePointer = dialogue.processReplyAndGetNodePointer(replyId, time);
		} catch (EvaluationException ex) {
			throw new RuntimeException("Expression evaluation error: " +
					ex.getMessage(), ex);
		}
		WoolDialogue dialogueDefinition = state.getDialogueDefinition();
		WoolNode nextWoolNode;
		if (nodePointer instanceof WoolNodePointerInternal) {
			try {
				nextWoolNode = dialogue.progressDialogue(
						(WoolNodePointerInternal)nodePointer, time);
			} catch (EvaluationException e) {
				throw new RuntimeException("Expression evaluation error: " +
						e.getMessage(), e);
			}
			executeNextNode(nextWoolNode, loggedDialogue, userActionIndex);
			if (nextWoolNode == null)
				return null;
			return new ExecuteNodeResult(dialogueDefinition, nextWoolNode,
					loggedDialogue,
					loggedDialogue.getInteractionList().size() - 1);
		}
		else {
			LoggedDialogueStoreIO.setDialogueCompleted(loggedDialogue);
			String language = dialogue.getDialogueDescription().getLanguage();
			WoolNodePointerExternal externalNodePointer =
					(WoolNodePointerExternal)nodePointer;
			String dialogueId = externalNodePointer.getDialogueId();
			String nodeId = externalNodePointer.getNodeId();
			return userService.startDialogue(dialogueId, nodeId, language,
					time);
		}
	}

	public ExecuteNodeResult backDialogue(DialogueState state, DateTime time)
			throws WoolException {
		LoggedDialogue loggedDialogue =
				(LoggedDialogue)state.getLoggedDialogue();
		List<WoolLoggedInteraction> interactions =
				loggedDialogue.getInteractionList();
		int prevIndex = findPreviousAgentInteractionIndex(interactions,
				state.getLoggedInteractionIndex());
		DialogueState backState = userService.getDialogueState(loggedDialogue,
				prevIndex);
		return executeCurrentNode(backState, time);
	}

	private int findPreviousAgentInteractionIndex(
			List<WoolLoggedInteraction> interactions, int start) {
		WoolLoggedInteraction interaction = interactions.get(start);
		while (interaction.getPreviousIndex() != -1) {
			int index = interaction.getPreviousIndex();
			interaction = interactions.get(index);
			if (interaction.getMessageSource() == WoolMessageSource.AGENT)
				return index;
		}
		return start;
	}

	public ExecuteNodeResult executeCurrentNode(DialogueState state,
			DateTime time) {
		LoggedDialogue loggedDialogue =
				(LoggedDialogue)state.getLoggedDialogue();
		ActiveWoolDialogue dialogue = state.getActiveDialogue();
		dialogue.setWoolVariableStore(userService.getVariableStore());
		WoolNode node = dialogue.getCurrentNode();
		try {
			node = dialogue.executeWoolNode(node, time);
		} catch (EvaluationException e) {
			throw new RuntimeException("Expression evaluation error: " +
					e.getMessage(), e);
		}
		return new ExecuteNodeResult(state.getDialogueDefinition(),
				node, loggedDialogue, state.getLoggedInteractionIndex());
	}

	/**
	 * This method is called before the current node is returned from
	 * startDialogue() or progressDialogue(). The node can be null as a result
	 * of progressDialogue() with an end reply.
	 * 
	 * <p>If the node is not null, this method adds a logged agent interaction
	 * for it.</p>
	 * 
	 * <p>If the node is null or it has no replies, the dialogue is
	 * completed.</p>
	 * 
	 * @param node the current node or null
	 * @throws DatabaseException if a database error occurs
	 * @throws IOException if a communication error occurs
	 */
	private void executeNextNode(WoolNode node, LoggedDialogue loggedDialogue,
			int previousIndex)
			throws DatabaseException, IOException {
		if (node != null) {
			LoggedDialogueStoreIO.addLoggedAgentInteraction(
					System.currentTimeMillis(),
					node.getHeader().getSpeaker(),
					loggedDialogue,
					node.getTitle(),
					previousIndex,
					createLoggableStatementFromNode(node));
		}
		if (node == null || node.getBody().getReplies().isEmpty()) {
			LoggedDialogueStoreIO.setDialogueCompleted(loggedDialogue);
		}
	}

	private String createLoggableStatementFromNode(WoolNode woolNode) {
		StringBuilder agentStatement = new StringBuilder();
		for (WoolNodeBody.Segment segment : woolNode.getBody().getSegments()) {
			agentStatement.append(segment.toString());
		}
		return agentStatement.toString();
	}
}
