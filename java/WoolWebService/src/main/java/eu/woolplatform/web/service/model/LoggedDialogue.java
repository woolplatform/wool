/*
 * Copyright 2019-2022 WOOL Foundation.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */
package eu.woolplatform.web.service.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import eu.woolplatform.wool.model.WoolLoggedDialogue;
import eu.woolplatform.wool.model.WoolLoggedInteraction;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown=true)
public class LoggedDialogue implements WoolLoggedDialogue {
	public static final String LOCAL_TIME_FORMAT =
			"yyyy-MM-dd'T'HH:mm:ss.SSS";

	private String id;
	private String user;
	private String localTime;
	private long utcTime;
	private String timezone;
	private String dialogueName;
	private String language;
	private boolean completed;
	private boolean cancelled;
	private List<WoolLoggedInteraction> interactionList = new ArrayList<>();

	public LoggedDialogue() {
	}

	/**
	 * Constructs a new instance at the specified time. It should define the
	 * local time and location-based time zone (not an offset).
	 *
	 * @param user the user ID
	 * @param tzTime the time
	 */
	public LoggedDialogue(String user, DateTime tzTime) {
		this.user = user;
		this.utcTime = tzTime.getMillis();
		this.timezone = tzTime.getZone().getID();
		this.localTime = tzTime.toString(LOCAL_TIME_FORMAT);
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public void setId(String id) {
		this.id = id;
	}

	@Override
	public String getUser() {
		return user;
	}

	@Override
	public void setUser(String user) {
		this.user = user;
	}

	@Override
	public String getLocalTime() {
		return localTime;
	}

	@Override
	public void setLocalTime(String localTime) {
		this.localTime = localTime;
	}

	@Override
	public long getUtcTime() {
		return utcTime;
	}

	@Override
	public void setUtcTime(long utcTime) {
		this.utcTime = utcTime;
	}

	@Override
	public String getTimezone() {
		return timezone;
	}

	@Override
	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}

	@Override
	public String getDialogueName() {
		return dialogueName;
	}

	@Override
	public void setDialogueName(String dialogueName) {
		this.dialogueName = dialogueName;
	}

	@Override
	public String getLanguage() {
		return language;
	}

	@Override
	public void setLanguage(String language) {
		this.language = language;
	}

	@Override
	public boolean isCompleted() {
		return completed;
	}

	@Override
	public void setCompleted(boolean completed) {
		this.completed = completed;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	@Override
	public List<WoolLoggedInteraction> getInteractionList() {
		return interactionList;
	}

	@Override
	public void setInteractionList(List<WoolLoggedInteraction> interactionList) {
		this.interactionList = interactionList;
	}
}
