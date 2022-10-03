package eu.woolplatform.web.service.controller.model;

import io.swagger.v3.oas.annotations.media.Schema;

public class DialogueMetadata {

	@Schema(description = "The name of the latest ongoing dialogue (not finished, or cancelled).",
			example = "dialogue-name")
	private String dialogueName;

	@Schema(description = "How many seconds ago was the latest engagement with this dialogue.",
			example = "60")
	private long secondsSinceLastEngagement;

	public DialogueMetadata(String dialogueName, long secondsSinceLastEngagement) {
		this.dialogueName = dialogueName;
		this.secondsSinceLastEngagement = secondsSinceLastEngagement;
	}

	public String getDialogueName() {
		return dialogueName;
	}

	public void setDialogueName(String dialogueName) {
		this.dialogueName = dialogueName;
	}

	public long getSecondsSinceLastEngagement() {
		return secondsSinceLastEngagement;
	}

	public void setSecondsSinceLastEngagement(long secondsSinceLastEngagement) {
		this.secondsSinceLastEngagement = secondsSinceLastEngagement;
	}

}
