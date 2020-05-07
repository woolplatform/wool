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

import eu.woolplatform.wool.model.WoolDialogue;
import eu.woolplatform.wool.model.WoolNode;
import eu.woolplatform.wool.model.WoolNodeBody;
import eu.woolplatform.wool.model.WoolVariableString;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class can translate {@link WoolNode WoolNode}s given a translation map.
 * The translation map can be obtained from a translation file using the {@link
 * WoolTranslationParser WoolTranslationParser}.
 *
 * @author Dennis Hofs (RRD)
 */
public class WoolTranslator {
	private Map<String,WoolTranslatable> translations;
	private Pattern preWhitespaceRegex;
	private Pattern postWhitespaceRegex;

	/**
	 * Constructs a new translator.
	 *
	 * @param translations the translation map
	 */
	public WoolTranslator(Map<WoolTranslatable,WoolTranslatable> translations) {
		this.translations = new LinkedHashMap<>();
		for (WoolTranslatable key : translations.keySet()) {
			this.translations.put(key.toString().trim(), translations.get(key));
		}
		preWhitespaceRegex = Pattern.compile("^\\s+");
		postWhitespaceRegex = Pattern.compile("\\s+$");
	}

	/**
	 * Translates the specified dialogue. This method creates a clone of the
	 * dialogue and then tries to fill in a translation for every translatable
	 * segment (plain text, variables and &lt;&lt;input&gt;&gt; commands).
	 *
	 * @param dialogue the dialogue
	 * @return the translated dialogue
	 */
	public WoolDialogue translate(WoolDialogue dialogue) {
		dialogue = new WoolDialogue(dialogue);
		for (WoolNode node : dialogue.getNodes()) {
			translateBody(node.getBody());
		}
		return dialogue;
	}

	/**
	 * Translates the specified node. This method creates a clone of the node
	 * and then tries to fill in a translation for every translatable segment
	 * (plain text, variables and &lt;&lt;input&gt;&gt; commands).
	 *
	 * @param node the node
	 * @return the translated node
	 */
	public WoolNode translate(WoolNode node) {
		node = new WoolNode(node);
		translateBody(node.getBody());
		return node;
	}

	private void translateBody(WoolNodeBody body) {
		WoolTranslatableExtractor extractor = new WoolTranslatableExtractor();
		List<WoolTranslatable> translatables = extractor.extractFromBody(body);
		for (WoolTranslatable translatable : translatables) {
			translateText(translatable);
		}
	}

	private void translateText(WoolTranslatable text) {
		String textPlain = text.toString();
		String preWhitespace = "";
		String postWhitespace = "";
		Matcher m = preWhitespaceRegex.matcher(textPlain);
		if (m.find())
			preWhitespace = m.group();
		m = postWhitespaceRegex.matcher(textPlain);
		if (m.find())
			postWhitespace = m.group();
		WoolTranslatable translation = translations.get(text.toString().trim());
		if (translation == null)
			return;
		WoolNodeBody body = text.getParent();
		List<WoolNodeBody.Segment> bodySegments = new ArrayList<>(
				body.getSegments());
		List<WoolNodeBody.Segment> textSegments = text.getSegments();
		int insertIndex = body.getSegments().indexOf(textSegments.get(0));
		for (WoolNodeBody.Segment segment : textSegments) {
			bodySegments.remove(segment);
		}
		if (preWhitespace.length() > 0) {
			bodySegments.add(insertIndex++, new WoolNodeBody.TextSegment(
					new WoolVariableString(preWhitespace)));
		}
		List<WoolNodeBody.Segment> transSegments = translation.getSegments();
		for (WoolNodeBody.Segment transSegment : transSegments) {
			bodySegments.add(insertIndex++, transSegment);
		}
		if (postWhitespace.length() > 0) {
			bodySegments.add(insertIndex + 1, new WoolNodeBody.TextSegment(
					new WoolVariableString(postWhitespace)));
		}
		body.clearSegments();
		for (WoolNodeBody.Segment segment : bodySegments) {
			body.addSegment(segment);
		}
	}
}
