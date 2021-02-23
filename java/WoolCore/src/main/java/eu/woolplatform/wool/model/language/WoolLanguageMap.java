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
 * A {@link WoolLanguageMap} is a wrapper object containing a {@link List} of {@link WoolLanguageSet}s,
 * as well as some convenience methods for manipulating {@link WoolLanguageSet}s.
 *
 * @author Harm op den Akker (Innovation Sprint)
 */
public class WoolLanguageMap {

	@JacksonXmlProperty(localName = "language-set")
	@JacksonXmlElementWrapper(useWrapping = false)
	private List<WoolLanguageSet> languageSets;

	// ----- Constructors

	/**
	 * Creates an instance of an empty {@link WoolLanguageMap}.
	 */
	public WoolLanguageMap() {
		languageSets = new ArrayList<>();
	}

	/**
	 * Creates an instance of a {@link WoolLanguageMap} with a given {@link WoolLanguageSet}.
	 * @param languageSets a list of {@link WoolLanguageSet}s contained in this {@link WoolLanguageMap}.
	 */
	public WoolLanguageMap(List<WoolLanguageSet> languageSets) {
		this.languageSets = languageSets;
	}

	// ----- Getters

	/**
	 * Returns the {@link List} of {@link WoolLanguageSet}s in this {@link WoolLanguageMap}.
	 * @return the {@link List} of {@link WoolLanguageSet}s in this {@link WoolLanguageMap}.
	 */
	public List<WoolLanguageSet> getLanguageSets() {
		return languageSets;
	}

	// ----- Setters

	/**
	 * Sets the {@link List} of {@link WoolLanguageSet}s for this {@link WoolLanguageMap}.
	 * @param languageSets the {@link List} of {@link WoolLanguageSet}s for this {@link WoolLanguageMap}.
	 */
	public void setLanguageSets(List<WoolLanguageSet> languageSets) {
		this.languageSets = languageSets;
	}

	// ----- Methods

	/**
	 * Adds the given {@link WoolLanguageSet} to this {@link WoolLanguageMap}.
	 * @param woolLanguageSet the {@link WoolLanguageSet} to add to this {@link WoolLanguageMap}.
	 */
	public void addLanguageSet(WoolLanguageSet woolLanguageSet) {
		languageSets.add(woolLanguageSet);
	}

	public String toString() {
		String result = "WoolLanguageMap: \n";
		for(WoolLanguageSet wls : languageSets) {
			result += wls.toString();
		}
		return result;
	}
}
