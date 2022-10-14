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

package eu.woolplatform.wool.model.protocol;

import eu.woolplatform.wool.model.WoolNode;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is used for dialogue messages that are sent to the client in the
 * web service protocol. It can be generated from an executed {@link WoolNode
 * WoolNode} using the {@link DialogueMessageFactory DialogueMessageFactory}.
 * The {@link WoolNode WoolNode} having been executed means that variables have
 * been resolved and "if" and "set" commands have been executed.
 *
 * @author Dennis Hofs (RRD)
 */
public class DialogueMessage {
	private String dialogue;
	private String node;
	private String loggedDialogueId;
	private int loggedInteractionIndex;
	private String speaker;
	private DialogueStatement statement;
	private List<ReplyMessage> replies = new ArrayList<>();

	public String getDialogue() {
		return dialogue;
	}

	public void setDialogue(String dialogue) {
		this.dialogue = dialogue;
	}

	public String getNode() {
		return node;
	}

	public void setNode(String node) {
		this.node = node;
	}

	public String getLoggedDialogueId() {
		return loggedDialogueId;
	}

	public void setLoggedDialogueId(String loggedDialogueId) {
		this.loggedDialogueId = loggedDialogueId;
	}

	public int getLoggedInteractionIndex() {
		return loggedInteractionIndex;
	}

	public void setLoggedInteractionIndex(int loggedInteractionIndex) {
		this.loggedInteractionIndex = loggedInteractionIndex;
	}

	public String getSpeaker() {
		return speaker;
	}

	public void setSpeaker(String speaker) {
		this.speaker = speaker;
	}

	public DialogueStatement getStatement() {
		return statement;
	}

	public void setStatement(DialogueStatement statement) {
		this.statement = statement;
	}

	public List<ReplyMessage> getReplies() {
		return replies;
	}

	public void setReplies(List<ReplyMessage> replies) {
		this.replies = replies;
	}
	
	public void addReply(ReplyMessage reply) {
		replies.add(reply);
	}
}
