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

package nl.rrd.wool.parser;

import com.fasterxml.jackson.core.type.TypeReference;
import nl.rrd.wool.exception.ParseException;
import nl.rrd.wool.i18n.*;
import nl.rrd.wool.json.JsonMapper;
import nl.rrd.wool.model.WoolDialogue;
import nl.rrd.wool.model.WoolDialogueDescription;
import nl.rrd.wool.model.WoolProject;
import nl.rrd.wool.utils.FileUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * This class can read an entire WOOL project consisting of ".wool" dialogue
 * files and ".json" translation files. The files should be in the resources
 * on the classpath. At construction you can specify the root path.
 *
 * <p>The files should be organized as language/speaker/dialogue-name.wool or
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
 */
public class WoolProjectParser {
	private static final String PROJECT_FILE = "dialogues.json";

	private String resourcePath;

	private List<WoolFileDescription> dialogueFiles = new ArrayList<>();
	private List<WoolFileDescription> translationFiles = new ArrayList<>();

	private Map<WoolFileDescription, WoolDialogue> dialogues =
			new LinkedHashMap<>();
	private Map<WoolFileDescription,Map<WoolTranslatable,WoolTranslatable>> translations =
			new LinkedHashMap<>();

	private Map<WoolDialogueDescription, WoolDialogue> translatedDialogues =
			new LinkedHashMap<>();

	/**
	 * Constructs a new parser.
	 *
	 * @param resourcePath the resource path (without leading or trailing slash)
	 */
	public WoolProjectParser(String resourcePath) {
		this.resourcePath = resourcePath;
	}

	public WoolProjectParserResult parse() throws IOException {
		WoolProjectParserResult result = new WoolProjectParserResult();
		parseFiles(result);
		if (!result.getParseErrors().isEmpty())
			return result;
		createTranslatedDialogues(result);
		if (!result.getParseErrors().isEmpty())
			return result;
		WoolProject project = new WoolProject();
		project.setDialogues(translatedDialogues);
		result.setProject(project);
		return result;
	}

	/**
	 * Tries to parse the project file and all dialogue and translation files.
	 * This method fills variables "dialogues" and "translations". Any parse
	 * errors will be added to "readResult".
	 *
	 * <p>It uses "dialogueFiles" and "translationFiles". They will be cleared
	 * in the end.</p>
	 *
	 * @param readResult the read result
	 * @throws IOException if a reading error occurs
	 */
	private void parseFiles(WoolProjectParserResult readResult)
			throws IOException {
		try {
			doParseFiles(readResult);
		} finally {
			dialogueFiles.clear();
			translationFiles.clear();
		}
	}

	private void doParseFiles(WoolProjectParserResult readResult) throws IOException {
		try {
			parseProjectFile();
		} catch (ParseException ex) {
			getParseErrors(readResult, resourcePath + "/" + PROJECT_FILE)
					.add(ex);
			return;
		}
		Set<WoolDialogueDescription> dlgDescrSet = new HashSet<>();
		for (WoolFileDescription descr : dialogueFiles) {
			dlgDescrSet.add(fileDescriptionToDialogueDescription(descr));
			WoolParserResult dlgReadResult = parseDialogueFile(descr);
			if (dlgReadResult.getParseErrors().isEmpty()) {
				dialogues.put(descr, dlgReadResult.getDialogue());
			} else {
				getParseErrors(readResult, descr).addAll(
						dlgReadResult.getParseErrors());
			}
		}
		for (WoolFileDescription descr : translationFiles) {
			WoolDialogueDescription dlgDescr =
					fileDescriptionToDialogueDescription(descr);
			if (dlgDescrSet.contains(dlgDescr)) {
				getParseErrors(readResult, descr).add(new ParseException(
						String.format("Found both translation file \"%s\" and dialogue file \"%s.wool\"",
						descr.getFileName(), dlgDescr.getFileName()) + ": " +
						descr));
				continue;
			}
			WoolTranslationParserResult transParseResult = parseTranslationFile(
					descr);
			if (!transParseResult.getParseErrors().isEmpty()) {
				getParseErrors(readResult, descr).addAll(
						transParseResult.getParseErrors());
			}
			if (!transParseResult.getWarnings().isEmpty()) {
				getWarnings(readResult, descr).addAll(
						transParseResult.getWarnings());
			}
			if (transParseResult.getParseErrors().isEmpty())
				translations.put(descr, transParseResult.getTranslations());
		}
	}

	private List<ParseException> getParseErrors(
			WoolProjectParserResult readResult, String resourcePath) {
		List<ParseException> errors = readResult.getParseErrors().get(
				resourcePath);
		if (errors != null)
			return errors;
		errors = new ArrayList<>();
		readResult.getParseErrors().put(resourcePath, errors);
		return errors;
	}

	private List<ParseException> getParseErrors(
			WoolProjectParserResult readResult, WoolFileDescription descr) {
		String path = fileDescriptionToResourcePath(descr);
		List<ParseException> errors = readResult.getParseErrors().get(path);
		if (errors != null)
			return errors;
		errors = new ArrayList<>();
		readResult.getParseErrors().put(path, errors);
		return errors;
	}

	private List<String> getWarnings(WoolProjectParserResult readResult,
			WoolFileDescription descr) {
		String path = fileDescriptionToResourcePath(descr);
		List<String> warnings = readResult.getWarnings().get(path);
		if (warnings != null)
			return warnings;
		warnings = new ArrayList<>();
		readResult.getWarnings().put(path, warnings);
		return warnings;
	}

