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

package eu.woolplatform.wool.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonIgnoreProperties(ignoreUnknown=true)
public class WoolLoggedInteraction {

	private long timestamp;
	private WoolMessageSource messageSource;
	private String sourceName;
	private String dialogueId;
	private String nodeId;
	private String statement;
	private int previousIndex = -1;

	@JsonInclude(Include.NON_NULL)
	private int replyId;


	// ---------- Constructors:

	public WoolLoggedInteraction() {
	}

	public WoolLoggedInteraction(long timestamp,
			WoolMessageSource messageSource, String sourceName,
			String dialogueId, String nodeId, int previousIndex,
			String statement) {
		this.timestamp = timestamp;
		this.messageSource = messageSource;
		this.sourceName = sourceName;
		this.dialogueId = dialogueId;
		this.nodeId = nodeId;
		this.previousIndex = previousIndex;
		this.statement = statement;
	}

	public WoolLoggedInteraction(long timestamp,
			WoolMessageSource messageSource, String sourceName,
			String dialogueId, String nodeId, int previousIndex,
			String statement, int replyId) {
		this.timestamp = timestamp;
		this.messageSource = messageSource;
		this.sourceName = sourceName;
		this.dialogueId = dialogueId;
		this.nodeId = nodeId;
		this.previousIndex = previousIndex;
		this.statement = statement;
		this.replyId = replyId;
	}
	
	// ---------- Getters:
	
	public long getTimestamp() {
		return timestamp;
	}
	
	public WoolMessageSource getMessageSource() {
		return messageSource;
	}
	
	public String getSourceName() {
		return sourceName;
	}

	public String getDialogueId() {
		return this.dialogueId;
	}
	
	public String getNodeId() {
		return this.nodeId;
	}

	public int getPreviousIndex() {
		return previousIndex;
	}

	public String getStatement() {
		return statement;
	}
	
	public int getReplyId() {
		return this.replyId;
	}
	
	// ---------- Setters:
	
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	
	public void setMessageSource(WoolMessageSource messageSource) {
		this.messageSource = messageSource;
	}
	
	public void setSourceName(String sourceName) {
		this.sourceName = sourceName;
	}

	public void setDialogueId(String dialogueId) {
		this.dialogueId = dialogueId;
	}
	
	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

	public void setPreviousIndex(int previousIndex) {
		this.previousIndex = previousIndex;
	}

	public void setStatement(String statement) {
		this.statement = statement;
	}
	
	public void setReplyId(int replyId) {
		this.replyId = replyId;
	}
	
	
}
