/*
 * Copyright 2019-2022 WOOL Foundation - Licensed under the MIT License:
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package eu.woolplatform.wool.parser;

import java.util.ArrayList;
import java.util.List;

import nl.rrd.utils.ReferenceParameter;
import nl.rrd.utils.exception.LineNumberParseException;
import eu.woolplatform.wool.model.WoolVariableString;

public class WoolBodyTokenizer {
	private BodyState bodyState = new BodyState();

	/**
	 * Reads the body tokens from the specified line. The line should end with a
	 * newline (\n) character.
	 * 
	 * @param line the line with \n
	 * @param lineNum the line number (first line is 1)
	 * @return the body tokens
	 * @throws LineNumberParseException if a parsing error occurs
	 */
	public List<WoolBodyToken> readBodyTokens(String line, int lineNum)
			throws LineNumberParseException {
		List<WoolBodyToken> tokens = new ArrayList<>();
		startBodyTextBuffer(1);
		StringBuilder specialBuffer = null;
		boolean foundComment = false;
		int i = 0;
		while (!foundComment && i < line.length()) {
			char c = line.charAt(i);
			if (specialBuffer == null) {
				switch(c) {
				case '$': // possible start of variable
					i = readBodyVariable(tokens, line, lineNum, i);
					break;
				case '\\':
				case '<': // possible start of <<
				case '>': // possible start of >>
				case '/': // possible start of //
					specialBuffer = new StringBuilder();
					specialBuffer.append(c);
					i++;
					break;
				case '[': // possible start of [[, only special outside command
				case ']': // possible start of ]], only special outside command
					if (bodyState.inCommand) {
						bodyState.textBuffer.append(c);
					} else {
						specialBuffer = new StringBuilder();
						specialBuffer.append(c);
					}
					i++;
					break;
				case '"': // start of quoted string, only special inside command
					if (bodyState.inCommand)
						i = readQuotedString(tokens, line, lineNum, i);
					else {
						bodyState.textBuffer.append(c);
						i++;
					}
					break;
				case '|': // reply separator, only special inside reply and outside command
					if (bodyState.inReply && !bodyState.inCommand) {
						finishTextToken(tokens, line, lineNum, i);
						finishReplySeparator(tokens, lineNum, i + 1);
						startBodyTextBuffer(i + 2);
					} else {
						bodyState.textBuffer.append(c);
					}
					i++;
					break;
				default:
					bodyState.textBuffer.append(c);
					i++;
				}
			} else {
				char specialStart = specialBuffer.charAt(0);
				switch (specialStart) {
				case '\\':
					bodyState.textBuffer.append(c);
					specialBuffer = null;
					i++;
					break;
				case '<':
					if (c == '<') {
						finishTextToken(tokens, line, lineNum, i - 1);
						finishCommandStart(tokens, lineNum, i);
						startBodyTextBuffer(i + 2);
						i++;
					} else {
						bodyState.textBuffer.append(specialBuffer);
					}
					specialBuffer = null;
					break;
				case '>':
					if (c == '>') {
						finishTextToken(tokens, line, lineNum, i - 1);
						finishCommandEnd(tokens, lineNum, i);
						startBodyTextBuffer(i + 2);
						i++;
					} else {
						bodyState.textBuffer.append(specialBuffer);
					}
					specialBuffer = null;
					break;
				case '[':
					if (c == '[') {
						finishTextToken(tokens, line, lineNum, i - 1);
						finishReplyStart(tokens, lineNum, i);
						startBodyTextBuffer(i + 2);
						i++;
					} else {
						bodyState.textBuffer.append(specialBuffer);
					}
					specialBuffer = null;
					break;
				case ']':
					if (c == ']') {
						finishTextToken(tokens, line, lineNum, i - 1);
						finishReplyEnd(tokens, lineNum, i);
						startBodyTextBuffer(i + 2);
						i++;
					} else {
						bodyState.textBuffer.append(specialBuffer);
					}
					specialBuffer = null;
					break;
				case '/':
					if (c == '/') {
						foundComment = true;
					} else {
						bodyState.textBuffer.append(specialBuffer);
					}
					specialBuffer = null;
					break;
				}
			}
		}
		if (specialBuffer != null) {
			char specialStart = specialBuffer.charAt(0);
			switch (specialStart) {
			case '\\':
				break;
			case '<':
			case '>':
			case '[':
			case ']':
			case '/':
				bodyState.textBuffer.append(specialStart);
				break;
			}
		}
		finishTextToken(tokens, line, lineNum, line.length());
		return tokens;
	}

	private int readBodyVariable(List<WoolBodyToken> tokens, String line,
			int lineNum, int start) {
		ReferenceParameter<Integer> end = new ReferenceParameter<>();
		String varName = readVariableName(line, start + 1, end);
		if (varName.length() == 0) {
			bodyState.textBuffer.append('$');
			return end.get();
		}
		finishTextToken(tokens, line, lineNum, start);
		WoolBodyToken token = new WoolBodyToken();
		token.setType(WoolBodyToken.Type.VARIABLE);
		token.setText(line.substring(start, end.get()));
		token.setValue(varName);
		token.setLineNum(lineNum);
		token.setColNum(start + 1);
		tokens.add(token);
		startBodyTextBuffer(end.get() + 1);
		return end.get();
	}

	private String readVariableName(String line, int start,
			ReferenceParameter<Integer> end) {
		for (int i = start; i < line.length(); i++) {
			char c = line.charAt(i);
			if (i == start && (c < 'A' || c > 'Z') &&
					(c < 'a' || c > 'z') && c != '_') {
				end.set(i);
				return "";
			} else if (i > start && (c < 'A' || c > 'Z') &&
					(c < 'a' || c > 'z') && (c < '0' || c > '9') && c != '_') {
				end.set(i);
				return line.substring(start, i);
			}
		}
		end.set(line.length());
		return line.substring(start);
	}

	private int readQuotedString(List<WoolBodyToken> tokens, String line,
			int lineNum, int start) throws LineNumberParseException {
		finishTextToken(tokens, line, lineNum, start);
		ReferenceParameter<Integer> end = new ReferenceParameter<>();
		WoolVariableString string = readQuotedString(line, lineNum, start, end);
		WoolBodyToken token = new WoolBodyToken();
		token.setType(WoolBodyToken.Type.QUOTED_STRING);
		token.setLineNum(lineNum);
		token.setColNum(start + 1);
		token.setText(line.substring(start, end.get()));
		token.setValue(string);
		tokens.add(token);
		startBodyTextBuffer(end.get() + 1);
		return end.get();
	}

	private WoolVariableString readQuotedString(String line, int lineNum,
			int start, ReferenceParameter<Integer> end)
			throws LineNumberParseException {
		WoolVariableString result = new WoolVariableString();
		StringBuilder textBuffer = new StringBuilder();
		int textStart = start + 1;
		boolean prevEscape = false;
		int i = start + 1;
		while (i < line.length()) {
			if (prevEscape) {
				prevEscape = false;
				i++;
				continue;
			}
			char c = line.charAt(i);
			switch (c) {
			case '\\':
				textBuffer.append(line, textStart, i);
				textStart = i + 1;
				prevEscape = true;
				i++;
				break;
			case '$':
				ReferenceParameter<Integer> varEnd =
						new ReferenceParameter<>();
				String varName = readVariableName(line, i + 1, varEnd);
				if (varName.length() > 0) {
					textBuffer.append(line, textStart, i);
					if (textBuffer.length() > 0) {
						result.addSegment(new WoolVariableString.TextSegment(
								textBuffer.toString()));
					}
					result.addSegment(new WoolVariableString.VariableSegment(
							varName));
					textBuffer = new StringBuilder();
					textStart = varEnd.get();
					i = textStart;
				} else {
					i++;
				}
				break;
			case '"':
				textBuffer.append(line, textStart, i);
				if (textBuffer.length() > 0) {
					result.addSegment(new WoolVariableString.TextSegment(
							textBuffer.toString()));
				}
				end.set(i + 1);
				return result;
			default:
				i++;
			}
		}
		throw new LineNumberParseException("Quoted string not terminated",
				lineNum, start + 1);
	}
	
	private void startBodyTextBuffer(int colNum) {
		bodyState.textBuffer = new StringBuilder();
		bodyState.textStartCol = colNum;
	}
	
	private void finishTextToken(List<WoolBodyToken> tokens, String line,
			int lineNum, int end) {
		String text = bodyState.textBuffer.toString();
		if (text.length() == 0)
			return;
		WoolBodyToken token = new WoolBodyToken();
		token.setType(WoolBodyToken.Type.TEXT);
		token.setText(line.substring(bodyState.textStartCol - 1, end));
		token.setValue(text);
		token.setLineNum(lineNum);
		token.setColNum(bodyState.textStartCol);
		tokens.add(token);
	}
	
	private void finishCommandStart(List<WoolBodyToken> tokens, int lineNum,
			int colNum) throws LineNumberParseException {
		if (bodyState.inCommand) {
			throw new LineNumberParseException("Found << inside <<...>>",
					lineNum, colNum);
		}
		WoolBodyToken token = new WoolBodyToken();
		token.setType(WoolBodyToken.Type.COMMAND_START);
		token.setText("<<");
		token.setLineNum(lineNum);
		token.setColNum(colNum);
		tokens.add(token);
		bodyState.inCommand = true;
	}
	
	private void finishCommandEnd(List<WoolBodyToken> tokens, int lineNum,
			int colNum) throws LineNumberParseException {
		if (!bodyState.inCommand) {
			throw new LineNumberParseException("Found >> without preceding <<",
					lineNum, colNum);
		}
		WoolBodyToken token = new WoolBodyToken();
		token.setType(WoolBodyToken.Type.COMMAND_END);
		token.setText(">>");
		token.setLineNum(lineNum);
		token.setColNum(colNum);
		tokens.add(token);
		bodyState.inCommand = false;
	}
	
	private void finishReplyStart(List<WoolBodyToken> tokens, int lineNum,
			int colNum) throws LineNumberParseException {
		if (bodyState.inReply) {
			throw new LineNumberParseException("Found [[ inside [[...]]",
					lineNum, colNum);
		}
		WoolBodyToken token = new WoolBodyToken();
		token.setType(WoolBodyToken.Type.REPLY_START);
		token.setText("[[");
		token.setLineNum(lineNum);
		token.setColNum(colNum);
		tokens.add(token);
		bodyState.inReply = true;
	}
	
	private void finishReplyEnd(List<WoolBodyToken> tokens, int lineNum,
			int colNum) throws LineNumberParseException {
		if (!bodyState.inReply) {
			throw new LineNumberParseException("Found ]] without preceding [[",
					lineNum, colNum);
		}
		WoolBodyToken token = new WoolBodyToken();
		token.setType(WoolBodyToken.Type.REPLY_END);
		token.setText("]]");
		token.setLineNum(lineNum);
		token.setColNum(colNum);
		tokens.add(token);
		bodyState.inReply = false;
	}
	
	private void finishReplySeparator(List<WoolBodyToken> tokens, int lineNum,
			int colNum) {
		WoolBodyToken token = new WoolBodyToken();
		token.setType(WoolBodyToken.Type.REPLY_SEPARATOR);
		token.setText("|");
		token.setLineNum(lineNum);
		token.setColNum(colNum);
		tokens.add(token);
	}

	private static class BodyState {
		private boolean inCommand = false;
		private boolean inReply = false;
		private StringBuilder textBuffer;
		private int textStartCol;
	}
}
