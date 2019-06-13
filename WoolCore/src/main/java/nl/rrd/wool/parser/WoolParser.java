package nl.rrd.wool.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import nl.rrd.wool.exception.LineNumberParseException;
import nl.rrd.wool.io.LineColumnNumberReader;
import nl.rrd.wool.model.WoolDialogue;
import nl.rrd.wool.model.WoolNode;
import nl.rrd.wool.model.WoolNodeBody;
import nl.rrd.wool.model.WoolNodeHeader;
import nl.rrd.wool.model.WoolReply;
import nl.rrd.wool.model.WoolVariableString;
import nl.rrd.wool.model.command.WoolCommand;
import nl.rrd.wool.model.nodepointer.WoolNodePointer;
import nl.rrd.wool.model.nodepointer.WoolNodePointerExternal;
import nl.rrd.wool.model.nodepointer.WoolNodePointerInternal;
import nl.rrd.wool.utils.CurrentIterator;

public class WoolParser {
	private static final String NODE_NAME_REGEX = "[A-Za-z0-9_-]+";
	
	private LineColumnNumberReader reader;
	
	public WoolParser(String filename) throws FileNotFoundException {
		this(new File(filename));
	}
	
	public WoolParser(File file) throws FileNotFoundException {
		this(new FileInputStream(file));
	}
	
	public WoolParser(InputStream input) {
		init(input);
	}
	
	public WoolParser(Reader reader) {
		init(new LineColumnNumberReader(reader));
	}
	
	private void init(InputStream input) {
		try {
			init(new InputStreamReader(input, "UTF-8"));
		} catch (UnsupportedEncodingException ex) {
			throw new RuntimeException("UTF-8 not supported: " +
					ex.getMessage(), ex);
		}
	}
	
	private void init(Reader reader) {
		if (reader instanceof LineColumnNumberReader) {
			this.reader = (LineColumnNumberReader)reader;
		} else if (reader instanceof BufferedReader) {
			this.reader = new LineColumnNumberReader(reader);
		} else {
			this.reader = new LineColumnNumberReader(
					new BufferedReader(reader));
		}
	}
	
	public void close() throws IOException {
		reader.close();
	}
	
	public WoolDialogue readDialogue() throws LineNumberParseException,
			IOException {
		WoolDialogue dialogue = new WoolDialogue();
		WoolNode node;
		while ((node = readNode()) != null) {
			dialogue.getNodes().add(node);
		}
		return dialogue;
	}
	
	private WoolNode readNode() throws LineNumberParseException, IOException {
		Map<String,String> headerMap = new LinkedHashMap<>();
		boolean inHeader = true;
		int lineNum = reader.getLineNum();
		String line = readLine();
		while (line != null && inHeader) {
			if (getContent(line).equals("---")) {
				inHeader = false;
			} else {
				parseHeaderLine(headerMap, line, lineNum);
				lineNum = reader.getLineNum();
				line = readLine();
			}
		}
		if (inHeader) {
			if (headerMap.isEmpty())
				return null;
			throw new LineNumberParseException(
					"Found incomplete node at end of file",
					reader.getLineNum(), reader.getColNum());
		}
		WoolNodeHeader header = createHeader(headerMap, lineNum);
		WoolNodeBody body = new WoolNodeBody();
		boolean inBody = true;
		WoolBodyTokenizer tokenizer = new WoolBodyTokenizer();
		lineNum = reader.getLineNum();
		line = readLine();
		List<WoolBodyToken> bodyTokens = new ArrayList<>();
		while (line != null && inBody) {
			if (getContent(line).equals("===")) {
				inBody = false;
			} else {
				bodyTokens.addAll(tokenizer.readBodyTokens(line + "\n",
						lineNum));
				lineNum = reader.getLineNum();
				line = readLine();
			}
		}
		parseBodyTokens(bodyTokens, body, Arrays.asList("action", "if", "set"));
		return new WoolNode(header, body);
	}
	
