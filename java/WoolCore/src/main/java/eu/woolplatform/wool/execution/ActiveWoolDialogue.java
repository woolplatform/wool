/*
 * Copyright 2019-2020 Roessingh Research and Development.
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

package eu.woolplatform.wool.execution;

import org.joda.time.DateTime;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import eu.woolplatform.utils.expressions.EvaluationException;
import eu.woolplatform.wool.exception.WoolException;
import eu.woolplatform.wool.model.WoolDialogue;
import eu.woolplatform.wool.model.WoolDialogueDescription;
import eu.woolplatform.wool.model.WoolNode;
import eu.woolplatform.wool.model.WoolNodeBody;
import eu.woolplatform.wool.model.WoolReply;
import eu.woolplatform.wool.model.command.WoolCommand;
import eu.woolplatform.wool.model.command.WoolInputCommand;
import eu.woolplatform.wool.model.command.WoolSetCommand;
import eu.woolplatform.wool.model.nodepointer.WoolNodePointer;
import eu.woolplatform.wool.model.nodepointer.WoolNodePointerInternal;

/**
 * An {@link ActiveWoolDialogue} is a wrapper around a {@link WoolDialogue}, which contains
 * a static definition of a dialogue (referred to as the {@code dialogueDefinition}). 
 * The {@link ActiveWoolDialogue} also contains utility functions to keep track of the state during 
 * "execution" of the dialogue.
 * 
 * @author Harm op den Akker (Roessingh Research and Development)
 * @author Tessa Beinema (Roessingh Research and Development)
 */
public class ActiveWoolDialogue {

	private WoolDialogueDescription dialogueDescription;
	private WoolDialogue dialogueDefinition;
	private WoolNode currentNode;
	private DialogueState dialogueState;
	private WoolVariableStore woolVariableStore;
		
	// ----------- Constructors:

	/**
	 * Creates an instance of an {@link ActiveWoolDialogue} with a given {@link
	 * WoolDialogueDescription} and {@link WoolDialogue}.
	 *
	 * @param dialogueDescription the dialogue description
	 * @param dialogueDefinition the dialogue definition
	 */
	public ActiveWoolDialogue(WoolDialogueDescription dialogueDescription,
			WoolDialogue dialogueDefinition) {
		this.dialogueDescription = dialogueDescription;
		this.dialogueDefinition = dialogueDefinition;
		this.dialogueState = DialogueState.INACTIVE;
	}
	
	// ---------- Getters:

	public WoolDialogueDescription getDialogueDescription() {
		return dialogueDescription;
	}

	public WoolDialogue getDialogueDefinition() {
		return dialogueDefinition;
	}
	
	public WoolNode getCurrentNode() {
		return currentNode;
	}
	
	public DialogueState getDialogueState() {
		return dialogueState;
	}
	
	/**
	 * Returns the {@link WoolVariableStore} associated with this {@link ActiveWoolDialogue}.
	 * @return the {@link WoolVariableStore} associated with this {@link ActiveWoolDialogue}.
	 */
	public WoolVariableStore getWoolVariableStore() {
		return woolVariableStore;
	}
	
	// ---------- Setters:

	/**
	 * Sets the {@link WoolVariableStore} used to store/retrieve parameters for this {@link ActiveWoolDialogue}.
	 * @param woolVariableStore the {@link WoolVariableStore} used to store/retrieve parameters for this {@link ActiveWoolDialogue}.
	 */
	public void setWoolVariableStore(WoolVariableStore woolVariableStore) {
		this.woolVariableStore = woolVariableStore;
	}
	
	// ---------- Convenience:
	
	/**
	 * Returns the name of this {@link ActiveWoolDialogue} as defined in the associated {@link WoolDialogue}.
	 * @return the name of this {@link ActiveWoolDialogue} as defined in the associated {@link WoolDialogue}.
	 */
	public String getDialogueName() {
		return dialogueDefinition.getDialogueName();
	}
	
	// ---------- Functions:
	
