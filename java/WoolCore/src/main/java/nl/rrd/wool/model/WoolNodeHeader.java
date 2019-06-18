package nl.rrd.wool.model;

import java.util.HashMap;
import java.util.Map;

public class WoolNodeHeader {
	
	private String title;
	private String speaker;
	private Map<String,String> optionalTags;
	
	// ---------- Constructors:
	
	public WoolNodeHeader() { 
		optionalTags = new HashMap<String,String>();
	}
	
	public WoolNodeHeader(String title) {
		this.title = title;
		optionalTags = new HashMap<String,String>();
	}
	
	public WoolNodeHeader(String title, Map<String,String> optionalTags) {
		this.title = title;
		this.optionalTags = optionalTags;
	}
	
	// ---------- Getters:
	
	public String getTitle() {
		return title;
	}
	
	public String getSpeaker() {
		return this.speaker;
	}
	
	public Map<String,String> getOptionalTags() {
		return optionalTags;
	}
	
	// ---------- Setters:
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public void setSpeaker(String speaker) {
		this.speaker = speaker;
	}
	
	public void setOptionalTags(Map<String,String> optionalTags) {
		this.optionalTags = optionalTags;
	}
	
	// ---------- Utility:
	
	public void addOptionalTag(String key, String value) {
		optionalTags.put(key,value);
	}
	
	public String toString() {
		String newline = System.getProperty("line.separator");
		StringBuilder result = new StringBuilder();
		result.append("title: " + title);
		result.append(newline + "speaker: " + speaker);
		for (String key : optionalTags.keySet()) {
			String value = optionalTags.get(key);
			result.append(newline + key + ": " + value);
		}
		return result.toString();
	}

}
