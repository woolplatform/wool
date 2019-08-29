/*
 * Copyright 2019 Roessingh Research and Development.
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

package nl.rrd.wool.execution;

import java.util.List;
import java.util.Map;

import nl.rrd.wool.exception.WoolException;
import nl.rrd.wool.execution.WoolVariableStore.VariableSource;
import nl.rrd.wool.expressions.EvaluationException;
import nl.rrd.wool.expressions.Value;
import nl.rrd.wool.model.WoolDialogue;
import nl.rrd.wool.model.WoolNode;
import nl.rrd.wool.model.WoolNodeBody;
import nl.rrd.wool.model.WoolReply;
import nl.rrd.wool.model.command.WoolCommand;
import nl.rrd.wool.model.command.WoolInputCommand;
import nl.rrd.wool.model.command.WoolSetCommand;
import nl.rrd.wool.model.nodepointer.WoolNodePointer;
import nl.rrd.wool.model.nodepointer.WoolNodePointerInternal;

/**
 * An {@link ActiveWoolDialogue} is a wrapper around a {@link WoolDialogue}, which contains
 * a static definition of a dialogue (referred to as the {@code dialogueDefinition}). 
 * The {@link ActiveWoolDialogue} also contains utility functions to keep track of the state during 
 * "execution" of the dialogue.
 * 
 * @author Harm op den Akker
 * @author Tessa Beinema
 */
public class ActiveWoolDialogue {
	
	private WoolDialogue dialogueDefinition;
	private WoolNode currentNode;
	private DialogueState dialogueState;
	private WoolVariableStore woolVariableStore;
		
	// ----------- Constructors:
	
	public ActiveWoolDialogue(WoolDialogue dialogueDefinition) {
		this.dialogueDefinition = dialogueDefinition;
		this.dialogueState = DialogueState.INACTIVE;
	}
	
	// ---------- Getters:
	
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
	
	public void setCurrentNode(WoolNode currentNode) {
		this.currentNode = currentNode;
	}
	
	public void setDialogueState(DialogueState dialogueState) {
		this.dialogueState = dialogueState;
	}
	
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
	 * @return the initial {@link WoolNode}.
	 * @throws WoolException if the request is invalid
	 * @throws EvaluationException if an expression cannot be evaluated
	 */
	public WoolNode startDialogue() throws WoolException, EvaluationException {
		return startDialogue(null);
	}
	
	/**
	 * "Starts" this {@link ActiveWoolDialogue} at the provided {@WoolNode},
	 * returning that first node and updating the dialogue's internal state.
	 * If you set the nodeId to null, it will return the start node.
	 * 
	 * @return nodeId the node ID or null
	 * @return the first {@link WoolNode}
	 * @throws WoolException if the request is invalid
	 * @throws EvaluationException if an expression cannot be evaluated
	 */
	public WoolNode startDialogue(String nodeId) throws WoolException,
			EvaluationException {
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
		this.currentNode = nextNode;
		if(this.currentNode.getBody().getReplies().size() == 0) {
			this.dialogueState = DialogueState.FINISHED;
		}
		return executeWoolNode(nextNode);
	}
	
	/**
	 * Retrieves the pointer to the next node based on the provided reply id.
	 * This might be a pointer to the end node. This method also performs any
	 * "set" actions associated with the reply.
	 * 
	 * @param replyId the reply ID
	 * @return WoolNodePointer the pointer to the next node
	 */
	public WoolNodePointer processReplyAndGetNodePointer(int replyId)
			throws EvaluationException {
		WoolReply selectedWoolReply = currentNode.getBody().findReplyById(
				replyId);
		Map<String,Object> variableMap = woolVariableStore.getModifiableMap(
				VariableSource.CORE);
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
	 * next {@link WoolNode} which results from a call to the {@link
	 * #executeWoolNode(WoolNode)} function.</p>
	 *  
	 * @param nodePointer the next node pointer from the selected reply
	 * @return the next {@link WoolNode} that follows on the selected reply or
	 * null  
	 * @throws EvaluationException if an expression cannot be evaluated
	 */
	public WoolNode progressDialogue(WoolNodePointerInternal nodePointer)
			throws EvaluationException {
		WoolNode nextNode = null;
		if (!nodePointer.getNodeId().toLowerCase().equals("end"))
			nextNode = dialogueDefinition.getNodeById(nodePointer.getNodeId());
		this.currentNode = nextNode;
		if (nextNode == null || nextNode.getBody().getReplies().isEmpty())
			this.dialogueState = DialogueState.FINISHED;
		if (nextNode != null)
			this.currentNode = executeWoolNode(nextNode);
		return currentNode;
	}
	
	public void storeReplyInput(int replyId, Object input) {
		WoolInputCommand inputCmd = findInputCommand(replyId);
		if (inputCmd == null)
			return;
		String variableName = inputCmd.getVariableName();
		this.woolVariableStore.setValue(variableName, input, VariableSource.CORE);
	}
	
	private WoolInputCommand findInputCommand(int replyId) {
		WoolReply reply = this.currentNode.getBody().findReplyById(replyId);
		WoolNodeBody body = reply.getStatement();
		if (body == null)
			return null;
		for (WoolNodeBody.Segment segment : body.getSegments()) {
			if (!(segment instanceof WoolNodeBody.CommandSegment))
				continue;
			WoolNodeBody.CommandSegment cmdSegment =
					(WoolNodeBody.CommandSegment)segment;
			if (cmdSegment.getCommand() instanceof WoolInputCommand)
				return (WoolInputCommand)cmdSegment.getCommand();
		}
		return null;
	}

	/**
	 * The user's client returned the given {@code replyId} - what was the statement that was
	 * uttered by the user?
	 * @param replyId
	 * @return 
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
				Value value = new Value(woolVariableStore.getValue(
						command.getVariableName()));
				result.append(value.toString());
			}
		}
		return result.toString();
	}
	
	/**
	 * Executes the agent statement and reply statements in the specified node
	 * with respect to the specified variable map. It executes ("if" and "set")
	 * commands and resolves variables. Any resulting body content that should
	 * be sent to the client, is added to the (agent or reply) statement body in
	 * the resulting node. This content can be text or client commands, with all
	 * variables resolved.
	 * 
	 * @param woolNode a node to execute
	 * @return the executed WoolNode
	 * @throws EvaluationException if an expression cannot be evaluated
	 */
	private WoolNode executeWoolNode(WoolNode woolNode)
			throws EvaluationException {
		WoolNode processedNode = new WoolNode();
		processedNode.setHeader(woolNode.getHeader());
		WoolNodeBody processedBody = new WoolNodeBody();
		Map<String,Object> variables = woolVariableStore.getModifiableMap(
				VariableSource.CORE);
		woolNode.getBody().execute(variables, true, processedBody);
		processedNode.setBody(processedBody);
		return processedNode;
	}	
}
