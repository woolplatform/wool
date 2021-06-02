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

package eu.woolplatform.wool.model.protocol;

import eu.woolplatform.wool.execution.ExecuteNodeResult;
import eu.woolplatform.wool.model.WoolNode;
import eu.woolplatform.wool.model.WoolNodeBody;
import eu.woolplatform.wool.model.WoolReply;
import eu.woolplatform.wool.model.command.WoolActionCommand;
import eu.woolplatform.wool.model.command.WoolCommand;
import eu.woolplatform.wool.model.command.WoolInputCommand;
import eu.woolplatform.wool.model.nodepointer.WoolNodePointerInternal;

public class DialogueMessageFactory {
	
	/**
	 * Generates a DialogueMessage based on the given executed node. Since the
	 * node has already been executed, it should not contain variables or "if"
	 * and "set" commands.
	 * 
	 * @param executedNode the executed node
	 * @return the DialogueMessage
	 */
	public static DialogueMessage generateDialogueMessage(
			ExecuteNodeResult executedNode) {
		DialogueMessage dialogueMessage = new DialogueMessage();
		WoolNode node = executedNode.getWoolNode();
		WoolNodeBody body = node.getBody();
		dialogueMessage.setDialogue(executedNode.getDialogue()
				.getDialogueName());
		dialogueMessage.setNode(node.getTitle());
		dialogueMessage.setLoggedDialogueId(executedNode.getLoggedDialogue()
				.getId());
		dialogueMessage.setLoggedInteractionIndex(
				executedNode.getInteractionIndex());
		dialogueMessage.setSpeaker(node.getHeader().getSpeaker());
		dialogueMessage.setStatement(generateDialogueStatement(body));
		for (WoolReply reply : body.getReplies()) {
			dialogueMessage.addReply(generateDialogueReply(reply));
		}
		return dialogueMessage;
	}
	
	private static DialogueStatement generateDialogueStatement(
			WoolNodeBody body) {
		DialogueStatement statement = new DialogueStatement();
		for (WoolNodeBody.Segment segment : body.getSegments()) {
			if (segment instanceof WoolNodeBody.TextSegment) {
				WoolNodeBody.TextSegment textSegment =
						(WoolNodeBody.TextSegment)segment;
				String text = textSegment.getText().evaluate(null);
				statement.addTextSegment(text);
			} else {
				WoolNodeBody.CommandSegment cmdSegment =
						(WoolNodeBody.CommandSegment)segment;
				WoolCommand cmd = cmdSegment.getCommand();
				if (cmd instanceof WoolActionCommand) {
					statement.addActionSegment((WoolActionCommand)cmd);
				} else if (cmd instanceof WoolInputCommand) {
					statement.addInputSegment((WoolInputCommand)cmd);
				}
			}
		}
		return statement;
	}
	
	private static ReplyMessage generateDialogueReply(WoolReply reply) {
		ReplyMessage replyMsg = new ReplyMessage();
		replyMsg.setReplyId(reply.getReplyId());
		if (reply.getStatement() != null) {
			replyMsg.setStatement(generateDialogueStatement(
					reply.getStatement()));
		}
		if (reply.getNodePointer() instanceof WoolNodePointerInternal) {
			WoolNodePointerInternal pointer =
					(WoolNodePointerInternal)reply.getNodePointer();
			if (pointer.getNodeId().equalsIgnoreCase("end"))
				replyMsg.setEndsDialogue(true);
		}
		for (WoolCommand cmd : reply.getCommands()) {
			if (!(cmd instanceof WoolActionCommand))
				continue;
			WoolActionCommand actionCmd = (WoolActionCommand)cmd;
			replyMsg.addAction(new DialogueAction(actionCmd));
		}
		return replyMsg;
	}
}
