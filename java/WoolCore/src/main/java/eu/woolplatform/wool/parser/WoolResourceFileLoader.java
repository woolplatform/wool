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

package eu.woolplatform.wool.parser;

import com.fasterxml.jackson.core.type.TypeReference;
import eu.woolplatform.utils.exception.ParseException;
import eu.woolplatform.utils.io.FileUtils;
import eu.woolplatform.utils.json.JsonMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This WOOL file loader can load files from resources on the classpath. The
 * files should be organized as language/speaker/dialogue-name.wool or
 * language/speaker/dialogue-name.json. Example: en/robin/intro.wool</p>
 *
 * <p>The files should be specified in project file "dialogues.json" in the
 * root directory. This can automatically be generated at build time. It
 * should be structured like:<br />
 *<pre>{
 *    "en": {
 *        "robin":[
 *            "dialogue1.wool",
 *            "dialogue2.wool",
 *            "dialogue3.json"
 *        ]
 *    }
 *}</pre></p>
 *
 * @author Dennis Hofs (RRD)
 */
public class WoolResourceFileLoader implements WoolFileLoader {
	private static final String PROJECT_FILE = "dialogues.json";

	private String resourcePath;

	/**
	 * Constructs a new instance.
	 *
	 * @param resourcePath the resource path (without leading or trailing slash)
	 */
	public WoolResourceFileLoader(String resourcePath) {
		this.resourcePath = resourcePath;
	}

	@Override
	public List<WoolFileDescription> listWoolFiles() throws IOException {
		List<WoolFileDescription> result = new ArrayList<>();
		String path = resourcePath + "/" + PROJECT_FILE;
		InputStream input = getClass().getClassLoader().getResourceAsStream(
				path);
		try (Reader reader = new InputStreamReader(input,
				StandardCharsets.UTF_8)) {
			String json = FileUtils.readFileString(reader);
			Map<String, ?> map = JsonMapper.parse(json,
					new TypeReference<Map<String, ?>>() {});
			for (String language : map.keySet()) {
				parseLanguageValue(language, map.get(language), result);
			}
		} catch (ParseException ex) {
			throw new IOException("Failed to parse resource " + path + ": " +
					ex.getMessage(), ex);
		}
		return result;
	}

	private void parseLanguageValue(String language, Object value,
			List<WoolFileDescription> files) throws ParseException {
		Map<String,?> map = JsonMapper.convert(value,
				new TypeReference<Map<String,?>>() {});
		for (String speaker : map.keySet()) {
			parseSpeakerValue(language, speaker, map.get(speaker), files);
		}
	}

	private void parseSpeakerValue(String language, String speaker,
			Object value, List<WoolFileDescription> files) throws ParseException {
		List<String> list = JsonMapper.convert(value,
				new TypeReference<List<String>>() {});
		for (String filename : list) {
			WoolFileDescription descr = new WoolFileDescription(speaker,
					language, filename);
			if (filename.endsWith(".wool") || filename.endsWith(".json"))
				files.add(descr);
		}
	}

	@Override
	public Reader openFile(WoolFileDescription descr) throws IOException {
		String path = resourcePath + "/" + descr.getLanguage() + "/" +
				descr.getMainSpeaker() + "/" + descr.getFileName();
		return new InputStreamReader(getClass().getClassLoader()
				.getResourceAsStream(path), StandardCharsets.UTF_8);
	}
}
