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

import eu.woolplatform.wool.model.WoolNodeBody;
import eu.woolplatform.wool.model.WoolReply;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is used for dialogue replies that are sent to the client in the
 * web service protocol. It mirrors the class {@link WoolReply WoolReply}. The
 * differences are:
 *
 * <p><ul>
 * <li>The statement (if available) has been converted from a {@link
 * WoolNodeBody WoolNodeBody} to a {@link DialogueStatement DialogueStatement}
 * where variables have been resolved and set commands have been executed. The
 * statement only contains text segments and input segments.</li>
 *
 * <li>The commands in a {@link WoolReply WoolReply} can be action commands or
 * set commands. Since only action commands are relevant to the client, this
 * class does not contain set commands. They will be handled on the server when
 * the user selects the reply. Action commands have been converted to {@link
 * DialogueAction DialogueAction}s where variables have been resolved.</li>
 *
 * <li>This class does not have a node pointer, because that is not relevant for
 * the client. It is handled by the server when the user selects this reply.
 * Instead of that, this class indicates whether this reply ends the
 * dialogue. This is set if the reply has a pointer to the end node.</li>
 * </ul></p>
 *
 * @author Dennis Hofs (RRD)
 */
public class ReplyMessage {
	private int replyId;
	private DialogueStatement statement = null;
	private List<DialogueAction> actions = new ArrayList<>();
	private boolean endsDialogue = false;

	/**
	 * Returns the reply ID.
	 * 
	 * @return the reply ID
	 */
	public int getReplyId() {
		return replyId;
	}

	/**
	 * Sets the reply ID.
	 * 
	 * @param replyId the reply ID
	 */
	public void setReplyId(int replyId) {
		this.replyId = replyId;
	}

	/**
	 * Returns the reply statement. This is null if the reply is an autoforward
	 * reply.
	 * 
	 * @return the reply statement or null (default)
	 */
	public DialogueStatement getStatement() {
		return statement;
	}

	/**
	 * Sets the reply statement. This is null if the reply is an autoforward
	 * reply.
	 * 
	 * @param statement the reply statement or null (default)
	 */
	public void setStatement(DialogueStatement statement) {
		this.statement = statement;
	}

	/**
	 * Returns the actions that should be performed when this reply is chosen.
	 * 
	 * @return the actions that should be performed when this reply is chosen
	 */
	public List<DialogueAction> getActions() {
		return actions;
	}

	/**
	 * Sets the actions that should be performed when this reply is chosen.
	 * 
	 * @param actions the actions that should be performed when this reply is
	 * chosen
	 */
	public void setActions(List<DialogueAction> actions) {
		this.actions = actions;
	}
	
	/**
	 * Adds an action that should be performed when this reply is chosen.
	 * 
	 * @param action the action that should be performed when this reply is
	 * chosen
	 */
	public void addAction(DialogueAction action) {
		actions.add(action);
	}

	/**
	 * Returns whether this reply ends the dialogue.
	 * 
	 * @return true if this reply ends the dialogue, false otherwise (default)
	 */
	public boolean isEndsDialogue() {
		return endsDialogue;
	}

	/**
	 * Sets whether this reply ends the dialogue.
	 * 
	 * @param endsDialogue true if this reply ends the dialogue, false otherwise
	 * (default)
	 */
	public void setEndsDialogue(boolean endsDialogue) {
		this.endsDialogue = endsDialogue;
	}
}
