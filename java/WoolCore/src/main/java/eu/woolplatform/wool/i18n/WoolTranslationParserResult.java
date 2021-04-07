package eu.woolplatform.wool.i18n;

import eu.woolplatform.utils.exception.ParseException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WoolTranslationParserResult {
	private Map<WoolTranslatable,List<WoolContextTranslation>> translations = null;
	private List<ParseException> parseErrors = new ArrayList<>();
	private List<String> warnings = new ArrayList<>();

	public Map<WoolTranslatable,List<WoolContextTranslation>> getTranslations() {
		return translations;
	}

	public void setTranslations(
			Map<WoolTranslatable,List<WoolContextTranslation>> translations) {
		this.translations = translations;
	}

	public List<ParseException> getParseErrors() {
		return parseErrors;
	}

	public void setParseErrors(List<ParseException> parseErrors) {
		this.parseErrors = parseErrors;
	}

	public List<String> getWarnings() {
		return warnings;
	}

	public void setWarnings(List<String> warnings) {
		this.warnings = warnings;
	}
}
