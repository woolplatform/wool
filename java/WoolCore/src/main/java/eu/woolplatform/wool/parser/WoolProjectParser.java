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

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import eu.woolplatform.utils.exception.ParseException;
import eu.woolplatform.utils.i18n.I18nLanguageFinder;
import eu.woolplatform.wool.i18n.WoolContextTranslation;
import eu.woolplatform.wool.i18n.WoolTranslatable;
import eu.woolplatform.wool.i18n.WoolTranslationContext;
import eu.woolplatform.wool.i18n.WoolTranslationParser;
import eu.woolplatform.wool.i18n.WoolTranslationParserResult;
import eu.woolplatform.wool.i18n.WoolTranslator;
import eu.woolplatform.wool.model.WoolDialogue;
import eu.woolplatform.wool.model.WoolDialogueDescription;
import eu.woolplatform.wool.model.WoolProject;

/**
 * This class can read an entire WOOL project consisting of ".wool" dialogue
 * files and ".json" translation files.
 *
 * @author Dennis Hofs (RRD)
 */
public class WoolProjectParser {
	private WoolFileLoader fileLoader;

	private Map<WoolFileDescription, WoolDialogue> dialogues =
			new LinkedHashMap<>();
	private Map<WoolFileDescription,Map<WoolTranslatable,List<WoolContextTranslation>>> translations =
			new LinkedHashMap<>();

	private Map<WoolDialogueDescription,WoolDialogue> translatedDialogues =
			new LinkedHashMap<>();

	public WoolProjectParser(WoolFileLoader fileLoader) {
		this.fileLoader = fileLoader;
	}

	public WoolProjectParserResult parse() throws IOException {
		WoolProjectParserResult result = new WoolProjectParserResult();
		List<WoolFileDescription> files = fileLoader.listWoolFiles();
		parseFiles(files, result);
		if (!result.getParseErrors().isEmpty())
			return result;
		createTranslatedDialogues(result);
		if (!result.getParseErrors().isEmpty())
			return result;
		WoolProject project = new WoolProject();
		project.setDialogues(translatedDialogues);
		Map<WoolDialogueDescription,WoolDialogue> sourceDialogues =
				new LinkedHashMap<>();
		for (WoolFileDescription descr : dialogues.keySet()) {
			sourceDialogues.put(fileDescriptionToDialogueDescription(descr),
					dialogues.get(descr));
		}
		project.setSourceDialogues(sourceDialogues);
		Map<WoolDialogueDescription,Map<WoolTranslatable,List<WoolContextTranslation>>> dlgTranslations =
				new LinkedHashMap<>();
		for (WoolFileDescription descr : translations.keySet()) {
			dlgTranslations.put(fileDescriptionToDialogueDescription(descr),
					translations.get(descr));
		}
		project.setTranslations(dlgTranslations);
		result.setProject(project);
		return result;
	}

