package eu.woolplatform.wool.model.language;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.ArrayList;
import java.util.List;

public class WoolLanguageMap {

	@JacksonXmlProperty(localName = "WoolLanguageSet")
	@JacksonXmlElementWrapper(useWrapping = false)
	private List<WoolLanguageSet> languageSets;

	// ----- Constructors

	public WoolLanguageMap() {
		languageSets = new ArrayList<WoolLanguageSet>();
	}

	// ----- Getters

	public List<WoolLanguageSet> getLanguageSets() {
		return languageSets;
	}

	// ----- Setters

	public void setLanguageSets(List<WoolLanguageSet> languageSets) {
		this.languageSets = languageSets;
	}

	// ----- Methods

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
