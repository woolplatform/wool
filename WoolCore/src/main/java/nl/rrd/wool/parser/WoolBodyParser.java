package nl.rrd.wool.parser;

import java.util.List;

import nl.rrd.wool.exception.LineNumberParseException;
import nl.rrd.wool.model.WoolNodeBody;
import nl.rrd.wool.model.WoolVariableString;
import nl.rrd.wool.model.command.WoolCommand;
import nl.rrd.wool.utils.CurrentIterator;

public class WoolBodyParser {
	private WoolNodeState nodeState;
	
	public WoolBodyParser(WoolNodeState nodeState) {
		this.nodeState = nodeState;
	}
	
	public WoolNodeBody parse(List<WoolBodyToken> tokens,
			List<String> validCommands) throws LineNumberParseException {
		CurrentIterator<WoolBodyToken> it = new CurrentIterator<>(
				tokens.iterator());
		it.moveNext();
		ParseUntilIfClauseResult result = parseUntilIfClause(it, validCommands);
		if (result.ifClauseStartToken != null) {
			WoolBodyToken token = it.getCurrent();
			throw new LineNumberParseException(String.format(
					"Unexpected command \"%s\"", result.ifClauseName),
					token.getLineNum(), token.getColNum());
		}
		return result.body;
	}
	
	public ParseUntilIfClauseResult parseUntilIfClause(
			CurrentIterator<WoolBodyToken> tokens, List<String> validCommands)
			throws LineNumberParseException {
		ParseUntilIfClauseResult result = new ParseUntilIfClauseResult();
		result.body = new WoolNodeBody();
		while (result.ifClauseStartToken == null && tokens.getCurrent() != null) {
			WoolBodyToken token = tokens.getCurrent();
			switch (token.getType()) {
			case TEXT:
			case VARIABLE:
				WoolVariableString text = parseTextSegment(tokens);
				if (result.body.getReplies().isEmpty()) {
					result.body.addSegment(new WoolNodeBody.TextSegment(text));
				} else if (!text.isWhitespace()) {
					throw new LineNumberParseException(
							"Found content between replies", token.getLineNum(),
							token.getColNum());
				}
				break;
			case COMMAND_START:
				if (!result.body.getReplies().isEmpty()) {
					throw new LineNumberParseException(
							"Found << between replies", token.getLineNum(),
							token.getColNum());
				}
				WoolCommandParser cmdParser = new WoolCommandParser(
						validCommands, nodeState);
				String name = cmdParser.readCommandName(tokens);
				if (name.equals("elseif") || name.equals("else") ||
						name.equals("endif")) {
					result.ifClauseStartToken = token;
					result.ifClauseName = name;
				} else {
					WoolCommand command = cmdParser.parseFromName(token, tokens);
					result.body.addSegment(new WoolNodeBody.CommandSegment(command));
				}
				break;
			case REPLY_START:
				WoolReplyParser replyParser = new WoolReplyParser(nodeState);
				result.body.addReply(replyParser.parse(tokens));
				break;
			default:
				// If we get here, there must be a bug
				throw new LineNumberParseException("Unexpected token type: " +
						token.getType(), token.getLineNum(), token.getColNum());
			}
		}
		result.body.trimWhitespace();
		return result;
	}
	
	public class ParseUntilIfClauseResult {
		public WoolNodeBody body;
		public WoolBodyToken ifClauseStartToken = null;
		public String ifClauseName = null;
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
