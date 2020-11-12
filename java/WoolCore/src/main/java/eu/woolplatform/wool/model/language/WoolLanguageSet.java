package eu.woolplatform.wool.model.language;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.ArrayList;
import java.util.List;

public class WoolLanguageSet {

	@JacksonXmlProperty(localName = "WoolSourceLanguage")
	private WoolSourceLanguage sourceLanguage;

	@JacksonXmlProperty(localName = "TranslationLanguages")
	@JacksonXmlElementWrapper(useWrapping = true)
	private List<WoolTranslationLanguage> translationLanguages;

	// ----- Constructors

	public WoolLanguageSet() {
		this.translationLanguages = new ArrayList<WoolTranslationLanguage>();
	}

	public WoolLanguageSet(WoolSourceLanguage sourceLanguage) {
		this.sourceLanguage = sourceLanguage;
		this.translationLanguages = new ArrayList<WoolTranslationLanguage>();
	}

	// ----- Getters

	public WoolSourceLanguage getSourceLanguage() {
		return sourceLanguage;
	}

	public List<WoolTranslationLanguage> getTranslationLanguages() {
		return translationLanguages;
	}

	// ----- Setters

	public void setSourceLanguage(WoolSourceLanguage sourceLanguage) {
		this.sourceLanguage = sourceLanguage;
	}

	public void setTranslationLanguages(List<WoolTranslationLanguage> translationLanguages) {
		this.translationLanguages = translationLanguages;
	}

	// ----- Methods

	public void addTranslationLanguage(WoolTranslationLanguage translationLanguage) {
		translationLanguages.add(translationLanguage);
	}

	public String toString() {
		String result = "WoolLanguageSet: \n";
		result += "[SourceLanguage:"+sourceLanguage.toString()+"]\n";
		for(WoolTranslationLanguage wtl : translationLanguages) {
			result += wtl.toString()+"\n";
		}
		return result;
	}
}
