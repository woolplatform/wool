package nl.rrd.wool.i18n;

import nl.rrd.wool.exception.ParseException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WoolTranslationParserResult {
	private Map<WoolTranslatable,WoolTranslatable> translations = null;
	private List<ParseException> parseErrors = new ArrayList<>();
	private List<String> warnings = new ArrayList<>();

	public Map<WoolTranslatable,WoolTranslatable> getTranslations() {
		return translations;
	}

	public void setTranslations(
			Map<WoolTranslatable,WoolTranslatable> translations) {
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
