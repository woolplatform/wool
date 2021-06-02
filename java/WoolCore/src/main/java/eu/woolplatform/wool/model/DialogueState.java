package eu.woolplatform.wool.model;

import eu.woolplatform.wool.execution.ActiveWoolDialogue;

public class DialogueState {
	private WoolDialogueDescription dialogueDescription;
	private WoolDialogue dialogueDefinition;
	private WoolLoggedDialogue loggedDialogue;
	private int loggedInteractionIndex;
	private ActiveWoolDialogue activeDialogue;

	public DialogueState(WoolDialogueDescription dialogueDescription,
			WoolDialogue dialogueDefinition, WoolLoggedDialogue loggedDialogue,
			int loggedInteractionIndex, ActiveWoolDialogue activeDialogue) {
		this.dialogueDescription = dialogueDescription;
		this.dialogueDefinition = dialogueDefinition;
		this.loggedDialogue = loggedDialogue;
		this.loggedInteractionIndex = loggedInteractionIndex;
		this.activeDialogue = activeDialogue;
	}

	public WoolDialogueDescription getDialogueDescription() {
		return dialogueDescription;
	}

	public WoolDialogue getDialogueDefinition() {
		return dialogueDefinition;
	}

	public WoolLoggedDialogue getLoggedDialogue() {
		return loggedDialogue;
	}

	public int getLoggedInteractionIndex() {
		return loggedInteractionIndex;
	}

	public ActiveWoolDialogue getActiveDialogue() {
		return activeDialogue;
	}
}
