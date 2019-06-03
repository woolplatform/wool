package nl.rrd.wool.model;

public class WoolDialogueDescription {
	
	private String mainSpeaker;
	private String language;
	private String fileName;
	
	// -------------------- Constructors

	public WoolDialogueDescription () {	}

	public WoolDialogueDescription (String mainSpeaker, String language, String fileName) {
		this.setMainSpeaker(mainSpeaker);
		this.setLanguage(language);
		this.setFileName(fileName);
	}
	
	// -------------------- Getters

	public String getMainSpeaker() {
		return this.mainSpeaker;
	}
	
	public String getLanguage() {
		return this.language;
	}
	
	public String getFileName() {
		return this.fileName;
	}
	
	// -------------------- Setters
	
	public void setMainSpeaker(String mainSpeaker) {
		this.mainSpeaker = mainSpeaker;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}	

	public String toString() {
		return "Dialogue '" + this.fileName + ".wool.txt' with main speaker '" + this.mainSpeaker + "' in language '" + this.language + "'.";
	}
}