	/**
	 * Tries to create translated dialogues for all translation files. This
	 * method fills variable "translatedDialogues" with the dialogues from
	 * "dialogues" plus translated dialogues from "translations". Any parse
	 * errors will be added to "readResult".
	 *
	 * <p>It uses "dialogues" and "translations". They will be cleared in the
	 * end.</p>
	 *
	 * @param readResult the read result
	 */
	private void createTranslatedDialogues(WoolProjectParserResult readResult) {
		try {
			doCreateTranslatedDialogues(readResult);
		} finally {
			dialogues.clear();
			translations.clear();
		}
	}

	private void doCreateTranslatedDialogues(WoolProjectParserResult readResult) {
		for (WoolFileDescription descr : dialogues.keySet()) {
			WoolDialogueDescription dlgDescr =
					fileDescriptionToDialogueDescription(descr);
			WoolDialogue dlg = dialogues.get(descr);
			translatedDialogues.put(dlgDescr, dlg);
		}
		for (WoolFileDescription descr : translations.keySet()) {
			WoolDialogueDescription dlgDescr =
					fileDescriptionToDialogueDescription(descr);
			WoolDialogue source = findSourceDialogue(dlgDescr.getMainSpeaker(),
					dlgDescr.getFileName());
			if (source == null) {
				getParseErrors(readResult, descr).add(new ParseException(
						"No source dialogue found for translation: " +
						descr));
				continue;
			}
			WoolTranslator translator = new WoolTranslator(translations.get(
					descr));
			WoolDialogue translated = translator.translate(source);
			translatedDialogues.put(dlgDescr, translated);
		}
	}

	private WoolDialogue findSourceDialogue(String mainSpeaker, String dlgName) {
		List<WoolFileDescription> matches = new ArrayList<>();
		for (WoolFileDescription descr : dialogues.keySet()) {
			String currDlgName = fileNameToDialogueName(descr.getFileName());
			if (descr.getMainSpeaker().equals(mainSpeaker) &&
					currDlgName.equals(dlgName)) {
				matches.add(descr);
			}
		}
		if (matches.isEmpty())
			return null;
		if (matches.size() == 1)
			return dialogues.get(matches.get(0));
		Map<String,WoolFileDescription> lngMap = new HashMap<>();
		for (WoolFileDescription match : matches) {
			lngMap.put(match.getLanguage(), match);
		}
		I18nLanguageFinder finder = new I18nLanguageFinder(new ArrayList<>(
				lngMap.keySet()));
		finder.setUserLocale(Locale.ENGLISH);
		String language = finder.find();
		if (language == null)
			return dialogues.get(matches.get(0));
		else
			return dialogues.get(lngMap.get(language));
	}

	/**
	 * Parses the project file and fills variables "dialogueFiles" and
	 * "translationFiles".
	 *
	 * @throws ParseException if a parse error occurs
	 * @throws IOException if a reading error occurs
	 */
	private void parseProjectFile() throws ParseException, IOException {
		InputStream input = getClass().getClassLoader().getResourceAsStream(
				resourcePath + "/" + PROJECT_FILE);
		try (Reader reader = new InputStreamReader(input,
				StandardCharsets.UTF_8)) {
			String json = FileUtils.readFileString(reader);
			Map<String, ?> map = JsonMapper.parse(json,
					new TypeReference<Map<String, ?>>() {});
			for (String language : map.keySet()) {
				parseLanguageValue(language, map.get(language));
			}
		}
	}

	private void parseLanguageValue(String language, Object value) throws ParseException {
		Map<String, ?> map = JsonMapper.convert(value,
				new TypeReference<Map<String, ?>>() {});
		for (String speaker : map.keySet()) {
			parseSpeakerValue(language, speaker, map.get(speaker));
		}
	}

	private void parseSpeakerValue(String language, String speaker,
			Object value) throws ParseException {
		List<String> list = JsonMapper.convert(value,
				new TypeReference<List<String>>() {});
		for (String filename : list) {
			WoolFileDescription descr = new WoolFileDescription(speaker,
					language, filename);
			if (filename.endsWith(".wool"))
				dialogueFiles.add(descr);
			else if (filename.endsWith(".json"))
				translationFiles.add(descr);
		}
	}

	private WoolParserResult parseDialogueFile(WoolFileDescription description)
			throws IOException {
		String dlgName = fileNameToDialogueName(description.getFileName());
		String resourceName = fileDescriptionToResourcePath(description);
		InputStream input = getClass().getClassLoader().getResourceAsStream(
				resourceName);
		try (WoolParser woolParser = new WoolParser(dlgName, input)) {
			return woolParser.readDialogue();
		}
	}

	private WoolTranslationParserResult parseTranslationFile(
			WoolFileDescription description) throws IOException {
		String resourceName = fileDescriptionToResourcePath(description);
		InputStream input = getClass().getClassLoader().getResourceAsStream(
				resourceName);
		try (Reader reader = new InputStreamReader(input,
				StandardCharsets.UTF_8)) {
			return WoolTranslationParser.parse(reader);
		}
	}

	private String fileDescriptionToResourcePath(WoolFileDescription descr) {
		return resourcePath + "/" + descr.getLanguage() + "/" +
				descr.getMainSpeaker() + "/" + descr.getFileName();
	}

	private WoolDialogueDescription fileDescriptionToDialogueDescription(
			WoolFileDescription descr) {
		WoolDialogueDescription result = new WoolDialogueDescription();
		result.setMainSpeaker(descr.getMainSpeaker());
		result.setLanguage(descr.getLanguage());
		result.setFileName(fileNameToDialogueName(descr.getFileName()));
		return result;
	}

	private String fileNameToDialogueName(String fileName) {
		if (fileName.endsWith(".wool") || fileName.endsWith(".json"))
			return fileName.substring(0, fileName.length() - 5);
		else
			return fileName;
	}
}
