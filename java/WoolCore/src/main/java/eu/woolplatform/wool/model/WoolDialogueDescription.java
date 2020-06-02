/*
 * Copyright 2019 Roessingh Research and Development.
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

package eu.woolplatform.wool.model;

public class WoolDialogueDescription {
	
	private String language;
	private String dialogueName;
	
	// -------------------- Constructors

	public WoolDialogueDescription() {	}

	public WoolDialogueDescription(String language, String dialogueName) {
		this.setLanguage(language);
		this.setDialogueName(dialogueName);
	}
	
	// -------------------- Getters

	public String getLanguage() {
		return this.language;
	}
	
	public String getDialogueName() {
		return this.dialogueName;
	}
	
	// -------------------- Setters

	public void setLanguage(String language) {
		this.language = language;
	}

	public void setDialogueName(String dialogueName) {
		this.dialogueName = dialogueName;
	}

	@Override
	public int hashCode() {
		int result = language.hashCode();
		result = 31 * result + dialogueName.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj == null || obj.getClass() != getClass())
			return false;
		WoolDialogueDescription other = (WoolDialogueDescription)obj;
		if (!language.equals(other.language))
			return false;
		if (!dialogueName.equals(other.dialogueName))
			return false;
		return true;
	}

	public String toString() {
		return "Dialogue '" + this.dialogueName + "' in language '" +
				this.language + "'.";
	}
}
