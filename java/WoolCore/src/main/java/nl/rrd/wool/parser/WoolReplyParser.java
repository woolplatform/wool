package nl.rrd.wool.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nl.rrd.wool.exception.LineNumberParseException;
import nl.rrd.wool.model.WoolNodeBody;
import nl.rrd.wool.model.WoolReply;
import nl.rrd.wool.model.nodepointer.WoolNodePointer;
import nl.rrd.wool.model.nodepointer.WoolNodePointerExternal;
import nl.rrd.wool.model.nodepointer.WoolNodePointerInternal;
import nl.rrd.wool.utils.CurrentIterator;

public class WoolReplyParser {
	private WoolNodeState nodeState;
	
	private ReplySection statementSection;
	private ReplySection nodePointerSection;
	private ReplySection commandSection;
	
	public WoolReplyParser(WoolNodeState nodeState) {
		this.nodeState = nodeState;
	}
	
	public WoolReply parse(CurrentIterator<WoolBodyToken> tokens)
			throws LineNumberParseException {
		readSections(tokens);
		WoolNodeBody statement = parseStatement();
		WoolNodePointer nodePointer = parseNodePointer();
		WoolReply reply = new WoolReply(nodeState.createNextReplyId(),
				statement, nodePointer);
		if (commandSection != null)
			parseCommands(reply);
		return reply;
	}
	
	private void readSections(CurrentIterator<WoolBodyToken> tokens)
			throws LineNumberParseException {
		int maxSections = 3;
		WoolBodyToken startToken = tokens.getCurrent();
		tokens.moveNext();
		List<ReplySection> sections = new ArrayList<>();
		ReplySection currSection = new ReplySection();
		sections.add(currSection);
		boolean foundEnd = false;
		while (!foundEnd && tokens.getCurrent() != null) {
			WoolBodyToken token = tokens.getCurrent();
			switch (token.getType()) {
			case REPLY_SEPARATOR:
				if (sections.size() == maxSections) {
					throw new LineNumberParseException(String.format(
							"Exceeded maximum number of %s sections",
							maxSections), token.getLineNum(),
							token.getColNum());
				}
				currSection.endLineNum = token.getLineNum();
				currSection.endColNum = token.getColNum();
				currSection = new ReplySection();
				sections.add(currSection);
				break;
			case REPLY_END:
				currSection.endLineNum = token.getLineNum();
				currSection.endColNum = token.getColNum();
				foundEnd = true;
				break;
			default:
				currSection.tokens.add(token);
			}
			tokens.moveNext();
		}
		if (!foundEnd) {
			throw new LineNumberParseException("Reply not terminated",
					startToken.getLineNum(), startToken.getColNum());
		}
		statementSection = null;
		nodePointerSection = null;
		commandSection = null;
		if (sections.size() == 1) {
			nodePointerSection = sections.get(0);
		} else if (sections.size() == 2) {
			statementSection = sections.get(0);
			nodePointerSection = sections.get(1);
		} else {
			statementSection = sections.get(0);
			nodePointerSection = sections.get(1);
			commandSection = sections.get(2);
		}
	}
	
	private WoolNodeBody parseStatement() throws LineNumberParseException {
		if (statementSection == null)
			return null;
		WoolBodyParser bodyParser = new WoolBodyParser(nodeState);
		WoolNodeBody result = bodyParser.parse(statementSection.tokens,
				Arrays.asList("input"));
		if (result.getSegments().isEmpty())
			return null;
		else
			return result;
	}
	
	private WoolNodePointer parseNodePointer() throws LineNumberParseException {
		WoolBodyToken.trimWhitespace(nodePointerSection.tokens);
		if (nodePointerSection.tokens.size() == 0) {
			throw new LineNumberParseException("Empty node pointer in reply",
					nodePointerSection.endLineNum,
					nodePointerSection.endColNum);
		}
		WoolBodyToken nodePointerToken = nodePointerSection.tokens.get(0);
		if (nodePointerSection.tokens.size() != 1 ||
				nodePointerToken.getType() != WoolBodyToken.Type.TEXT) {
			throw new LineNumberParseException(
					"Invalid node pointer in reply",
					nodePointerToken.getLineNum(),
					nodePointerToken.getColNum());
		}
		String nodePointerStr = (String)nodePointerToken.getValue();
		if (!nodePointerStr.matches(WoolParser.NODE_NAME_REGEX +
				"(\\." + WoolParser.NODE_NAME_REGEX + ")?")) {
			throw new LineNumberParseException(
					"Invalid node pointer in reply: " + nodePointerStr,
					nodePointerToken.getLineNum(),
					nodePointerToken.getColNum());
		}
		int sep = nodePointerStr.indexOf('.');
		WoolNodePointer result;
		if (sep == -1) {
			result = new WoolNodePointerInternal(nodePointerStr);
		} else {
			result = new WoolNodePointerExternal(
					nodePointerStr.substring(0, sep),
					nodePointerStr.substring(sep + 1));
		}
		nodeState.addNodePointerToken(result, nodePointerToken);
		return result;
	}
	
	private void parseCommands(WoolReply reply)
			throws LineNumberParseException {
		CurrentIterator<WoolBodyToken> it = new CurrentIterator<>(
				commandSection.tokens.iterator());
		it.moveNext();
		WoolBodyToken.skipWhitespace(it);
		while (it.getCurrent() != null) {
			WoolBodyToken token = it.getCurrent();
			if (token.getType() != WoolBodyToken.Type.COMMAND_START) {
				throw new LineNumberParseException(
						"Expected <<, found token: " + token.getType(),
						token.getLineNum(), token.getColNum());
			}
			WoolCommandParser cmdParser = new WoolCommandParser(
					Arrays.asList("action", "set"), nodeState);
			reply.addCommand(cmdParser.parseFromStart(it));
			WoolBodyToken.skipWhitespace(it);
		}
	}

	private class ReplySection {
		private List<WoolBodyToken> tokens = new ArrayList<>();
		private int endLineNum;
		private int endColNum;
	}
}
