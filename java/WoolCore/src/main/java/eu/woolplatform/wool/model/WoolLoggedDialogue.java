package eu.woolplatform.wool.model;

import java.util.List;

public interface WoolLoggedDialogue {
	String getId();
	void setId(String id);
	String getUser();
	void setUser(String user);
	String getLocalTime();
	void setLocalTime(String localTime);
	long getUtcTime();
	void setUtcTime(long utcTime);
	String getTimezone();
	void setTimezone(String timezone);
	String getDialogueName();
	void setDialogueName(String dialogueName);
	String getLanguage();
	void setLanguage(String language);
	boolean isCompleted();
	void setCompleted(boolean completed);
	boolean isCancelled();
	void setCancelled(boolean cancelled);
	List<WoolLoggedInteraction> getInteractionList();
	void setInteractionList(List<WoolLoggedInteraction> interactionList);
}
