/*
 * Copyright 2019-2021 Innovation Sprint.
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

package eu.woolplatform.wool.model.language;

import eu.woolplatform.utils.exception.ParseException;
import eu.woolplatform.utils.xml.AbstractSimpleSAXHandler;
import eu.woolplatform.utils.xml.SimpleSAXHandler;
import org.xml.sax.Attributes;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Harm op den Akker (Innovation Sprint)
 */
public class WoolLanguageSet {

	private WoolLanguage sourceLanguage;
	private List<WoolLanguage> translationLanguages;

	// ----- Constructors

	public WoolLanguageSet() {
		this.translationLanguages = new ArrayList<WoolLanguage>();
	}

	public WoolLanguageSet(WoolLanguage sourceLanguage) {
		this.sourceLanguage = sourceLanguage;
		this.translationLanguages = new ArrayList<WoolLanguage>();
	}

	// ----- Getters

	public WoolLanguage getSourceLanguage() {
		return sourceLanguage;
	}

	public List<WoolLanguage> getTranslationLanguages() {
		return translationLanguages;
	}

	// ----- Setters

	public void setSourceLanguage(WoolLanguage sourceLanguage) {
		this.sourceLanguage = sourceLanguage;
	}

	public void setTranslationLanguages(List<WoolLanguage> translationLanguages) {
		this.translationLanguages = translationLanguages;
	}

	// ----- Methods

	public void addTranslationLanguage(WoolLanguage translationLanguage) {
		translationLanguages.add(translationLanguage);
	}

	public String toString() {
		String result = "WoolLanguageSet: \n";
		result += "[SourceLanguage:"+sourceLanguage.toString()+"]\n";
		for(WoolLanguage woolLanguage : translationLanguages) {
			result += woolLanguage.toString()+"\n";
		}
		return result;
	}

	// ----- XML Handling

	public static SimpleSAXHandler<WoolLanguageSet> getXMLHandler() {
		return new XMLHandler();
	}

	private static class XMLHandler extends AbstractSimpleSAXHandler<WoolLanguageSet> {

		private WoolLanguageSet result;
		private SimpleSAXHandler<WoolLanguage> languageHandler = null;

		@Override
		public void startElement(String name, Attributes atts, List<String> parents) throws ParseException {
			if(name.equals("language-set")) {
				result = new WoolLanguageSet();
			} else if(name.equals("source-language") || name.equals("translation-language")) {
				languageHandler = WoolLanguage.getXMLHandler();
				languageHandler.startElement(name,atts,parents);
			} else {
				if(languageHandler != null) languageHandler.startElement(name,atts,parents);
			}
		}

		@Override
		public void endElement(String name, List<String> parents) throws ParseException {
			if(languageHandler != null) languageHandler.endElement(name,parents);
			if(name.equals("source-language")) {
				WoolLanguage sourceLanguage = languageHandler.getObject();
				result.setSourceLanguage(sourceLanguage);
				languageHandler = null;
			} else if(name.equals("translation-language")) {
				WoolLanguage translationLanguage = languageHandler.getObject();
				result.addTranslationLanguage(translationLanguage);
				languageHandler = null;
			}
		}

		@Override
		public void characters(String ch, List<String> parents) throws ParseException {

		}

		@Override
		public WoolLanguageSet getObject() {
			return result;
		}
	}
}
