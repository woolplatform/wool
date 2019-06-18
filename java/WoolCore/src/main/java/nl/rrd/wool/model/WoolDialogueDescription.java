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
