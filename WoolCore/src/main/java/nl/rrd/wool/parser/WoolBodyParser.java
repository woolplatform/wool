package nl.rrd.wool.parser;

import java.util.List;

import nl.rrd.wool.exception.LineNumberParseException;
import nl.rrd.wool.model.WoolNodeBody;
import nl.rrd.wool.model.WoolVariableString;
import nl.rrd.wool.model.command.WoolCommand;
import nl.rrd.wool.utils.CurrentIterator;

public class WoolBodyParser {
	public WoolNodeBody parse(List<WoolBodyToken> tokens,
			List<String> validCommands) throws LineNumberParseException {
		WoolNodeBody body = new WoolNodeBody();
		CurrentIterator<WoolBodyToken> it = new CurrentIterator<>(
				tokens.iterator());
		it.moveNext();
		while (it.getCurrent() != null) {
			WoolBodyToken token = it.getCurrent();
			switch (token.getType()) {
			case TEXT:
			case VARIABLE:
				WoolVariableString text = parseTextSegment(it);
				if (body.getReplies().isEmpty()) {
					body.addSegment(new WoolNodeBody.TextSegment(text));
				} else if (!text.isWhitespace()) {
					throw new LineNumberParseException(
							"Found content between replies", token.getLineNum(),
							token.getColNum());
				}
				break;
			case COMMAND_START:
				if (!body.getReplies().isEmpty()) {
					throw new LineNumberParseException(
							"Found << between replies", token.getLineNum(),
							token.getColNum());
				}
				WoolCommandParser cmdParser = new WoolCommandParser(
						validCommands);
				WoolCommand command = cmdParser.parse(it);
				body.addSegment(new WoolNodeBody.CommandSegment(command));
				break;
			case REPLY_START:
				WoolReplyParser replyParser = new WoolReplyParser();
				body.addReply(replyParser.parse(it));
				break;
			default:
				// If we get here, there must be a bug
				throw new LineNumberParseException("Unexpected token type: " +
						token.getType(), token.getLineNum(), token.getColNum());
			}
		}
		body.trimWhitespace();
		return body;
	}

	private WoolVariableString parseTextSegment(
			CurrentIterator<WoolBodyToken> tokens) {
		WoolVariableString string = new WoolVariableString();
		boolean foundEnd = false;
		while (!foundEnd && tokens.getCurrent() != null) {
			WoolBodyToken token = tokens.getCurrent();
			switch (token.getType()) {
			case TEXT:
				string.addSegment(new WoolVariableString.TextSegment(
						(String)token.getValue()));
				break;
			case VARIABLE:
				string.addSegment(new WoolVariableString.VariableSegment(
						(String)token.getValue()));
				break;
			default:
				foundEnd = true;
			}
			if (!foundEnd)
				tokens.moveNext();
		}
		return string;
	}
}
