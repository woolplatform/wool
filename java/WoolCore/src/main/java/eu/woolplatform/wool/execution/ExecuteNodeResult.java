package eu.woolplatform.wool.execution;

import eu.woolplatform.wool.model.WoolNode;

public class ExecuteNodeResult {
	private String dialogueId;
	private WoolNode woolNode;
	private String loggedDialogueId;
	private int interactionIndex;

	public ExecuteNodeResult(String dialogueId, WoolNode woolNode,
			String loggedDialogueId, int interactionIndex) {
		this.dialogueId = dialogueId;
		this.woolNode = woolNode;
		this.loggedDialogueId = loggedDialogueId;
		this.interactionIndex = interactionIndex;
	}

	public String getDialogueId() {
		return dialogueId;
	}

	public WoolNode getWoolNode() {
		return woolNode;
	}

	public String getLoggedDialogueId() {
		return loggedDialogueId;
	}

	public int getInteractionIndex() {
		return interactionIndex;
	}
}
