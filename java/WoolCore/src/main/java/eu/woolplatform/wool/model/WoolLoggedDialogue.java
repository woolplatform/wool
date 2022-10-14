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
