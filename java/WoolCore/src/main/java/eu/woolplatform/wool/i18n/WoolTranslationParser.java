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

package eu.woolplatform.wool.i18n;

import com.fasterxml.jackson.core.type.TypeReference;
import eu.woolplatform.utils.exception.LineNumberParseException;
import eu.woolplatform.utils.exception.ParseException;
import eu.woolplatform.utils.io.FileUtils;
import eu.woolplatform.utils.json.JsonMapper;
import eu.woolplatform.wool.model.WoolNodeBody;
import eu.woolplatform.wool.parser.WoolBodyParser;
import eu.woolplatform.wool.parser.WoolBodyToken;
import eu.woolplatform.wool.parser.WoolBodyTokenizer;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

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
	public static WoolTranslationParserResult parse(URL url)
			throws IOException {
		try (InputStream input = url.openStream()) {
			return parse(input);
		}
	}

	public static WoolTranslationParserResult parse(File file)
			throws IOException {
		try (InputStream input = new FileInputStream(file)) {
			return parse(input);
		}
	}

	public static WoolTranslationParserResult parse(InputStream input)
			throws IOException{
		return parse(new InputStreamReader(input, StandardCharsets.UTF_8));
	}

	public static WoolTranslationParserResult parse(Reader reader)
			throws IOException {
		WoolTranslationParserResult result = new WoolTranslationParserResult();
		Map<WoolTranslatable,List<WoolContextTranslation>> translations =
				new LinkedHashMap<>();
		String json = FileUtils.readFileString(reader);
		if (json.trim().isEmpty()) {
			result.getWarnings().add("Empty translation file");
			result.setTranslations(translations);
			return result;
		}
		Map<String,?> map;
		try {
			map = JsonMapper.parse(json, new TypeReference<Map<String, ?>>() {});
		} catch (ParseException ex) {
			result.getParseErrors().add(ex);
			return result;
		}
		parse(new LinkedHashSet<>(), map, translations, result);
		if (result.getParseErrors().isEmpty())
			result.setTranslations(translations);
		return result;
	}

	private static void parse(Set<String> context, Map<String,?> map,
			Map<WoolTranslatable,List<WoolContextTranslation>> translations,
			WoolTranslationParserResult parseResult) {
		for (String key : map.keySet()) {
			Object value = map.get(key);
			if (value instanceof String) {
				parseTranslatable(context, key, (String)value, translations,
						parseResult);
			} else {
				parseContextMap(key, value, translations, parseResult);
			}
		}
	}

	private static void parseTranslatable(Set<String> context, String key,
			String value,
			Map<WoolTranslatable,List<WoolContextTranslation>> translations,
			WoolTranslationParserResult parseResult) {
		boolean success = true;
		WoolTranslatable source = null;
		try {
			source = parseTranslationString(key);
		} catch (ParseException ex) {
			parseResult.getParseErrors().add(new ParseException(String.format(
					"Failed to parse translation key \"%s\"", key) + ": " +
					ex.getMessage(), ex));
			success = false;
		}
		if (source != null) {
			try {
				checkDuplicateTranslation(source, context, translations);
			} catch (ParseException ex) {
				parseResult.getParseErrors().add(ex);
				success = false;
			}
		}
		if (value.trim().isEmpty()) {
			parseResult.getWarnings().add(String.format(
					"Empty translation value for key \"%s\"", key));
			return;
		}
		WoolTranslatable transValue = null;
		try {
			transValue = parseTranslationString(value);
		} catch (ParseException ex) {
			parseResult.getParseErrors().add(new ParseException(String.format(
					"Failed to parse translation value for key \"%s\"", key) +
					": " + value + ": " + ex.getMessage(), ex));
			success = false;
		}
		if (success) {
			List<WoolContextTranslation> transList = translations.get(source);
			if (transList == null) {
				transList = new ArrayList<>();
				translations.put(source, transList);
			}
			transList.add(new WoolContextTranslation(context, transValue));
		}
	}

	private static void checkDuplicateTranslation(WoolTranslatable source,
			Set<String> context,
			Map<WoolTranslatable,List<WoolContextTranslation>> translations)
			throws ParseException {
		if (!translations.containsKey(source))
			return;
		List<WoolContextTranslation> sourceTrans = translations.get(source);
		for (WoolContextTranslation trans : sourceTrans) {
			if (trans.getContext().equals(context)) {
				throw new ParseException(String.format(
						"Found duplicate translation \"%s\" with context %s",
						source, context));
			}
		}
	}

	private static void parseContextMap(String key, Object value,
			Map<WoolTranslatable,List<WoolContextTranslation>> translations,
			WoolTranslationParserResult parseResult) {
		String contextListStr = key.trim();
		Set<String> context = new LinkedHashSet<>();
		if (!contextListStr.isEmpty()) {
			String[] contextList = contextListStr.split("\\s+");
			Collections.addAll(context, contextList);
		}
		Map<String,?> map;
		try {
			map = JsonMapper.convert(value,
					new TypeReference<Map<String, ?>>() {});
		} catch (ParseException ex) {
			parseResult.getParseErrors().add(new ParseException(
					"Failed to parse translation map after context key \"" +
					key + "\": " + ex.getMessage(), ex));
			return;
		}
		parse(context, map, translations, parseResult);
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
		List<WoolSourceTranslatable> sourceTranslatables =
				extractor.extractFromBody(null, null, body);
		List<WoolTranslatable> translatables = new ArrayList<>();
		for (WoolSourceTranslatable sourceTranslatable : sourceTranslatables) {
			translatables.add(sourceTranslatable.getTranslatable());
		}
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
