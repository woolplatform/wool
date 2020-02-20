package nl.rrd.wool.i18n;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.rrd.wool.exception.LineNumberParseException;
import nl.rrd.wool.exception.ParseException;
import nl.rrd.wool.model.WoolNodeBody;
import nl.rrd.wool.parser.WoolBodyParser;
import nl.rrd.wool.parser.WoolBodyToken;
import nl.rrd.wool.parser.WoolBodyTokenizer;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This class can parse a WOOL translation file. The file should contain a
 * JSON object with key-value pairs, where the keys are translatables in one
 * language, and the values are translatables in another language. A
 * translatable should be the string representation of a {@link WoolTranslatable
 * WoolTranslatable}. That is a text that may include variables and
 * &lt;&lt;input&gt;&gt; commands.
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
		ObjectMapper mapper = new ObjectMapper();
		Map<String,String> map = mapper.readValue(reader,
				new TypeReference<Map<String,String>>() {});
		Map<WoolTranslatable, WoolTranslatable> translations =
				new LinkedHashMap<>();
		for (String key : map.keySet()) {
			WoolTranslatable transKey = parseTranslationString(key);
			String value = map.get(key);
			WoolTranslatable transValue = parseTranslationString(value);
			if (translations.containsKey(transKey)) {
				throw new ParseException("Found duplicate translation key: " +
						transKey);
			}
			translations.put(transKey, transValue);
		}
		return translations;
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
