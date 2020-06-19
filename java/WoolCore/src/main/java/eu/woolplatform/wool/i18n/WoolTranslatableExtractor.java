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

import eu.woolplatform.wool.model.WoolNode;
import eu.woolplatform.wool.model.WoolNodeBody;
import eu.woolplatform.wool.model.WoolReply;
import eu.woolplatform.wool.model.WoolVariableString;
import eu.woolplatform.wool.model.command.WoolCommand;
import eu.woolplatform.wool.model.command.WoolIfCommand;
import eu.woolplatform.wool.model.command.WoolInputCommand;
import eu.woolplatform.wool.model.command.WoolRandomCommand;

import java.util.ArrayList;
import java.util.List;

/**
 * This class can extract all translatable segments (plain text, variables and
 * &lt;&lt;input&gt;&gt; commands) from a {@link WoolNode WoolNode} or {@link
 * WoolNodeBody WoolNodeBody}. This includes translatables within "if" and
 * "random" commands and replies.
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
				} else if (cmd instanceof WoolRandomCommand) {
					WoolRandomCommand rndCmd = (WoolRandomCommand)cmd;
					finishCurrentTranslatableSegment(body, current, result);
					result.addAll(getTranslatableSegmentsFromRandomCommand(
							rndCmd));
				} else if (cmd instanceof WoolInputCommand) {
					current.add(segment);
				}
			}
		}
		finishCurrentTranslatableSegment(body, current, result);
		for (WoolReply reply : body.getReplies()) {
			if (reply.getStatement() != null)
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

	private List<WoolTranslatable> getTranslatableSegmentsFromRandomCommand(
			WoolRandomCommand rndCmd) {
		List<WoolTranslatable> result = new ArrayList<>();
		for (WoolRandomCommand.Clause clause : rndCmd.getClauses()) {
			result.addAll(extractFromBody(clause.getStatement()));
		}
		return result;
	}

	private void finishCurrentTranslatableSegment(WoolNodeBody parent,
			List<WoolNodeBody.Segment> current,
			List<WoolTranslatable> translatables) {
		if (containsText(current)) {
			List<WoolNodeBody.Segment> segments = new ArrayList<>(current);
			translatables.add(new WoolTranslatable(parent, segments));
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