	/**
	 * Tries to parse all project files (dialogue and translation files). This
	 * method fills variables "dialogues" and "translations". Any parse errors
	 * will be added to "readResult".
	 *
	 * <p>It uses "dialogueFiles" and "translationFiles". They will be cleared
	 * in the end.</p>
	 *
	 * @param files the project files
	 * @param readResult the read result
	 * @throws IOException if a reading error occurs
	 */
	private void parseFiles(List<WoolFileDescription> files,
			WoolProjectParserResult readResult) throws IOException {
		Set<WoolDialogueDescription> dlgDescrSet = new HashSet<>();
		List<WoolFileDescription> dialogueFiles = new ArrayList<>();
		List<WoolFileDescription> translationFiles = new ArrayList<>();
		for (WoolFileDescription file : files) {
			if (file.getFilePath().endsWith(".wool"))
				dialogueFiles.add(file);
			else if (file.getFilePath().endsWith(".json"))
				translationFiles.add(file);
		}
		Set<String> dialogueNames = new HashSet<>();
		for (WoolFileDescription descr : dialogueFiles) {
			dlgDescrSet.add(fileDescriptionToDialogueDescription(descr));
			WoolParserResult dlgReadResult = parseDialogueFile(descr);
			if (dlgReadResult.getParseErrors().isEmpty()) {
				dialogues.put(descr, dlgReadResult.getDialogue());
				dialogueNames.add(dlgReadResult.getDialogue()
						.getDialogueName());
			} else {
				getParseErrors(readResult, descr).addAll(
						dlgReadResult.getParseErrors());
			}
		}
		if (readResult.getParseErrors().isEmpty()) {
			// validate referenced dialogues in external node pointers
			for (WoolFileDescription descr : dialogues.keySet()) {
				WoolDialogue dlg = dialogues.get(descr);
				for (String refName : dlg.getDialoguesReferenced()) {
					if (!dialogueNames.contains(refName)) {
						getParseErrors(readResult, descr).add(
								new ParseException(String.format(
								"Found external node pointer in dialogue %s to unknown dialogue %s",
								dlg.getDialogueName(), refName)));
					}
				}
			}
		}
		for (WoolFileDescription descr : translationFiles) {
			WoolDialogueDescription dlgDescr =
					fileDescriptionToDialogueDescription(descr);
			if (dlgDescrSet.contains(dlgDescr)) {
				getParseErrors(readResult, descr).add(new ParseException(
						String.format("Found both translation file \"%s\" and dialogue file \"%s.wool\"",
						descr.getFilePath(), dlgDescr.getDialogueName()) + ": " +
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
			WoolProjectParserResult readResult, WoolFileDescription descr) {
		String path = fileDescriptionToPath(descr);
		List<ParseException> errors = readResult.getParseErrors().get(path);
		if (errors != null)
			return errors;
		errors = new ArrayList<>();
		readResult.getParseErrors().put(path, errors);
		return errors;
	}

	private List<String> getWarnings(WoolProjectParserResult readResult,
			WoolFileDescription descr) {
		String path = fileDescriptionToPath(descr);
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
		for (WoolFileDescription descr : dialogues.keySet()) {
			WoolDialogueDescription dlgDescr =
					fileDescriptionToDialogueDescription(descr);
			WoolDialogue dlg = dialogues.get(descr);
			translatedDialogues.put(dlgDescr, dlg);
		}
		for (WoolFileDescription descr : translations.keySet()) {
			WoolDialogueDescription dlgDescr =
					fileDescriptionToDialogueDescription(descr);
			WoolDialogue source = findSourceDialogue(
					dlgDescr.getDialogueName());
			if (source == null) {
				getParseErrors(readResult, descr).add(new ParseException(
						"No source dialogue found for translation: " +
						descr));
				continue;
			}
			WoolTranslator translator = new WoolTranslator(
					new WoolTranslationContext(), translations.get(descr));
			WoolDialogue translated = translator.translate(source);
			translatedDialogues.put(dlgDescr, translated);
		}
	}

	private WoolDialogue findSourceDialogue(String dlgName) {
		List<WoolFileDescription> matches = new ArrayList<>();
		for (WoolFileDescription descr : dialogues.keySet()) {
			String currDlgName = fileNameToDialogueName(descr.getFilePath());
			if (currDlgName.equals(dlgName))
				matches.add(descr);
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

	private WoolParserResult parseDialogueFile(WoolFileDescription description)
			throws IOException {
		String dlgName = fileNameToDialogueName(description.getFilePath());
		try (WoolParser woolParser = new WoolParser(dlgName,
				fileLoader.openFile(description))) {
			return woolParser.readDialogue();
		}
	}

	private WoolTranslationParserResult parseTranslationFile(
			WoolFileDescription description) throws IOException {
		try (Reader reader = fileLoader.openFile(description)) {
			return WoolTranslationParser.parse(reader);
		}
	}

	private WoolDialogueDescription fileDescriptionToDialogueDescription(
			WoolFileDescription descr) {
		WoolDialogueDescription result = new WoolDialogueDescription();
		result.setLanguage(descr.getLanguage());
		result.setDialogueName(fileNameToDialogueName(descr.getFilePath()));
		return result;
	}

	private String fileNameToDialogueName(String fileName) {
		if (fileName.endsWith(".wool") || fileName.endsWith(".json"))
			return fileName.substring(0, fileName.length() - 5);
		else
			return fileName;
	}

	private String fileDescriptionToPath(WoolFileDescription descr) {
		return descr.getLanguage() + "/" + descr.getFilePath();
	}

	private static void showUsage() {
		System.out.println("Usage:");
		System.out.println("java " + WoolProjectParser.class.getName() + " [options] <projectdir>");
		System.out.println("    Parse WOOL project directory and print a summary of each dialogue");
		System.out.println("");
		System.out.println("Options:");
		System.out.println("-h -? --help");
		System.out.println("    Print this usage message");
	}

	public static void main(String[] args) {
		String dirname = null;
		int i = 0;
		while (i < args.length) {
			String arg = args[i++];
			if (arg.equals("-h") || arg.equals("-?") || arg.equals("--help")) {
				showUsage();
				return;
			} else {
				dirname = arg;
			}
		}
		if (dirname == null) {
			showUsage();
			System.exit(1);
			return;
		}
		File dir = new File(dirname);
		if (!dir.exists()) {
			System.err.println("ERROR: Directory not found: " + dirname);
			System.exit(1);
			return;
		}
		try {
			dir = dir.getCanonicalFile();
		} catch (IOException ex) {}
		if (!dir.isDirectory()) {
			System.err.println("ERROR: Path is not a directory: " + dir.getAbsolutePath());
			System.exit(1);
			return;
		}
		WoolProjectParserResult readResult;
		try {
			WoolFileLoader fileLoader = new WoolDirectoryFileLoader(dir);
			WoolProjectParser parser = new WoolProjectParser(fileLoader);
			readResult = parser.parse();
		} catch (IOException ex) {
			System.err.println(
					"ERROR: Can't read WOOL project from directory: " +
					dir.getAbsolutePath() + ": " + ex.getMessage());
			System.exit(1);
			return;
		}
		if (!readResult.getParseErrors().isEmpty()) {
			for (String key : readResult.getParseErrors().keySet()) {
				System.err.println("ERROR: Failed to parse file: " + key);
				List<ParseException> errors = readResult.getParseErrors().get(
						key);
				for (ParseException ex : errors) {
					System.err.println(ex.getMessage());
				}
			}
			System.exit(1);
			return;
		}
		for (String key : readResult.getWarnings().keySet()) {
			System.err.println("WARNING: " + key);
			List<String> errors = readResult.getWarnings().get(key);
			for (String error : errors) {
				System.err.println(error);
			}
		}
		System.out.println("Finished parsing WOOL project from directory: " +
				dir.getAbsolutePath());
		Map<WoolDialogueDescription,WoolDialogue> dialogues =
				readResult.getProject().getDialogues();
		for (WoolDialogueDescription dialogue : dialogues.keySet()) {
			System.out.println("----------");
			System.out.println("DIALOGUE " + dialogue.getDialogueName() +
					" (" + dialogue.getLanguage() + ")");
			System.out.println(dialogues.get(dialogue));
		}
	}
}
