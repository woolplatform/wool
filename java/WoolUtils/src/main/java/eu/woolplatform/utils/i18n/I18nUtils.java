/*
 * Copyright 2019 Roessingh Research and Development.
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

package eu.woolplatform.utils.i18n;

import eu.woolplatform.utils.exception.ParseException;

import java.util.Locale;

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

	/**
	 * Converts a locale to a language tag like "en-US". The language tag is the
	 * same format as in an HTTP Accept-Language header. It always starts with
	 * a lower-case language code and optionally it may be followed by an
	 * upper-case country code and a lower-case variant code.
	 *
	 * @param locale the locale
	 * @return the language tag
	 */
	public static String localeToLanguageTag(Locale locale) {
		StringBuilder builder = new StringBuilder(
				locale.getLanguage().toLowerCase());
		if (locale.getCountry().isEmpty())
			return builder.toString();
		builder.append("-").append(locale.getCountry().toUpperCase());
		if (locale.getVariant().isEmpty())
			return builder.toString();
		builder.append("-").append(locale.getVariant().toLowerCase());
		return builder.toString();
	}

	/**
	 * Converts a locale to a language/country tag like "en-US". The result will
	 * not contain a variant. The language tag is the same format as in an HTTP
	 * Accept-Language header. It always starts with a lower-case language code
	 * and optionally it may be followed by an upper-case country code.
	 *
	 * @param locale the locale
	 * @return the language/country tag
	 */
	public static String localeToLanguageCountryTag(Locale locale) {
		StringBuilder builder = new StringBuilder(
				locale.getLanguage().toLowerCase());
		if (locale.getCountry().isEmpty())
			return builder.toString();
		builder.append("-").append(locale.getCountry().toUpperCase());
		return builder.toString();
	}
}
