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

package eu.woolplatform.web.logservice.controller.schema;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 *
 * @author Harm op den Akker
 */
public class LogEventPayload {

	@Schema(description = "Identifier of the WOOL User",
			example = "userId",
			required = true)
	private String userId;

	@Schema(description = "The (optional) session identifier",
			example = "4efd81c5-36a4-4071-9d84-105c13f5fd6d")
	private String sessionId;

	@Schema(description = "UTC Timestamp in milliseconds of the event",
			example = "1674571573285",
			required = true)
	private Long timeStamp;

	@Schema(description = "The time zone in which the event was generated",
			example = "Europe/Lisbon",
			required = true)
	private String timeZone;

	@Schema(description = "The name (id) of the dialogue associated with the event",
			example = "dialogue-name",
			required = true)
	private String dialogueId;

	@Schema(description = "The language in which the dialogue was executed",
			example = "en",
			required = true)
	private String language;

	@Schema(description = "The name (id) of the node of the dialogue that sparked the event",
			example = "Start",
			required = true)
	private String nodeId;

	@Schema(description = "Either 'AGENT' or 'USER', indicating the source of the event",
			example = "AGENT",
			required = true)
	private String messageSource;

	@Schema(description = "The given name for the message source (Speaker name)",
			example = "Bob",
			required = true)
	private String sourceName;

	@Schema(description = "The statement that was made in this dialogue step",
			example = "Hi, how are you doing today?",
			required = true)
	private String statement;

	// --------------------------------------------------------
	// -------------------- Constructor(s) --------------------
	// --------------------------------------------------------

	/**
	 * Creates an instance of an empty {@link LogEventPayload}.
	 */
	public LogEventPayload() { }

	public LogEventPayload(String userId, String sessionId, Long timeStamp, String timeZone,
						   String dialogueId, String language, String nodeId, String messageSource,
						   String sourceName, String statement) {
		this.userId = userId;
		this.sessionId = sessionId;
		this.timeStamp = timeStamp;
		this.timeZone = timeZone;
		this.dialogueId = dialogueId;
		this.language = language;
		this.nodeId = nodeId;
		this.messageSource = messageSource;
		this.sourceName = sourceName;
		this.statement = statement;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public Long getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(Long timeStamp) {
		this.timeStamp = timeStamp;
	}

	public String getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}

	public String getDialogueId() {
		return dialogueId;
	}

	public void setDialogueId(String dialogueId) {
		this.dialogueId = dialogueId;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getNodeId() {
		return nodeId;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

	public String getMessageSource() {
		return messageSource;
	}

	public void setMessageSource(String messageSource) {
		this.messageSource = messageSource;
	}

	public String getSourceName() {
		return sourceName;
	}

	public void setSourceName(String sourceName) {
		this.sourceName = sourceName;
	}

	public String getStatement() {
		return statement;
	}

	public void setStatement(String statement) {
		this.statement = statement;
	}

	@Override
	public String toString() {
		return "LogEventPayload{" +
				"userId='" + userId + '\'' +
				", sessionId='" + sessionId + '\'' +
				", timeStamp=" + timeStamp +
				", timeZone='" + timeZone + '\'' +
				", dialogueId='" + dialogueId + '\'' +
				", language='" + language + '\'' +
				", nodeId='" + nodeId + '\'' +
				", messageSource='" + messageSource + '\'' +
				", sourceName='" + sourceName + '\'' +
				", statement='" + statement + '\'' +
				'}';
	}
}