	private void parseHeaderLine(Map<String,String> headerMap, String line,
			int lineNum) throws LineNumberParseException {
		int commentSep = line.indexOf("//");
		if (commentSep != -1)
			line = line.substring(0, commentSep);
		if (line.trim().isEmpty())
			return;
		int sep = line.indexOf(':');
		if (sep == -1) {
			throw new LineNumberParseException(
					"Character : not found in header line", lineNum, 1);
		}
		String keyUntrimmed = line.substring(0, sep);
		String key = keyUntrimmed.trim();
		int keyIndex = 1;
		if (!key.isEmpty())
			keyIndex += skipWhitespace(keyUntrimmed, 0);
		String valueUntrimmed = line.substring(sep + 1);
		String value = valueUntrimmed.trim();
		int valueIndex = sep + 2;
		if (!value.isEmpty())
			valueIndex += skipWhitespace(valueUntrimmed, 0);
		if (key.length() == 0) {
			throw new LineNumberParseException("Found empty header name",
					lineNum, 0);
		}
		if (headerMap.containsKey(key)) {
			throw new LineNumberParseException("Found duplicate header: " + key,
					lineNum, keyIndex);
		}
		if (key.equals("title")) {
			if (!value.matches(NODE_NAME_REGEX)) {
				throw new LineNumberParseException(
						"Invalid node title: " + value, lineNum, valueIndex);
			}
		} else if (key.equals("speaker")) {
			if (value.length() == 0) {
				throw new LineNumberParseException("Found empty speaker",
						lineNum, valueIndex);
			}
		}
		headerMap.put(key, value);
	}
	
	private WoolNodeHeader createHeader(Map<String,String> headerMap,
			int lineNum) throws LineNumberParseException {
		List<String> requiredHeaders = Arrays.asList("title", "speaker");
		Map<String,String> optionalTags = new LinkedHashMap<>(headerMap);
		for (String requiredHeader : requiredHeaders) {
			if (!headerMap.containsKey(requiredHeader)) {
				throw new LineNumberParseException(String.format(
						"Required header \"%s\" not found", requiredHeader),
						lineNum, 1);
			}
			optionalTags.remove(requiredHeader);
		}
		WoolNodeHeader header = new WoolNodeHeader(headerMap.get("title"),
				optionalTags);
		header.setSpeaker(headerMap.get("speaker"));
		return header;
	}
	
