package nl.rrd.wool.execution;

import java.util.ArrayList;
import java.util.List;

import nl.rrd.wool.model.WoolDialogue;

public class WoolDefinitionAnalysisReport {
	
	private WoolDialogue woolDialogue;
	private List<String> warnings;
	private List<String> speakers;
	private long startTime;
	private long stopTime;
	
	public WoolDefinitionAnalysisReport(WoolDialogue woolDialogue) {
		this.woolDialogue = woolDialogue;
		warnings = new ArrayList<String>();
		speakers = new ArrayList<String>();
		this.startTime = System.currentTimeMillis();
		this.stopTime = 0l;
	}
	
	// ---------- Getters:
	
	public String getSummaryString() {
		String summary = "";
		summary += "----- ANALYSIS REPORT -----\n";
		summary += "Analysis of Wool Dialogue '"+woolDialogue.getDialogueName()+"':\n";
		if(stopTime == 0l) {
			summary += "Analysis incomplete.";
			return summary;
		} else {
			summary += "  Completed in "+getDurationInMilliseconds()+" ms.\n";
			summary += "  Generated "+warnings.size()+" warnings.\n";
			summary += "  Reported "+speakers.size()+" different speakers in dialogue.\n";
			summary += "---------------------------";
		}
			
		return summary;
	}
	
	public List<String> getWarnings() {
		return warnings;
	}
	
	public List<String> getSpeakers() {
		return speakers;
	}
	
	public long getStartTime() {
		return startTime;
	}
	
	public long getStopTime() {
		return stopTime;
	}
	
	public long getDurationInMilliseconds() {
		System.out.println("StartTime: "+startTime);
		System.out.println("StopTime: "+stopTime);
		if(stopTime == 0l) {
			return 0l;
		} else {
			return stopTime - startTime;
		}	
	}
		
	// --------- Functions:
	
	public void reportStopTime() {
		this.stopTime = System.currentTimeMillis();
	}
	
	public void addWarning(String warning) {
		warnings.add(warning);
	}
	
	/**
	 * Adds the given {@code speaker} to the set of recorded speakers in this
	 * {@link WoolDefinitionAnalysisReport} if it hasn't been seen before.
	 * @param speaker the speaker name to add.
	 */
	public void addSpeakerIfUnique(String speaker) {
		if(!speakers.contains(speaker)) {
			speakers.add(speaker);
		}
	}
	
}
