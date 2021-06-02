/*
 * Copyright 2019-2021 WOOL Platform.
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

import eu.woolplatform.utils.exception.ParseException;
import eu.woolplatform.utils.xml.AbstractSimpleSAXHandler;
import eu.woolplatform.utils.xml.SimpleSAXHandler;
import eu.woolplatform.utils.xml.XMLWriter;
import eu.woolplatform.wool.exception.WoolDuplicateLanguageCodeException;
import eu.woolplatform.wool.exception.WoolUnknownLanguageCodeException;
import eu.woolplatform.wool.model.language.WoolLanguage;
import eu.woolplatform.wool.model.language.WoolLanguageMap;
import eu.woolplatform.wool.model.language.WoolLanguageSet;
import org.xml.sax.Attributes;

import java.io.IOException;
import java.util.List;

/**
 * The {@link WoolProjectMetaData} class is the object representation of a wool
 * metadata .xml file. This object can be serialized into an XML file using an {@link XMLWriter} or
 * be constructed from an XML file using a {@link SimpleSAXHandler}. Additionally contains methods
 * for dynamically modifying the contents of a {@link WoolProjectMetaData} specification while
 * maintaining certain constraints.
 *
 * @author Harm op den Akker (Innovation Sprint)
 */
public class WoolProjectMetaData {

	private String name;
	private String basePath;
	private String description;
	private String version;
	private WoolLanguageMap woolLanguageMap;

	// ----- Constructors

	/**
	 * Creates an instance of an empty {@link WoolProjectMetaData} object.
	 */
	public WoolProjectMetaData() { }

	/**
	 * Creates an instance of a {@link WoolProjectMetaData} object with the given parameters.
	 * @param name a descriptive name of the wool project.
	 * @param basePath the folder in which this wool project is stored.
	 * @param description a textual description of this wool project.
	 * @param version free-form version information (e.g. v0.1.0).
	 * @param woolLanguageMap contains all the languages supported by this wool project.
	 */
	public WoolProjectMetaData(String name, String basePath, String description, String version, WoolLanguageMap woolLanguageMap) {
		this.name = name;
		this.basePath = basePath;
		this.description = description;
		this.version = version;
		this.woolLanguageMap = woolLanguageMap;
	}

	// ----- Getters

	/**
	 * Returns the name of this {@link WoolProject} as a String.
	 * @return the name of this {@link WoolProject} as a String.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns a String representation of the base path of this {@link WoolProject}.
	 * @return a String representation of the base path of this {@link WoolProject}.
	 */
	public String getBasePath() {
		return basePath;
	}

	/**
	 * Returns the description of this {@link WoolProject}.
	 * @return the description of this {@link WoolProject}.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Returns the version of this {@link WoolProject}.
	 * @return the version of this {@link WoolProject}.
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * Returns the {@link WoolLanguageMap} that contains a description of all
	 * languages supported in this {@link WoolProject} and their mapping from source-
	 * to translation languages.
	 * @return the {@link WoolLanguageMap} for this {@link WoolProjectMetaData}.
	 */
	public WoolLanguageMap getWoolLanguageMap() {
		return woolLanguageMap;
	}

	// ----- Setters

	/**
	 * Sets the name of this wool project.
	 * @param name the name of this wool project.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Sets the the base path for this wool project as a {@link String}.
	 * @param basePath the the base path for this wool project as a {@link String}.
	 */
	public void setBasePath(String basePath) {
		this.basePath = basePath;
	}

	/**
	 * Sets the description text for this wool project.
	 * @param description the description text for this wool project.
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Sets the version string for this wool project.
	 * @param version the version string for this wool project.
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * Sets the {@link WoolLanguageMap} for this wool project, containing a mapping of all
	 * supported source- and translation languages.
	 * @param woolLanguageMap the {@link WoolLanguageMap} for this wool project.
	 */
	public void setWoolLanguageMap(WoolLanguageMap woolLanguageMap) {
		this.woolLanguageMap = woolLanguageMap;
	}

	// ----- Methods

