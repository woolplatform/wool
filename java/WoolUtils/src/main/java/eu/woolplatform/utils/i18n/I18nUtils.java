package eu.woolplatform.utils.i18n;

import java.util.Locale;

import eu.woolplatform.utils.exception.ParseException;

public class I18nUtils {
	/**
	 * Converts a language tag like "en-US" to a locale. The language tag is the
	 * same format as in an HTTP Accept-Language header. It is case-insensitive.
	 *
	 * @param tag the language tag
	 * @return the locale
	 * @throws ParseException if the language tag is invalid
	 */
	public static Locale languageTagToLocale(String tag) throws ParseException {
		tag = tag.trim();
		if (tag.length() == 0)
			throw new ParseException("Empty language");
		String[] parts = tag.split("-");
		for (int i = 0; i < parts.length && i < 3; i++) {
			if (parts[i].length() == 0)
				throw new ParseException("Empty subtag in language: " + tag);
		}
		if (parts.length == 1)
			return new Locale(parts[0]);
		else if (parts.length == 2)
			return new Locale(parts[0], parts[1]);
		else
			return new Locale(parts[0], parts[1], parts[2]);
	}
}
