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

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Harm op den Akker (Innovation Sprint)
 */
public class WoolLanguageSet {

	@JacksonXmlProperty(localName = "source-language")
	private WoolLanguage sourceLanguage;

	@JacksonXmlProperty(localName = "translation-language")
	@JacksonXmlElementWrapper(useWrapping = false)
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
}
