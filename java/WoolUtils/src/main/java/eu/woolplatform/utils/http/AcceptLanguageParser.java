package eu.woolplatform.utils.http;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu.woolplatform.utils.exception.ParseException;
import eu.woolplatform.utils.i18n.I18nUtils;

public class AcceptLanguageParser {
	public static List<Locale> parse(String acceptLanguage) {
		List<Locale> result = new ArrayList<>();
		acceptLanguage = acceptLanguage.trim();
		if (acceptLanguage.length() == 0)
			return result;
		String[] languages = acceptLanguage.split(",");
		List<LanguageWeight> lws = new ArrayList<>();
		for (String language : languages) {
			language = language.trim();
			lws.add(parseLanguageWeight(language));
		}
		Collections.sort(lws, new WeightComparator());
		for (LanguageWeight lw : lws) {
			if (lw.language.equals("*"))
				return result;
			Locale locale;
			try {
				locale = I18nUtils.languageTagToLocale(lw.language);
			} catch (ParseException ex) {
				locale = null;
			}
			if (locale != null)
				result.add(locale);
		}
		return result;
	}

	private static LanguageWeight parseLanguageWeight(String languageWeight) {
		LanguageWeight result = new LanguageWeight();
		result.weight = 1;
		languageWeight = languageWeight.trim();
		int sep = languageWeight.indexOf(';');
		if (sep == -1) {
			result.language = languageWeight;
			return result;
		}
		result.language = languageWeight.substring(0, sep).trim();
		String weightSpec = languageWeight.substring(sep + 1).trim();
		Pattern regex = Pattern.compile("q\\s*=\\s*(\\S+)");
		Matcher m = regex.matcher(weightSpec);
		if (!m.matches())
			return result;
		String weight = m.group(1);
		try {
			result.weight = Float.parseFloat(weight);
		} catch (NumberFormatException ex) {
			return result;
		}
		return result;
	}
	
	private static class LanguageWeight {
		public String language;
		public float weight;
	}
	
	private static class WeightComparator implements
	Comparator<LanguageWeight> {
		@Override
		public int compare(LanguageWeight o1, LanguageWeight o2) {
			return -Float.compare(o1.weight, o2.weight);
		}
	}
}
