package eu.woolplatform.wool.execution;

import eu.woolplatform.wool.model.WoolDialogue;
import eu.woolplatform.wool.model.WoolLoggedDialogue;
import eu.woolplatform.wool.model.WoolNode;

public class ExecuteNodeResult {
	private WoolDialogue dialogue;
	private WoolNode woolNode;
	private WoolLoggedDialogue loggedDialogue;
	private int interactionIndex;

	public ExecuteNodeResult(WoolDialogue dialogue, WoolNode woolNode,
			WoolLoggedDialogue loggedDialogue, int interactionIndex) {
		this.dialogue = dialogue;
		this.woolNode = woolNode;
		this.loggedDialogue = loggedDialogue;
		this.interactionIndex = interactionIndex;
	}

	public WoolDialogue getDialogue() {
		return dialogue;
	}

	public WoolNode getWoolNode() {
		return woolNode;
	}

	public WoolLoggedDialogue getLoggedDialogue() {
		return loggedDialogue;
	}

	public int getInteractionIndex() {
		return interactionIndex;
	}
}
