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

package eu.woolplatform.wool.parser;

/**
 * This class describes a WOOL file. This can be a ".wool" dialogue file or a
 * ".json" translation file.
 *
 * @author Dennis Hofs (RRD)
 */
public class WoolFileDescription {
	
	private String mainSpeaker;
	private String language;
	private String fileName;
	
	// -------------------- Constructors

	public WoolFileDescription() {	}

	/**
	 * Constructs a new description. The file name can be a ".wool" dialogue
	 * file or a ".json" translation file.
	 *
	 * @param mainSpeaker the main speaker
	 * @param language the language code (for example en_GB)
	 * @param fileName the file name (.wool or .json)
	 */
	public WoolFileDescription(String mainSpeaker, String language, String fileName) {
		this.setMainSpeaker(mainSpeaker);
		this.setLanguage(language);
		this.setFileName(fileName);
	}
	
	// -------------------- Getters

	/**
	 * Returns the main speaker.
	 *
	 * @return the main speaker
	 */
	public String getMainSpeaker() {
		return this.mainSpeaker;
	}

	/**
	 * Return the language code (for example en_GB).
	 *
	 * @return the language code (for example en_GB)
	 */
	public String getLanguage() {
		return this.language;
	}

	/**
	 * Returns the file name. This can be a ".wool" dialogue file or a ".json"
	 * translation file.
	 *
	 * @return the file name (.wool or .json)
	 */
	public String getFileName() {
		return this.fileName;
	}
	
	// -------------------- Setters

	/**
	 * Sets the main speaker.
	 *
	 * @param mainSpeaker the main speaker
	 */
	public void setMainSpeaker(String mainSpeaker) {
		this.mainSpeaker = mainSpeaker;
	}

	/**
	 * Sets the language code (for example en_GB).
	 *
	 * @param language the language code (for example en_GB)
	 */
	public void setLanguage(String language) {
		this.language = language;
	}

	/**
	 * Sets the file name. This can be a ".wool" dialogue file or a ".json"
	 * translation file.
	 *
	 * @param fileName the file name (.wool or .json)
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		WoolFileDescription other = (WoolFileDescription)obj;
		if (!mainSpeaker.equals(other.mainSpeaker))
			return false;
		if (!language.equals(other.language))
			return false;
		if (!fileName.equals(other.fileName))
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		int result = mainSpeaker.hashCode();
		result = 31 * result + language.hashCode();
		result = 31 * result + fileName.hashCode();
		return result;
	}

	public String toString() {
		String fileType;
		if (fileName.endsWith(".wool"))
			fileType = "Dialogue file";
		else if (fileName.endsWith(".json"))
			fileType = "Translation file";
		else
			fileType = "Unknown file";
		return String.format(
				"%s \"%s\" with main speaker \"%s\" in language \"%s\"",
				fileType, fileName, mainSpeaker, language);
	}
}