	/**
	 * Attempts to set a new language with the given {@code name} and {@code code} to the given
	 * {@link WoolLanguageSet} as the source language in this wool project. This methods will succeed
	 * and return {@code true} if and only if a language with the given {@code code} does not exist yet
	 * in the {@link WoolLanguageMap} of this wool project.
	 *
	 * @param name the name of the source language to add.
	 * @param code the code of the source language to add.
	 * @param woolLanguageSet the language set to which to add the language
	 * @throws WoolDuplicateLanguageCodeException in case a language with the given {@code code} already exists in this wool project.
	 */
	public void setSourceLanguage(String name, String code, WoolLanguageSet woolLanguageSet) throws WoolDuplicateLanguageCodeException {
		if(languageExists(code)) throw new WoolDuplicateLanguageCodeException("A language with the given language code '"+code+"' is already defined in this wool project.",code);

		woolLanguageSet.setSourceLanguage(new WoolLanguage(name,code));
	}

	/**
	 * Attempts to add a new source language to this wool project with the given {@code name} and {@code code} by
	 * creating a new {@link WoolLanguageSet} for it. This method will fail with a {@link WoolDuplicateLanguageCodeException}
	 * if a language with the given {@code code} already exists in this wool project. Otherwise, it will return a pointer
	 * to the newly created {@link WoolLanguageSet}.
	 * @param name the name of the source language to add.
	 * @param code the code of the source language to add.
	 * @throws WoolDuplicateLanguageCodeException in case a language with the given {@code code} already exists in this wool project.
	 * @return the newly created {@link WoolLanguageSet}
	 */
	public WoolLanguageSet addSourceLanguage(String name, String code) throws WoolDuplicateLanguageCodeException {
		if(languageExists(code)) throw new WoolDuplicateLanguageCodeException("A language with the given language code '"+code+"' is already defined in this wool project.",code);

		WoolLanguageSet wls = new WoolLanguageSet(new WoolLanguage(name, code));
		woolLanguageMap.addLanguageSet(wls);
		return wls;
	}

	/**
	 * Attempts to add a new language with the given {@code name} and {@code code} to the given
	 * {@link WoolLanguageSet} as a translation language. This methods will succeed and return
	 * {@code true} if and only if a language with the given {@code code} does not exist yet
	 * in this {@link WoolLanguageMap}.
	 *
	 * @param name the name of the language to add.
	 * @param code the code of the language to add.
	 * @param woolLanguageSet the language set to which to add the language
	 * @throws WoolDuplicateLanguageCodeException in case a language with the given {@code code} already exists in this wool project.
	 */
	public void addTranslationLanguage(String name, String code, WoolLanguageSet woolLanguageSet) throws WoolDuplicateLanguageCodeException {
		if(languageExists(code)) throw new WoolDuplicateLanguageCodeException("A language with the given language code '"+code+"' is already defined in this wool project.",code);

		woolLanguageSet.addTranslationLanguage(new WoolLanguage(name,code));
	}

	/**
	 * Checks whether a language with the given {@code languageCode} exists in this {@link WoolLanguageMap}.
	 * @param languageCode the language code to search for
	 * @return true if the given {@code languageCode} exists, false otherwise
	 */
	public boolean languageExists(String languageCode) {
		for(WoolLanguageSet woolLanguageSet : woolLanguageMap.getLanguageSets()) {
			if(woolLanguageSet.getSourceLanguage().getCode().equals(languageCode)) return true;
			for(WoolLanguage woolTranslationLanguage : woolLanguageSet.getTranslationLanguages()) {
				if(woolTranslationLanguage.getCode().equals(languageCode)) return true;
			}
		}
		return false;
	}