	private void parseBodyTokens(List<WoolBodyToken> tokens, WoolNodeBody body,
			List<String> validCommands) throws LineNumberParseException {
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
				body.addReply(parseReplySegment(it));
				break;
			default:
				// If we get here, there must be a bug
				throw new LineNumberParseException("Unexpected token type: " +
						token.getType(), token.getLineNum(), token.getColNum());
			}
		}
		body.trimWhitespace();
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
	
	private class ReplySection {
		private List<WoolBodyToken> tokens = new ArrayList<>();
		private int endLineNum;
		private int endColNum;
	}
	
	private WoolReply parseReplySegment(CurrentIterator<WoolBodyToken> tokens)
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
		ReplySection statementSection = null;
		ReplySection nodePointerSection = null;
		ReplySection commandSection = null;
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
		WoolNodeBody statement = null;
		if (statementSection != null) {
			statement = new WoolNodeBody();
			parseBodyTokens(statementSection.tokens, statement,
					Arrays.asList("input"));
			if (statement.getSegments().isEmpty())
				statement = null;
		}
		trimWhitespace(nodePointerSection.tokens);
		if (nodePointerSection.tokens.size() == 0) {
			throw new LineNumberParseException("Empty node pointer in reply",
					nodePointerSection.endLineNum,
					nodePointerSection.endColNum);
		}
		WoolBodyToken nodePointerToken = nodePointerSection.tokens.get(0);
		if (nodePointerSection.tokens.size() != 1 ||
				nodePointerToken.getType() != WoolBodyToken.Type.TEXT) {
			StringBuilder text = new StringBuilder();
			for (WoolBodyToken token : nodePointerSection.tokens) {
				text.append(token.getText());
			}
			throw new LineNumberParseException(
					"Invalid node pointer in reply: " + text,
					nodePointerToken.getLineNum(),
					nodePointerToken.getColNum());
		}
		String nodePointerStr = (String)nodePointerToken.getValue();
		if (!nodePointerStr.matches(NODE_NAME_REGEX +
				"(\\." + NODE_NAME_REGEX + ")?")) {
			throw new LineNumberParseException(
					"Invalid node pointer in reply: " + nodePointerStr,
					nodePointerToken.getLineNum(),
					nodePointerToken.getColNum());
		}
		int sep = nodePointerStr.indexOf('.');
		WoolNodePointer nodePointer;
		if (sep == -1) {
			nodePointer = new WoolNodePointerInternal(nodePointerStr);
		} else {
			nodePointer = new WoolNodePointerExternal(
					nodePointerStr.substring(0, sep),
					nodePointerStr.substring(sep + 1));
		}
		if (commandSection != null) {
			// TODO parse command, remove text tokens with only whitespace
		}
		return new WoolReply(statement, nodePointer);
	}
	
	/**
	 * Removes a possible comment, and leading and trailing white space from a
	 * string. This should not be used for body lines, because this method does
	 * not check whether a comment marker (//) is inside a string literal.
	 *
	 * @param s the string or null
	 * @return the content or null
	 */
	private String getContent(String s) {
		if (s == null)
			return null;
		int commentIndex = s.indexOf("//");
		if (commentIndex != -1)
			s = s.substring(0, commentIndex);
		return s.trim();
	}
	
	/**
	 * Reads whitespace characters from the specified index and returns the
	 * number of characters read.
	 * 
	 * @param s the string
	 * @param start the start index
	 * @return the number of whitespace characters
	 */
	private int skipWhitespace(String s, int start) {
		int result = 0;
		for (int i = start; i < s.length(); i++) {
			char c = s.charAt(i);
			if (!Character.isWhitespace(c))
				return result;
			result++;
		}
		return result;
	}
	
	private void trimWhitespace(List<WoolBodyToken> tokens) {
		removeLeadingWhitespace(tokens);
		removeTrailingWhitespace(tokens);
	}
	
	private void removeLeadingWhitespace(List<WoolBodyToken> tokens) {
		while (!tokens.isEmpty()) {
			WoolBodyToken token = tokens.get(0);
			if (token.getType() != WoolBodyToken.Type.TEXT)
				return;
			String text = (String)token.getValue();
			text = text.replaceAll("^\\s+", "");
			token.setValue(text);
			if (text.length() > 0)
				return;
			tokens.remove(0);
		}
	}
	
	private void removeTrailingWhitespace(List<WoolBodyToken> tokens) {
		while (!tokens.isEmpty()) {
			WoolBodyToken token = tokens.get(tokens.size() - 1);
			if (token.getType() != WoolBodyToken.Type.TEXT)
				return;
			String text = (String)token.getValue();
			text = text.replaceAll("\\s+$", "");
			token.setValue(text);
			if (text.length() > 0)
				return;
			tokens.remove(tokens.size() - 1);
		}
	}
	
	private String readLine() throws IOException {
		StringBuilder builder = new StringBuilder();
		boolean foundCR = false;
		while (true) {
			if (foundCR) {
				Object restoreState = reader.getRestoreState(1);
				int c = reader.read();
				if (c == -1 || c == '\n')
					reader.clearRestoreState(restoreState);
				else
					reader.restoreState(restoreState);
				return builder.toString();
			}
			int c = reader.read();
			if (c == -1) {
				if (builder.length() == 0)
					return null;
				else
					return builder.toString();
			} else if (c == '\n') {
				return builder.toString();
			} else if (c == '\r') {
				foundCR = true;
			} else {
				builder.append((char)c);
			}
		}
	}
}
