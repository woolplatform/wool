package eu.woolplatform.wool.execution;

import eu.woolplatform.wool.model.WoolDialogue;
import eu.woolplatform.wool.model.WoolNode;

public class ExecuteNodeResult {
	private WoolDialogue dialogue;
	private WoolNode woolNode;
	private String loggedDialogueId;
	private int interactionIndex;

	public ExecuteNodeResult(WoolDialogue dialogue, WoolNode woolNode,
			String loggedDialogueId, int interactionIndex) {
		this.dialogue = dialogue;
		this.woolNode = woolNode;
		this.loggedDialogueId = loggedDialogueId;
		this.interactionIndex = interactionIndex;
	}

	public WoolDialogue getDialogue() {
		return dialogue;
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
