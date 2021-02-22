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
