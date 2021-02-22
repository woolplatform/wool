package eu.woolplatform.wool.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import eu.woolplatform.wool.exception.WoolDuplicateLanguageCodeException;
import eu.woolplatform.wool.exception.WoolUnknownLanguageCodeException;
import eu.woolplatform.wool.model.language.WoolLanguage;
import eu.woolplatform.wool.model.language.WoolLanguageMap;
import eu.woolplatform.wool.model.language.WoolLanguageSet;

/**
 * The {@link WoolProjectMetaData} class is the object representation of a wool
 * metadata .xml file. This object can be serialized into an XML file or be constructed
 * from XML using a Jackson XMLMapper. Additionally contains methods for modifying the
 * contents of a wool project metadata specification.
 *
 * @author Harm op den Akker (Innovation Sprint)
 */
@JacksonXmlRootElement(localName = "wool-project")
public class WoolProjectMetaData {

	@JacksonXmlProperty(isAttribute = true)
	private String name;

	@JsonIgnore
	private String basePath;

	private String description;

	@JacksonXmlProperty(isAttribute = true)
	private String version;

	@JacksonXmlProperty(localName = "language-map")
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
	 * @returns the newly created {@link WoolLanguageSet}
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
		result += woolLanguageMap.toString();
		return result;
	}
}