	/**
	 * "Starts" this {@link ActiveWoolDialogue}, returning the start node and
	 * updating its internal state.
	 * 
	 * @param time the time in the time zone of the user. This will be stored
	 * with changes in the variable store.
	 * @return the initial {@link WoolNode}.
	 * @throws WoolException if the request is invalid
	 * @throws EvaluationException if an expression cannot be evaluated
	 */
	public WoolNode startDialogue(DateTime time) throws WoolException,
			EvaluationException {
		return startDialogue(null, time);
	}
	
	/**
	 * "Starts" this {@link ActiveWoolDialogue} at the provided {@link
	 * WoolNode}, returning that node and updating the dialogue's internal
	 * state. If you set the nodeId to null, it will return the start node.
	 *
	 * @param nodeId the node ID or null
	 * @param time the time in the time zone of the user. This will be stored
	 * with changes in the variable store.
	 * @return the {@link WoolNode}
	 * @throws WoolException if the request is invalid
	 * @throws EvaluationException if an expression cannot be evaluated
	 */
	public WoolNode startDialogue(String nodeId, DateTime time)
			throws WoolException, EvaluationException {
		this.dialogueState = DialogueState.ACTIVE;
		WoolNode nextNode;
		if (nodeId == null) {
			nextNode = dialogueDefinition.getStartNode();
		} else {
			nextNode = dialogueDefinition.getNodeById(nodeId);
			if (nextNode == null) {
				throw new WoolException(WoolException.Type.NODE_NOT_FOUND,
						String.format("Node \"%s\" not found in dialogue \"%s\"",
								nodeId, dialogueDefinition.getDialogueName()));
			}
		}
		this.currentNode = executeWoolNode(nextNode, time);
		if (this.currentNode.getBody().getReplies().size() == 0)
			this.dialogueState = DialogueState.FINISHED;
		return currentNode;
	}
	
	/**
	 * Retrieves the pointer to the next node based on the provided reply id.
	 * This might be a pointer to the end node. This method also performs any
	 * "set" actions associated with the reply.
	 * 
	 * @param replyId the reply ID
	 * @param time the time in the time zone of the user
	 * @return WoolNodePointer the pointer to the next node
	 * @throws EvaluationException if an expression cannot be evaluated
	 */
	public WoolNodePointer processReplyAndGetNodePointer(int replyId,
			DateTime time) throws EvaluationException {
		WoolReply selectedWoolReply = currentNode.getBody().findReplyById(
				replyId);
		Map<String,Object> variableMap = woolVariableStore.getModifiableMap(
				true, time);
		for (WoolCommand command : selectedWoolReply.getCommands()) {
			if (command instanceof WoolSetCommand) {
				WoolSetCommand setCommand = (WoolSetCommand)command;
				setCommand.getExpression().evaluate(variableMap);
			}
		}
		return selectedWoolReply.getNodePointer();
	}
	
	/**
	 * Takes the next node pointer from the selected reply and determines the
	 * next node. The pointer might point to the end note, which means that
	 * there is no next node. If there is no next node, or the next node has no
	 * reply options, then the dialogue is considered finished.
	 * 
	 * <p>If there is a next node, then it returns the executed version of that
	 * next {@link WoolNode}.</p>
	 *  
	 * @param nodePointer the next node pointer from the selected reply
	 * @param time the time in the time zone of the user. This will be stored
	 * with changes in the variable store.
	 * @return the next {@link WoolNode} that follows on the selected reply or
	 * null  
	 * @throws EvaluationException if an expression cannot be evaluated
	 */
	public WoolNode progressDialogue(WoolNodePointerInternal nodePointer,
			DateTime time) throws EvaluationException {
		WoolNode nextNode = null;
		if (!nodePointer.getNodeId().toLowerCase().equals("end"))
			nextNode = dialogueDefinition.getNodeById(nodePointer.getNodeId());
		this.currentNode = nextNode;
		if (nextNode == null || nextNode.getBody().getReplies().isEmpty())
			this.dialogueState = DialogueState.FINISHED;
		if (nextNode != null)
			this.currentNode = executeWoolNode(nextNode, time);
		return currentNode;
	}

