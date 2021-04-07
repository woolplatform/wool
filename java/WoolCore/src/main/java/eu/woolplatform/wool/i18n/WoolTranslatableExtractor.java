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
	public List<WoolSourceTranslatable> extractFromNode(WoolNode node) {
		return extractFromBody(node.getHeader().getSpeaker(),
				WoolSourceTranslatable.USER, node.getBody());
	}

	public List<WoolSourceTranslatable> extractFromBody(String speaker,
			String addressee, WoolNodeBody body) {
		List<WoolSourceTranslatable> result = new ArrayList<>();
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
					finishCurrentTranslatableSegment(speaker, addressee, body,
							current, result);
					result.addAll(getTranslatableSegmentsFromIfCommand(speaker,
							addressee, ifCmd));
				} else if (cmd instanceof WoolRandomCommand) {
					WoolRandomCommand rndCmd = (WoolRandomCommand)cmd;
					finishCurrentTranslatableSegment(speaker, addressee, body,
							current, result);
					result.addAll(getTranslatableSegmentsFromRandomCommand(
							speaker, addressee, rndCmd));
				} else if (cmd instanceof WoolInputCommand) {
					current.add(segment);
				}
			}
		}
		finishCurrentTranslatableSegment(speaker, addressee, body, current,
				result);
		for (WoolReply reply : body.getReplies()) {
			if (reply.getStatement() != null) {
				result.addAll(extractFromBody(addressee, speaker,
						reply.getStatement()));
			}
		}
		return result;
	}

	private List<WoolSourceTranslatable> getTranslatableSegmentsFromIfCommand(
			String speaker, String addressee, WoolIfCommand ifCmd) {
		List<WoolSourceTranslatable> result = new ArrayList<>();
		for (WoolIfCommand.Clause clause : ifCmd.getIfClauses()) {
			result.addAll(extractFromBody(speaker, addressee,
					clause.getStatement()));
		}
		if (ifCmd.getElseClause() != null) {
			result.addAll(extractFromBody(speaker, addressee,
					ifCmd.getElseClause()));
		}
		return result;
	}

	private List<WoolSourceTranslatable> getTranslatableSegmentsFromRandomCommand(
			String speaker, String addressee, WoolRandomCommand rndCmd) {
		List<WoolSourceTranslatable> result = new ArrayList<>();
		for (WoolRandomCommand.Clause clause : rndCmd.getClauses()) {
			result.addAll(extractFromBody(speaker, addressee,
					clause.getStatement()));
		}
		return result;
	}

	private void finishCurrentTranslatableSegment(String speaker,
			String addressee, WoolNodeBody parent,
			List<WoolNodeBody.Segment> current,
			List<WoolSourceTranslatable> translatables) {
		if (hasContent(current)) {
			List<WoolNodeBody.Segment> segments = new ArrayList<>(current);
			WoolSourceTranslatable translatable = new WoolSourceTranslatable(
					speaker, addressee, new WoolTranslatable(parent, segments));
			translatables.add(translatable);
		}
		current.clear();
	}

	private boolean hasContent(List<WoolNodeBody.Segment> segments) {
		for (WoolNodeBody.Segment segment : segments) {
			if (segment instanceof WoolNodeBody.TextSegment) {
				WoolNodeBody.TextSegment textSegment =
						(WoolNodeBody.TextSegment)segment;
				WoolVariableString string = textSegment.getText();
				if (hasContent(string))
					return true;
			} else if (segment instanceof WoolNodeBody.CommandSegment) {
				WoolNodeBody.CommandSegment cmdSegment =
						(WoolNodeBody.CommandSegment)segment;
				if (cmdSegment.getCommand() instanceof WoolInputCommand)
					return true;
			}
		}
		return false;
	}

	private boolean hasContent(WoolVariableString string) {
		return !string.getSegments().isEmpty() && !string.isWhitespace();
	}
}
