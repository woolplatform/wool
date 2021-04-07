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

package eu.woolplatform.wool.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import eu.woolplatform.utils.i18n.I18nLanguageFinder;
import eu.woolplatform.wool.i18n.WoolContextTranslation;
import eu.woolplatform.wool.i18n.WoolTranslatable;
import eu.woolplatform.wool.i18n.WoolTranslationContext;
import eu.woolplatform.wool.i18n.WoolTranslator;

public class WoolProject {
	private Map<WoolDialogueDescription,WoolDialogue> dialogues =
			new LinkedHashMap<>();
	private Map<WoolDialogueDescription,WoolDialogue> sourceDialogues =
			new LinkedHashMap<>();
	private Map<WoolDialogueDescription,Map<WoolTranslatable,List<WoolContextTranslation>>> translations =
			new LinkedHashMap<>();

	public Map<WoolDialogueDescription, WoolDialogue> getDialogues() {
		return dialogues;
	}

	public void setDialogues(
			Map<WoolDialogueDescription,WoolDialogue> dialogues) {
		this.dialogues = dialogues;
	}

	public Map<WoolDialogueDescription, WoolDialogue> getSourceDialogues() {
		return sourceDialogues;
	}

	public void setSourceDialogues(
			Map<WoolDialogueDescription,WoolDialogue> sourceDialogues) {
		this.sourceDialogues = sourceDialogues;
	}

	public Map<WoolDialogueDescription,Map<WoolTranslatable,List<WoolContextTranslation>>> getTranslations() {
		return translations;
	}

	public void setTranslations(Map<WoolDialogueDescription,Map<WoolTranslatable,List<WoolContextTranslation>>> translations) {
		this.translations = translations;
	}

	public WoolDialogue getTranslatedDialogue(WoolDialogueDescription descr,
			WoolTranslationContext context) {
		WoolDialogue dialogue = sourceDialogues.get(descr);
		if (dialogue != null)
			return dialogue;
		Map<WoolTranslatable,List<WoolContextTranslation>> translations =
				this.translations.get(descr);
		if (translations == null)
			return null;
		dialogue = findSourceDialogue(descr.getDialogueName());
		if (dialogue == null)
			return null;
		WoolTranslator translator = new WoolTranslator(context, translations);
		return translator.translate(dialogue);
	}

	private WoolDialogue findSourceDialogue(String dlgName) {
		List<WoolDialogueDescription> matches = new ArrayList<>();
		for (WoolDialogueDescription descr : sourceDialogues.keySet()) {
			if (descr.getDialogueName().equals(dlgName))
				matches.add(descr);
		}
		if (matches.isEmpty())
			return null;
		if (matches.size() == 1)
			return dialogues.get(matches.get(0));
		Map<String,WoolDialogueDescription> lngMap = new HashMap<>();
		for (WoolDialogueDescription match : matches) {
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
}