	/**
	 * Stores the specified variables in the variable store.
	 *
	 * @param variables the variables
	 * @param time the time in the time zone of the user
	 */
	public void storeReplyInput(Map<String,?> variables, DateTime time) {
		Map<String,Object> map = woolVariableStore.getModifiableMap(true, time);
		map.putAll(variables);
	}

	/**
	 * The user's client returned the given {@code replyId} - what was the
	 * statement that was uttered by the user?
	 *
	 * @param replyId the reply ID
	 * @return the statement
	 * @throws WoolException if no reply with the specified ID is found
	 */
	public String getUserStatementFromReplyId(int replyId) throws WoolException {
		WoolReply selectedReply = currentNode.getBody().findReplyById(replyId);
		if (selectedReply == null) {
			throw new WoolException(WoolException.Type.REPLY_NOT_FOUND,
					String.format("Reply with ID %s not found in dialogue \"%s\", node \"%s\"",
					replyId, dialogueDefinition.getDialogueName(),
					currentNode.getTitle()));
		}
		if (selectedReply.getStatement() == null)
			return "AUTOFORWARD";
		StringBuilder result = new StringBuilder();
		List<WoolNodeBody.Segment> segments = selectedReply.getStatement()
				.getSegments();
		for (WoolNodeBody.Segment segment : segments) {
			if (segment instanceof WoolNodeBody.TextSegment) {
				WoolNodeBody.TextSegment textSegment =
						(WoolNodeBody.TextSegment)segment;
				result.append(textSegment.getText().evaluate(null));
			} else {
				WoolNodeBody.CommandSegment cmdSegment =
						(WoolNodeBody.CommandSegment)segment;
				// a reply statement can only contain an "input" command
				WoolInputCommand command =
						(WoolInputCommand)cmdSegment.getCommand();
				result.append(command.getStatementLog(woolVariableStore));
			}
		}
		return result.toString();
	}

	/**
	 * Executes the agent statement and reply statements in the specified node.
	 * It executes ("if", "random" and "set") commands and resolves variables.
	 * Any resulting body content that should be sent to the client, is added to
	 * the (agent or reply) statement body in the resulting node. This content
	 * can be text or client commands, with all variables resolved.
	 *
	 * @param woolNode a node to execute
	 * @param time the time in the time zone of the user. This will be stored
	 * with changes in the variable store.
	 * @return the executed WoolNode
	 * @throws EvaluationException if an expression cannot be evaluated
	 */
	private WoolNode executeWoolNode(WoolNode woolNode, DateTime time)
			throws EvaluationException {
		WoolNode processedNode = new WoolNode();
		processedNode.setHeader(woolNode.getHeader());
		WoolNodeBody processedBody = new WoolNodeBody();
		Map<String,Object> variables = woolVariableStore.getModifiableMap(true,
				time);
		woolNode.getBody().execute(variables, true, processedBody);
		processedNode.setBody(processedBody);
		return processedNode;
	}

	/**
	 * Executes the agent statement and reply statements in the specified node.
	 * It executes "if" and "random" commands and resolves variables. Any
	 * resulting body content that should be sent to the client, is added to the
	 * (agent or reply) statement body in the resulting node. This content can
	 * be text or client commands, with all variables resolved.
	 *
	 * <p>This method does not change the dialogue state and does not change
	 * any variables. Any "set" commands have no effect.</p>
	 *
	 * @param woolNode a node to execute
	 * @return the executed WoolNode
	 * @throws EvaluationException if an expression cannot be evaluated
	 */
	public WoolNode executeWoolNodeStateless(WoolNode woolNode)
			throws EvaluationException {
		WoolNode processedNode = new WoolNode();
		processedNode.setHeader(woolNode.getHeader());
		WoolNodeBody processedBody = new WoolNodeBody();
		Map<String,Object> variables = new LinkedHashMap<>(
				woolVariableStore.getModifiableMap(false, null));
		woolNode.getBody().execute(variables, true, processedBody);
		processedNode.setBody(processedBody);
		return processedNode;
	}
}