	/**
	 * Returns the {@link WoolLanguageSet} in this wool project for which the source language code
	 * matches the given {@code code}.
	 * @param sourceLanguageCode the language code of the source language for which to lookup its {@link WoolLanguageSet}.
	 * @return the {@link WoolLanguageSet} with a source language with the given {@code code} or throws an
	 * {@link WoolUnknownLanguageCodeException} if no such {@link WoolLanguageSet} exists.
	 * @throws WoolUnknownLanguageCodeException if no language set exists with the given source language code.
	 */
	public WoolLanguageSet getLanguageSetForSourceLanguage(String sourceLanguageCode) throws WoolUnknownLanguageCodeException {
		for(WoolLanguageSet wls : woolLanguageMap.getLanguageSets()) {
			if(wls.getSourceLanguage().getCode().equals(sourceLanguageCode)) return wls;
		}
		throw new WoolUnknownLanguageCodeException("No language set found with source language '"+sourceLanguageCode+"'.",sourceLanguageCode);
	}

	public String toString() {
		String result = "";
		result += "Wool Project Metadata:\n";
		result += "[name:"+name+"]\n";
		result += "[basePath:"+basePath+"]\n";
		result += "[description:"+description+"]\n";
		result += "[version:"+version+"]\n";
		if(woolLanguageMap != null)
			result += woolLanguageMap.toString();
		return result;
	}

	// ----- XML Handling

	/**
	 * Writes this {@link WoolProjectMetaData} to file using the given {@link XMLWriter}.
	 * @param writer the XML writer
	 * @throws IOException if a writing error occurs
	 */
	public void writeXML(XMLWriter writer) throws IOException {
		writer.writeStartElement("wool-project");
		writer.writeAttribute("name",name);
		writer.writeAttribute("version",version);

		writer.writeStartElement("description");
		writer.writeCharacters(description);
		writer.writeEndElement(); // description

		woolLanguageMap.writeXML(writer);

		writer.writeEndElement(); // wool-project
		writer.close();
	}

	/**
	 * Returns a {@link SimpleSAXHandler} that is able to parse the contents of an .xml file
	 * to a {@link WoolProjectMetaData} object.
	 * @return the XMl handler
	 */
	public static SimpleSAXHandler<WoolProjectMetaData> getXMLHandler() {
		return new XMLHandler();
	}

	/**
	 * TODO: Test error handling.
	 * TODO: Check for duplicate languages.
	 */
	private static class XMLHandler extends AbstractSimpleSAXHandler<WoolProjectMetaData> {

		private WoolProjectMetaData result;
		private int rootLevel = 0;
		private boolean inDescription = false;
		private SimpleSAXHandler<WoolLanguageMap> languageMapHandler = null;

		@Override
		public void startElement(String name, Attributes atts, List<String> parents) throws ParseException {

			if(rootLevel == 0) {
				if(!name.equals("wool-project")) {
					throw new ParseException("Expected element 'wool-project' while parsing wool project metadata, found '"+name+"'.");
				} else {
					result = new WoolProjectMetaData();
					if(atts.getValue("name") == null) {
						throw new ParseException("Missing attribute 'name' in element 'wool-project' while parsing wool project metadata.");
					} else {
						result.setName(atts.getValue("name"));
					}
					if(atts.getValue("version") != null) {
						result.setVersion(atts.getValue("version"));
					} else {
						result.setVersion("");
					}
					rootLevel++;
				}
			} else if(rootLevel == 1) {
				if(name.equals("description")) {
					inDescription = true;
				} else if(name.equals("language-map")) {
					languageMapHandler = WoolLanguageMap.getXMLHandler();
					languageMapHandler.startElement(name,atts,parents);
				} else {
					if(languageMapHandler != null) {
						languageMapHandler.startElement(name,atts,parents);
					} else {
						throw new ParseException("Unexpected element while parsing wool project metadata: '"+name+"'");
					}
				}
			}
		}

		@Override
		public void endElement(String name, List<String> parents) throws ParseException {
			if(languageMapHandler != null) {
				languageMapHandler.endElement(name,parents);
			} else if (name.equals("description")) inDescription = false;

			if(name.equals("language-map")) {
				result.setWoolLanguageMap(languageMapHandler.getObject());
			}
		}

		@Override
		public void characters(String ch, List<String> parents) throws ParseException {
			if(inDescription) {
				result.setDescription(ch);
			}
		}

		@Override
		public WoolProjectMetaData getObject() {
			return result;
		}
	}
}
