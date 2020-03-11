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

package nl.rrd.wool.i18n;

import com.fasterxml.jackson.core.type.TypeReference;
import nl.rrd.wool.exception.LineNumberParseException;
import nl.rrd.wool.exception.ParseException;
import nl.rrd.wool.json.JsonMapper;
import nl.rrd.wool.model.WoolNodeBody;
import nl.rrd.wool.parser.WoolBodyParser;
import nl.rrd.wool.parser.WoolBodyToken;
import nl.rrd.wool.parser.WoolBodyTokenizer;
import nl.rrd.wool.utils.FileUtils;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This class can parse a WOOL translation file. The file should contain a
 * JSON object with key-value pairs as exported by POEditor. There are two types
 * of key-value pairs:
 *
 * <p><ul>
 * <li>Key and value are strings. The key is a translatable in one language,
 * and the value is a translatable in another language. A translatable should be
 * the string representation of a {@link WoolTranslatable WoolTranslatable}.
 * That is a text that may include variables and &lt;&lt;input&gt;&gt;
 * commands.</li>
 * <li>The key is a string the value is a JSON object. In this case the key is
 * a context string, and the value contains translatable key-value pairs.</li>
 * </ul></p>
 *
 * <p>This parser ignores context strings and returns a flat map of
 * translatables. This means that it does not support different translations of
 * the same string with different contexts.</p>
 *
 * @author Dennis Hofs (RRD)
 */
public class WoolTranslationParser {
	public static Map<WoolTranslatable,WoolTranslatable> parse(URL url)
			throws ParseException, IOException {
		try (InputStream input = url.openStream()) {
			return parse(input);
		}
	}

	public static Map<WoolTranslatable,WoolTranslatable> parse(File file)
			throws ParseException, IOException {
		try (InputStream input = new FileInputStream(file)) {
			return parse(input);
		}
	}

	public static Map<WoolTranslatable,WoolTranslatable> parse(
			InputStream input) throws ParseException, IOException{
		return parse(new InputStreamReader(input, StandardCharsets.UTF_8));
	}

	public static Map<WoolTranslatable,WoolTranslatable> parse(Reader reader)
			throws ParseException, IOException {
		Map<WoolTranslatable, WoolTranslatable> translations =
				new LinkedHashMap<>();
		String json = FileUtils.readFileString(reader);
		if (json.trim().isEmpty())
			throw new ParseException("Empty translation file");
		Map<String,?> map = JsonMapper.parse(json,
				new TypeReference<Map<String,?>>() {});
		parse(map, translations);
		return translations;
	}

	private static void parse(Map<String,?> map,
			Map<WoolTranslatable,WoolTranslatable> translations)
			throws ParseException {
		for (String key : map.keySet()) {
			Object value = map.get(key);
			if (value instanceof String) {
				parseTranslatable(key, (String)value, translations);
			} else {
				parseContextMap(key, value, translations);
			}
		}
	}

	private static void parseTranslatable(String key, String value,
			Map<WoolTranslatable,WoolTranslatable> translations)
			throws ParseException {
		WoolTranslatable transKey;
		try {
			transKey = parseTranslationString(key);
		} catch (ParseException ex) {
			throw new ParseException(String.format(
					"Failed to parse translation key \"%s\"", key) + ": " +
					ex.getMessage(), ex);
		}
		WoolTranslatable transValue;
		try {
			transValue = parseTranslationString(value);
		} catch (ParseException ex) {
			throw new ParseException(String.format(
					"Failed to parse translation value for key \"%s\"", key) +
					": " + value + ": " + ex.getMessage(), ex);
		}
		if (translations.containsKey(transKey)) {
			throw new ParseException("Found duplicate translation key: " +
					transKey);
		}
		translations.put(transKey, transValue);
	}

	private static void parseContextMap(String key, Object value,
			Map<WoolTranslatable,WoolTranslatable> translations)
			throws ParseException {
		Map<String,?> map;
		try {
			map = JsonMapper.convert(value,
					new TypeReference<Map<String, ?>>() {});
		} catch (IllegalArgumentException ex) {
			throw new ParseException(
					"Failed to parse translation map after context key \"" +
					key + "\": " + ex.getMessage(), ex);
		}
		parse(map, translations);
	}

	private static WoolTranslatable parseTranslationString(String translation)
			throws ParseException {
		WoolBodyTokenizer tokenizer = new WoolBodyTokenizer();
		List<WoolBodyToken> tokens;
		try {
			tokens = tokenizer.readBodyTokens(translation, 1);
		} catch (LineNumberParseException ex) {
			throw new ParseException(
					"Invalid translation string: " + translation +
					": " + ex.getError());
		}
		WoolBodyParser parser = new WoolBodyParser(null);
		WoolNodeBody body;
		try {
			body = parser.parse(tokens, Collections.singletonList("input"));
		} catch (LineNumberParseException ex) {
			throw new ParseException(
					"Invalid translation string: " + translation +
					": " + ex.getError());
		}
		WoolTranslatableExtractor extractor = new WoolTranslatableExtractor();
		List<WoolTranslatable> translatables = extractor.extractFromBody(body);
		if (translatables.size() == 0) {
			throw new ParseException(
					"Invalid translation string: " + translation +
					": No translatable text found");
		}
		if (translatables.size() != 1) {
			throw new ParseException(
					"Invalid translation string: " + translation +
					": Multiple translatable texts found");
		}
		return translatables.get(0);
	}
}
