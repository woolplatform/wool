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

package eu.woolplatform.wool.i18n;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * A {@link WoolTranslationFile} is an object representation of a JSON file
 * that contains translations for a single .wool script.
 * The body of a {@link WoolTranslationFile} consists of a mapping from
 * speakerNames to a map of {term,translation}-pairs, e.g.:
 *
 * {
 *   "speaker1" : {
 *     "term1": "translation1",
 *     "term2": "translation2"
 *   },
 *   "speaker2" : {
 *     "term3":"translation3",
 *     "term4":"translation4",
 *   }
 * }
 */
public class WoolTranslationFile {

	private String fileName;
	private Map<String, Map<String,String>> contentMap;

	// ----- Constructors

	public WoolTranslationFile(String fileName) {
		this.fileName = fileName;
		this.contentMap = new HashMap<>();
	}

	// ----- Getters & Setters

	public String getFileName() {
		return fileName;
	}

	public Map<String, Map<String,String>> getContentMap() {
		return contentMap;
	}

	// ----- Functions

	public void addTerm(String speakerName, String term, String translation) {
		if(contentMap.containsKey(speakerName)) {
			Map<String,String> terms = contentMap.get(speakerName);
			terms.put(term,translation);
		} else {
			Map<String,String> terms = new HashMap<>();
			terms.put(term,translation);
			contentMap.put(speakerName,terms);
		}
	}

	/**
	 * Writes this {@link WoolTranslationFile} to a file, specified by {@link #getFileName()}
	 * in the given {@code directory}.
	 * @param directory the directory in which to store the .json file output
	 * @throws IOException in case the given directory is not a directory, or another file writing
	 * error occurs.
	 */
	public void writeToFile(File directory) throws IOException {
		if(!directory.isDirectory()) throw new IOException("The given directory parameter is not a directory.");

		// create object mapper instance
		ObjectMapper mapper = new ObjectMapper();

		// Create the json file object based on the given directory and this
		// object's fileName definition
		File jsonFile = new File(directory.getAbsolutePath() + File.separator + fileName + ".json");

		// Create an ObjectWriter with pretty printing
		ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());

		// convert map to JSON file
		writer.writeValue(jsonFile, contentMap);
	}
}
