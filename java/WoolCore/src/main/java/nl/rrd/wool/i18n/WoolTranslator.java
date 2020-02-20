package nl.rrd.wool.i18n;

import nl.rrd.wool.model.WoolNode;
import nl.rrd.wool.model.WoolNodeBody;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class can translate {@link WoolNode WoolNode}s given a translation map.
 * The translation map can be obtained from a translation file using the {@link
 * WoolTranslationParser WoolTranslationParser}.
 *
 * @author Dennis Hofs (RRD)
 */
public class WoolTranslator {
	private Map<WoolTranslatable,WoolTranslatable> translations;

	/**
	 * Constructs a new translator.
	 *
	 * @param translations the translation map
	 */
	public WoolTranslator(Map<WoolTranslatable,WoolTranslatable> translations) {
		this.translations = translations;
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
		WoolTranslatable translation = translations.get(text);
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
		List<WoolNodeBody.Segment> transSegments = translation.getSegments();
		for (int i = 0; i < transSegments.size(); i++) {
			bodySegments.add(insertIndex + i, transSegments.get(i));
		}
		body.clearSegments();
		for (WoolNodeBody.Segment segment : bodySegments) {
			body.addSegment(segment);
		}
	}
}
