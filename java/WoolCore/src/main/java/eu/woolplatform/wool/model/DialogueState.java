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
