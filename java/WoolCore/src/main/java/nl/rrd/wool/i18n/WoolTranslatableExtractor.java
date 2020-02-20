package nl.rrd.wool.i18n;

import nl.rrd.wool.model.WoolNode;
import nl.rrd.wool.model.WoolNodeBody;
import nl.rrd.wool.model.WoolReply;
import nl.rrd.wool.model.WoolVariableString;
import nl.rrd.wool.model.command.WoolCommand;
import nl.rrd.wool.model.command.WoolIfCommand;
import nl.rrd.wool.model.command.WoolInputCommand;

import java.util.ArrayList;
import java.util.List;

/**
 * This class can extract all translatable segments (plain text, variables and
 * &lt;&lt;input&gt;&gt; commands) from a {@link WoolNode WoolNode} or {@link
 * WoolNodeBody WoolNodeBody}. This includes translatables within if commands
 * and replies.
 *
 * @author Dennis Hofs (RRD)
 */
public class WoolTranslatableExtractor {
	public List<WoolTranslatable> extractFromNode(WoolNode node) {
		return extractFromBody(node.getBody());
	}

	public List<WoolTranslatable> extractFromBody(WoolNodeBody body) {
		List<WoolTranslatable> result = new ArrayList<>();
		List<WoolNodeBody.Segment> current = new ArrayList<>();
		for (int i = 0; i < body.getSegments().size(); i++) {
			WoolNodeBody.Segment segment = body.getSegments().get(i);
			if (segment instanceof WoolNodeBody.TextSegment) {
				WoolNodeBody.TextSegment textSegment =
						(WoolNodeBody.TextSegment)segment;
				current.add(textSegment);
			} else if (segment instanceof WoolNodeBody.CommandSegment) {
				WoolNodeBody.CommandSegment cmdSegment =
						(WoolNodeBody.CommandSegment)segment;
				WoolCommand cmd = cmdSegment.getCommand();
				if (cmd instanceof WoolIfCommand) {
					WoolIfCommand ifCmd = (WoolIfCommand)cmd;
					finishCurrentTranslatableSegment(body, current, result);
					result.addAll(getTranslatableSegmentsFromIfCommand(ifCmd));
				} else if (cmd instanceof WoolInputCommand) {
					current.add(segment);
				}
			}
		}
		finishCurrentTranslatableSegment(body, current, result);
		for (WoolReply reply : body.getReplies()) {
			result.addAll(extractFromBody(reply.getStatement()));
		}
		return result;
	}

	private List<WoolTranslatable> getTranslatableSegmentsFromIfCommand(
			WoolIfCommand ifCmd) {
		List<WoolTranslatable> result = new ArrayList<>();
		for (WoolIfCommand.Clause clause : ifCmd.getIfClauses()) {
			result.addAll(extractFromBody(clause.getStatement()));
		}
		if (ifCmd.getElseClause() != null)
			result.addAll(extractFromBody(ifCmd.getElseClause()));
		return result;
	}

	private void finishCurrentTranslatableSegment(WoolNodeBody parent,
			List<WoolNodeBody.Segment> current,
			List<WoolTranslatable> translatables) {
		if (containsText(current)) {
			translatables.add(new WoolTranslatable(parent,
					new ArrayList<>(current)));
		}
		current.clear();
	}

	private boolean containsText(List<WoolNodeBody.Segment> segments) {
		for (WoolNodeBody.Segment segment : segments) {
			if (segment instanceof WoolNodeBody.TextSegment) {
				WoolNodeBody.TextSegment textSegment =
						(WoolNodeBody.TextSegment)segment;
				WoolVariableString string = textSegment.getText();
				if (containsText(string))
					return true;
			}
		}
		return false;
	}

	private boolean containsText(WoolVariableString string) {
		for (WoolVariableString.Segment segment : string.getSegments()) {
			if (segment instanceof WoolVariableString.TextSegment)
				return true;
		}
		return false;
	}
}
