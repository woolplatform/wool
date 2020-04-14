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

package eu.woolplatform.utils.json;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class can read atomic JSON tokens from a stream. After construction the
 * reader is positioned before the first token. Call {@link #moveNext()
 * moveNext()} to move to the first token. Then you can get the current token
 * with {@link #getToken() getToken()}.
 * 
 * @author Dennis Hofs (RRD)
 */
public class JsonStreamReader {
	private static final int BUFFER_SIZE = 1024;
	
	private boolean isStringAtomic = true;
	
	private Reader reader;
	private boolean endOfStream = false;
	private StringBuilder parsedBoolean = null;
	private StringBuilder parsedNull = null;
	private StringBuilder parsedNumber = null;
	private JsonAtomicToken currentToken = null;
	private char[] buffer = new char[BUFFER_SIZE];
	private int bufferPos = 0;
	private int bufferLen = 0;
	private int documentLine = 1;
	private int documentLinePos = 1;
	private int tokenStartLine = 1;
	private int tokenStartLinePos = 1;
	private boolean consumedCR = false;
	private NumberPos numberPos = null;
	private List<ObjectList> objectListStack = new ArrayList<>();
	private StringBuilder stringEscape = null;
	private StringBuilder parsedString = null;
	private boolean inObjectKey = false;
	
	/**
	 * Constructs a new JSON stream reader. It reads UTF-8 characters from the
	 * specified input stream.
	 * 
	 * @param input the input stream
	 */
	public JsonStreamReader(InputStream input) {
		this.reader = new InputStreamReader(input, StandardCharsets.UTF_8);
	}
	
	/**
	 * Constructs a new JSON stream reader.
	 * 
	 * @param reader the underlying reader
	 */
	public JsonStreamReader(Reader reader) {
		this.reader = reader;
	}

	/**
	 * Closes this reader and the underlying reader.
	 * 
	 * @throws IOException if the underlying reader can't be closed
	 */
	public void close() throws IOException {
		reader.close();
	}

	/**
	 * Sets whether a string is an atomic token. If this is true (default), you
	 * will receive token {@link JsonAtomicToken.Type#STRING STRING}. Otherwise
	 * you will receive tokens {@link JsonAtomicToken.Type#START_STRING
	 * START_STRING}, {@link JsonAtomicToken.Type#STRING_CHARACTER
	 * STRING_CHARACTER} and {@link JsonAtomicToken.Type#END_STRING
	 * END_STRING}. Reading a string as an atomic token is much more efficient,
	 * but you may want to receive separate character tokens for long strings.
	 * 
	 * @param isStringAtomic true if a string is an atomic token (default),
	 * false otherwise
	 */
	public void setIsStringAtomic(boolean isStringAtomic) {
		this.isStringAtomic = isStringAtomic;
	}
	
	/**
	 * Returns whether a string is an atomic token. If this is true (default),
	 * you will receive token {@link JsonAtomicToken.Type#STRING STRING}.
	 * Otherwise you will receive tokens {@link
	 * JsonAtomicToken.Type#START_STRING START_STRING}, {@link
	 * JsonAtomicToken.Type#STRING_CHARACTER STRING_CHARACTER} and {@link
	 * JsonAtomicToken.Type#END_STRING END_STRING}. Reading a string as an
	 * atomic token is more efficient, but you may want to receive separate
	 * character tokens for very long strings.
	 * 
	 * @return true if a string is an atomic token (default), false otherwise
	 */
	public boolean isStringAtomic() {
		return isStringAtomic;
	}
	
	/**
	 * Returns the current line number in the document. The first line is 1.
	 * If the reader is positioned at a token, then the document position is at
	 * the end of that token.
	 * 
	 * @return the current line number
	 */
	public int getDocumentLine() {
		return documentLine;
	}

	/**
	 * Returns the current character number in the current line in the
	 * document. The first character is 1. If the reader is positioned at a
	 * token, then the document position is at the end of that token.
	 * 
	 * @return the current character number
	 */
	public int getDocumentLinePos() {
		return documentLinePos;
	}
	
	/**
	 * Returns the line number in the document where the current token starts.
	 * The first line is 1. If the reader is before the first token, then this
	 * method returns 1. If the reader is after the last token, then this method
	 * return the line at the end of the document.
	 * 
	 * @return the line number where the current token starts
	 */
	public int getTokenStartLine() {
		return tokenStartLine;
	}
	
	/**
	 * Returns the character number in the line in the document where the
	 * current token starts. The first character is 1. If the reader is before
	 * the first token, then this method returns 1. If the reader is after the
	 * last token, then this method return the line at the end of the document.
	 * 
	 * @return the character number where the current token starts
	 */
	public int getTokenStartLinePos() {
		return tokenStartLinePos;
	}
	
	/**
	 * Moves to the next token. The first time you call this method, it moves
	 * to the first token. If the end of the stream is reached and the complete
	 * document is valid, this method returns false, meaning there are no more
	 * tokens.
	 * 
	 * @return true if there is another token, false if the end of the document
	 * @throws JsonParseException if the JSON content is invalid
	 * @throws IOException if a reading error occurs
	 */
	public boolean moveNext() throws JsonParseException, IOException {
		while (true) {
			if (endOfStream) {
				if (currentToken != null)
					return finishStream();
				else
					return false;
			}
			if (bufferLen == 0) {
				bufferPos = 0;
				int len = reader.read(buffer);
				if (len <= 0) {
					bufferLen = 0;
					endOfStream = true;
					return finishStream();
				} else {
					bufferLen = len;
				}
			}
			while (bufferLen > 0) {
				char c = buffer[bufferPos];
				if (parsedString != null) {
					if (parseStringChar(c))
						return true;
				} else if (parsedNumber != null) {
					if (parseNumberChar(c))
						return true;
				} else if (parsedBoolean != null) {
					if (parseBooleanChar(c))
						return true;
				} else if (parsedNull != null) {
					if (parseNullChar(c))
						return true;
				} else if (currentToken == null) {
					if (parseValueInitial(c))
						return true;
				} else {
					switch (currentToken.getType()) {
					case START_OBJECT:
						if (parseStartObject(c))
							return true;
						break;
					case OBJECT_PAIR_SEPARATOR:
						if (parseObjectKey(c))
							return true;
						break;
					case START_LIST:
						if (parseStartList(c))
							return true;
						break;
					case OBJECT_KEY_VALUE_SEPARATOR:
					case LIST_ITEM_SEPARATOR:
						if (parseValueInitial(c))
							return true;
						break;
					case START_STRING:
					case STRING_CHARACTER:
						if (parseStringChar(c))
							return true;
						break;
					case END_STRING:
					case STRING:
						if (inObjectKey) {
							if (parseAfterObjectKey(c))
								return true;
						} else {
							if (parseAfterValue(c))
								return true;
						}
						break;
					case END_OBJECT:
					case END_LIST:
					case NUMBER:
					case BOOLEAN:
					case NULL:
						if (parseAfterValue(c))
							return true;
						break;
					}
				}
			}
		}
	}

	/**
	 * Returns the current token. If the reader is positioned before the first
	 * token or after the last token, this method returns null.
	 * 
	 * @return the current token or null
	 */
	public JsonAtomicToken getToken() {
		return currentToken;
	}
	
	/**
	 * Parses a character when the reader is positioned before or in the
	 * initial token of a JSON value.
	 * 
	 * <p>If this completes a new token, this method sets currentToken and
	 * returns true. Usually the specified character is consumed (see {@link
	 * #consumeCharacter(char) consumeCharacter()}, but it may happen that the
	 * specified character indicates that the previous character completed a
	 * token (in particular a number). In that case, this method sets
	 * currentToken and returns true, but does not consume the specified
	 * character. It will be parsed again at a next iteration and may even be
	 * found invalid then.</p>
	 * 
	 * @param c the character
	 * @return true if a new token was completed, false otherwise
	 * @throws JsonParseException if the JSON content is invalid
	 */
	private boolean parseValueInitial(char c) throws JsonParseException {
		if (Character.isWhitespace(c)) {
			consumeCharacter(c);
			return false;
		} else if (c == '{') {
			tokenStartLine = documentLine;
			tokenStartLinePos = documentLinePos;
			consumeCharacter(c);
			currentToken = new JsonAtomicToken(
					JsonAtomicToken.Type.START_OBJECT);
			objectListStack.add(ObjectList.OBJECT);
			return true;
		} else if (c == '[') {
			tokenStartLine = documentLine;
			tokenStartLinePos = documentLinePos;
			consumeCharacter(c);
			currentToken = new JsonAtomicToken(
					JsonAtomicToken.Type.START_LIST);
			objectListStack.add(ObjectList.LIST);
			return true;
		} else if (c == '"') {
			tokenStartLine = documentLine;
			tokenStartLinePos = documentLinePos;
			consumeCharacter(c);
			if (isStringAtomic) {
				parsedString = new StringBuilder();
				return false;
			} else {
				currentToken = new JsonAtomicToken(
						JsonAtomicToken.Type.START_STRING);
				return true;
			}
		} else if (c == 't' || c == 'f') {
			tokenStartLine = documentLine;
			tokenStartLinePos = documentLinePos;
			consumeCharacter(c);
			parsedBoolean = new StringBuilder();
			parsedBoolean.append(c);
			return false;
		} else if (c == 'n') {
			tokenStartLine = documentLine;
			tokenStartLinePos = documentLinePos;
			consumeCharacter(c);
			parsedNull = new StringBuilder();
			parsedNull.append(c);
			return false;
		} else { 
			tokenStartLine = documentLine;
			tokenStartLinePos = documentLinePos;
			return parseNumberChar(c);
		}
	}
	
	/**
	 * Parses a character when the reader is positioned after the start of an
	 * object ({). See {@link #parseValueInitial(char) parseValueInitial()} for
	 * more information about the result.
	 * 
	 * @param c the character
	 * @return true if a new token was completed, false otherwise
	 * @throws JsonParseException if the JSON content is invalid
	 */
	private boolean parseStartObject(char c) throws JsonParseException {
		if (Character.isWhitespace(c)) {
			consumeCharacter(c);
			return false;
		} else if (c == '"') {
			tokenStartLine = documentLine;
			tokenStartLinePos = documentLinePos;
			consumeCharacter(c);
			inObjectKey = true;
			if (isStringAtomic) {
				parsedString = new StringBuilder();
				return false;
			} else {
				currentToken = new JsonAtomicToken(
						JsonAtomicToken.Type.START_STRING);
				return true;
			}
		} else if (c == '}') {
			tokenStartLine = documentLine;
			tokenStartLinePos = documentLinePos;
			consumeCharacter(c);
			currentToken = new JsonAtomicToken(
					JsonAtomicToken.Type.END_OBJECT);
			objectListStack.remove(objectListStack.size() - 1);
			return true;
		} else {
			throw new JsonParseException(
					"Invalid character after start object: " + c, documentLine,
					documentLinePos);
		}
	}
	
	/**
	 * Parses a character when the reader is positioned after an object pair
	 * separator and thus before an object key. See {@link
	 * #parseValueInitial(char) parseValueInitial()} for more information about
	 * the result.
	 * 
	 * @param c the character
	 * @return true if a new token was completed, false otherwise
	 * @throws JsonParseException if the JSON content is invalid
	 */
	private boolean parseObjectKey(char c) throws JsonParseException {
		if (Character.isWhitespace(c)) {
			consumeCharacter(c);
			return false;
		} else if (c == '"') {
			tokenStartLine = documentLine;
			tokenStartLinePos = documentLinePos;
			consumeCharacter(c);
			inObjectKey = true;
			if (isStringAtomic) {
				parsedString = new StringBuilder();
				return false;
			} else {
				currentToken = new JsonAtomicToken(
						JsonAtomicToken.Type.START_STRING);
				return true;
			}
		} else {
			throw new JsonParseException(
					"Expected start of object key, found: " + c, documentLine,
					documentLinePos);
		}
	}
	
	/**
	 * Parses a character when the reader is positioned after an object key.
	 * See {@link #parseValueInitial(char) parseValueInitial()} for more
	 * information about the result.
	 * 
	 * @param c the character
	 * @return true if a new token was completed, false otherwise
	 * @throws JsonParseException if the JSON content is invalid
	 */
	private boolean parseAfterObjectKey(char c) throws JsonParseException {
		if (Character.isWhitespace(c)) {
			consumeCharacter(c);
			return false;
		} else if (c == ':') {
			tokenStartLine = documentLine;
			tokenStartLinePos = documentLinePos;
			consumeCharacter(c);
			currentToken = new JsonAtomicToken(
					JsonAtomicToken.Type.OBJECT_KEY_VALUE_SEPARATOR);
			inObjectKey = false;
			return true;
		} else {
			throw new JsonParseException(
					"Invalid character after object key: " + c,
					documentLine, documentLinePos);
		}
	}
	
	/**
	 * Parses a character when the reader is positioned after the start of a
	 * list ([). See {@link #parseValueInitial(char) parseValueInitial()} for
	 * more information about the result.
	 * 
	 * @param c the character
	 * @return true if a new token was completed, false otherwise
	 * @throws JsonParseException if the JSON content is invalid
	 */
	private boolean parseStartList(char c) throws JsonParseException {
		if (Character.isWhitespace(c)) {
			consumeCharacter(c);
			return false;
		} else if (c == ']') {
			tokenStartLine = documentLine;
			tokenStartLinePos = documentLinePos;
			consumeCharacter(c);
			currentToken = new JsonAtomicToken(
					JsonAtomicToken.Type.END_LIST);
			objectListStack.remove(objectListStack.size() - 1);
			return true;
		} else {
			return parseValueInitial(c);
		}
	}
	
	/**
	 * Parses a character when the reader is positioned in a string. See {@link
	 * #parseValueInitial(char) parseValueInitial()} for more information about
	 * the result.
	 * 
	 * @param c the character
	 * @return true if a new token was completed, false otherwise
	 * @throws JsonParseException if the JSON content is invalid
	 */
	private boolean parseStringChar(char c) throws JsonParseException {
		if (stringEscape != null && stringEscape.length() == 1) {
			char escapedChar;
			switch (c) {
			case '"':
			case '\\':
			case '/':
				escapedChar = c;
				break;
			case 'b':
				escapedChar = '\b';
				break;
			case 'f':
				escapedChar = '\f';
				break;
			case 'n':
				escapedChar = '\n';
				break;
			case 'r':
				escapedChar = '\r';
				break;
			case 't':
				escapedChar = '\t';
				break;
			case 'u':
				stringEscape.append(c);
				consumeCharacter(c);
				return false;
			default:
				throw new JsonParseException(
						"Invalid string escape sequence: " + stringEscape + c,
						documentLine, documentLinePos);
			}
			consumeCharacter(c);
			stringEscape = null;
			if (isStringAtomic) {
				parsedString.append(escapedChar);
				return false;
			} else {
				currentToken = new JsonAtomicToken(
						JsonAtomicToken.Type.STRING_CHARACTER,
						Character.toString(escapedChar));
				return true;
			}
		} else if (stringEscape != null) {
			// stringEscape starts with \\u
			String charStr = Character.toString(c);
			if (!charStr.matches("[0-9A-Fa-f]")) {
				throw new JsonParseException(
						"Invalid string escape sequence: " + stringEscape + c,
						documentLine, documentLinePos);
			}
			consumeCharacter(c);
			stringEscape.append(c);
			if (stringEscape.length() < 6)
				return false;
			String hex = stringEscape.substring(2);
			int codePoint = Integer.parseInt(hex, 16);
			char[] cs = Character.toChars(codePoint);
			stringEscape = null;
			if (isStringAtomic) {
				parsedString.append(cs);
				return false;
			} else {
				currentToken = new JsonAtomicToken(
						JsonAtomicToken.Type.STRING_CHARACTER, new String(cs));
				return true;
			}
		} else {
			if (c == '"') {
				if (!isStringAtomic) {
					tokenStartLine = documentLine;
					tokenStartLinePos = documentLinePos;
				}
				consumeCharacter(c);
				if (isStringAtomic) {
					currentToken = new JsonAtomicToken(
							JsonAtomicToken.Type.STRING,
							parsedString.toString());
					parsedString = null;
				} else {
					currentToken = new JsonAtomicToken(
							JsonAtomicToken.Type.END_STRING);
				}
				return true;
			} else if (c == '\\') {
				if (!isStringAtomic) {
					tokenStartLine = documentLine;
					tokenStartLinePos = documentLinePos;
				}
				consumeCharacter(c);
				stringEscape = new StringBuilder();
				stringEscape.append(c);
				return false;
			} else if (!Character.isISOControl(c)) {
				if (!isStringAtomic) {
					tokenStartLine = documentLine;
					tokenStartLinePos = documentLinePos;
				}
				consumeCharacter(c);
				if (isStringAtomic) {
					parsedString.append(c);
					return false;
				} else {
					currentToken = new JsonAtomicToken(
							JsonAtomicToken.Type.STRING_CHARACTER,
							Character.toString(c));
					return true;
				}
			} else {
				throw new JsonParseException(String.format(
						"Control character not allowed: 0x%02x", (int)c),
						documentLine, documentLinePos);
			}
		}
	}
	
	/**
	 * Parses a character when the reader is positioned after a JSON value.
	 * This can be after a list item, an object value, or the root value of
	 * the document. See {@link #parseValueInitial(char) parseValueInitial()}
	 * for more information about the result.
	 * 
	 * @param c the character
	 * @return true if a new token was completed, false otherwise
	 * @throws JsonParseException if the JSON content is invalid
	 */
	private boolean parseAfterValue(char c) throws JsonParseException {
		if (Character.isWhitespace(c)) {
			consumeCharacter(c);
			return false;
		}
		if (objectListStack.isEmpty()) {
			throw new JsonParseException(
					"Unxpected character after root value: " + c, documentLine,
					documentLinePos);
		}
		ObjectList currObjectList = objectListStack.get(
				objectListStack.size() - 1);
		if (currObjectList == ObjectList.LIST) {
			if (c == ',') {
				tokenStartLine = documentLine;
				tokenStartLinePos = documentLinePos;
				consumeCharacter(c);
				currentToken = new JsonAtomicToken(
						JsonAtomicToken.Type.LIST_ITEM_SEPARATOR);
				return true;
			} else if (c == ']') {
				tokenStartLine = documentLine;
				tokenStartLinePos = documentLinePos;
				consumeCharacter(c);
				currentToken = new JsonAtomicToken(
						JsonAtomicToken.Type.END_LIST);
				objectListStack.remove(objectListStack.size() - 1);
				return true;
			} else {
				throw new JsonParseException(
						"Invalid character after list item: " + c,
						documentLine, documentLinePos);
			}
		} else {
			if (c == ',') {
				tokenStartLine = documentLine;
				tokenStartLinePos = documentLinePos;
				consumeCharacter(c);
				currentToken = new JsonAtomicToken(
						JsonAtomicToken.Type.OBJECT_PAIR_SEPARATOR);
				return true;
			} else if (c == '}') {
				tokenStartLine = documentLine;
				tokenStartLinePos = documentLinePos;
				consumeCharacter(c);
				currentToken = new JsonAtomicToken(
						JsonAtomicToken.Type.END_OBJECT);
				objectListStack.remove(objectListStack.size() - 1);
				return true;
			} else {
				throw new JsonParseException(
						"Invalid character after key/value pair in object: " +
						c, documentLine, documentLinePos);
			}
		}
	}
	
	/**
	 * Parses a character when the reader is positioned in a boolean value. See
	 * {@link #parseValueInitial(char) parseValueInitial()} for more
	 * information about the result.
	 * 
	 * @param c the character
	 * @return true if a new token was completed, false otherwise
	 * @throws JsonParseException if the JSON content is invalid
	 */
	private boolean parseBooleanChar(char c) throws JsonParseException {
		String expected = parsedBoolean.charAt(0) == 't' ?
				"true" : "false";
		char expectedChar = expected.charAt(parsedBoolean.length());
		if (c != expectedChar) {
			throw new JsonParseException("Invalid token: " +
					parsedBoolean + c, documentLine, documentLinePos);
		}
		parsedBoolean.append(c);
		consumeCharacter(c);
		if (parsedBoolean.length() == expected.length()) {
			parsedBoolean = null;
			currentToken = new JsonAtomicToken(
					JsonAtomicToken.Type.BOOLEAN, expected.equals("true"));
			return true;
		}
		return false;
	}
	
	/**
	 * Parses a character when the reader is positioned in a null value. See
	 * {@link #parseValueInitial(char) parseValueInitial()} for more
	 * information about the result.
	 * 
	 * @param c the character
	 * @return true if a new token was completed, false otherwise
	 * @throws JsonParseException if the JSON content is invalid
	 */
	private boolean parseNullChar(char c) throws JsonParseException {
		String expected = "null";
		char expectedChar = expected.charAt(parsedNull.length());
		if (c != expectedChar) {
			throw new JsonParseException("Invalid token: " +
					parsedNull + c, documentLine, documentLinePos);
		}
		parsedNull.append(c);
		consumeCharacter(c);
		if (parsedNull.length() == expected.length()) {
			parsedNull = null;
			currentToken = new JsonAtomicToken(JsonAtomicToken.Type.NULL);
			return true;
		}
		return false;
	}
	
	/**
	 * Position in a number token.
	 */
	private enum NumberPos {
		START,
		AFTER_MAIN_SIGN,
		AFTER_MAIN,
		IN_MAIN,
		AFTER_POINT,
		IN_FRACTION,
		AFTER_EXP,
		AFTER_EXP_SIGN,
		IN_EXP_NUMBER
	}
	
	/**
	 * Tries to parse a character in a number token. If the reader is
	 * positioned before a token, this method is called when the specified
	 * character does not match any other token type. It may not be a valid
	 * start character for a number. If the reader is positioned in a number
	 * token and the specified character is not accepted, it will decide
	 * whether the previous characters constitute a complete number token (see
	 * {@link #finishNumber() finishNumber()}. In that case the specified
	 * character is not consumed. See {@link #parseValueInitial(char)
	 * parseValueInitial()} for more information about the result of this
	 * method.
	 * 
	 * @param c the character
	 * @return true if a new token was completed, false otherwise
	 * @throws JsonParseException if the JSON content is invalid
	 */
	private boolean parseNumberChar(char c) throws JsonParseException {
		if (numberPos == null)
			numberPos = NumberPos.START;
		switch (numberPos) {
		case START:
			if (c == '-') {
				consumeCharacter(c);
				parsedNumber = new StringBuilder();
				parsedNumber.append(c);
				numberPos = NumberPos.AFTER_MAIN_SIGN;
				return false;
			} else if (c == '0') {
				consumeCharacter(c);
				parsedNumber = new StringBuilder();
				parsedNumber.append(c);
				numberPos = NumberPos.AFTER_MAIN;
				return false;
			} else if (c >= '1' && c <= '9') {
				consumeCharacter(c);
				parsedNumber = new StringBuilder();
				parsedNumber.append(c);
				numberPos = NumberPos.IN_MAIN;
				return false;
			} else {
				throw new JsonParseException(
						"Invalid character at start of value: " + c,
						documentLine, documentLinePos);
			}
		case AFTER_MAIN_SIGN:
			if (c == '0') {
				consumeCharacter(c);
				parsedNumber.append(c);
				numberPos = NumberPos.AFTER_MAIN;
				return false;
			} else if (c >= '1' && c <= '9') {
				consumeCharacter(c);
				parsedNumber.append(c);
				numberPos = NumberPos.IN_MAIN;
				return false;
			} else {
				throw new JsonParseException("Invalid number: " +
						parsedNumber + c, documentLine, documentLinePos);
			}
		case AFTER_MAIN:
			if (c == '.') {
				consumeCharacter(c);
				parsedNumber.append(c);
				numberPos = NumberPos.AFTER_POINT;
				return false;
			} else if (c == 'e' || c == 'E') {
				consumeCharacter(c);
				parsedNumber.append(c);
				numberPos = NumberPos.AFTER_EXP;
				return false;
			} else {
				finishNumber();
				return true;
			}
		case IN_MAIN:
			if (c >= '0' && c <= '9') {
				consumeCharacter(c);
				parsedNumber.append(c);
				return false;
			} else if (c == '.') {
				consumeCharacter(c);
				parsedNumber.append(c);
				numberPos = NumberPos.AFTER_POINT;
				return false;
			} else if (c == 'e' || c == 'E') {
				consumeCharacter(c);
				parsedNumber.append(c);
				numberPos = NumberPos.AFTER_EXP;
				return false;
			} else {
				finishNumber();
				return true;
			}
		case AFTER_POINT:
			if (c >= '0' && c <= '9') {
				consumeCharacter(c);
				parsedNumber.append(c);
				numberPos = NumberPos.IN_FRACTION;
				return false;
			} else {
				throw new JsonParseException("Invalid number: " +
						parsedNumber + c, documentLine, documentLinePos);
			}
		case IN_FRACTION:
			if (c >= '0' && c <= '9') {
				consumeCharacter(c);
				parsedNumber.append(c);
				return false;
			} else if (c == 'e' || c == 'E') {
				consumeCharacter(c);
				parsedNumber.append(c);
				numberPos = NumberPos.AFTER_EXP;
				return false;
			} else {
				finishNumber();
				return true;
			}
		case AFTER_EXP:
			if (c == '+' || c == '-') {
				consumeCharacter(c);
				parsedNumber.append(c);
				numberPos = NumberPos.AFTER_EXP_SIGN;
				return false;
			} else if (c >= '0' && c <= '9') {
				consumeCharacter(c);
				parsedNumber.append(c);
				numberPos = NumberPos.IN_EXP_NUMBER;
				return false;
			} else {
				throw new JsonParseException("Invalid number: " +
						parsedNumber + c, documentLine, documentLinePos);
			}
		case AFTER_EXP_SIGN:
			if (c >= '0' && c <= '9') {
				consumeCharacter(c);
				parsedNumber.append(c);
				numberPos = NumberPos.IN_EXP_NUMBER;
				return false;
			} else {
				throw new JsonParseException("Invalid number: " +
						parsedNumber + c, documentLine, documentLinePos);
			}
		case IN_EXP_NUMBER:
			if (c >= '0' && c <= '9') {
				consumeCharacter(c);
				parsedNumber.append(c);
				numberPos = NumberPos.IN_EXP_NUMBER;
				return false;
			} else {
				finishNumber();
				return true;
			}
		}
		throw new RuntimeException("Unknown number position: " + numberPos);
	}
	
	/**
	 * Called when the reader is positioned in a number token and the next
	 * character is not accepted as part of the number token, or the end of
	 * the document is reached. This method decides whether the previous
	 * characters constitute a complete number token. If so, it sets
	 * currentToken and resets the number token parser. Otherwise it throws an
	 * exception.
	 * 
	 * @throws JsonParseException if the number token is incomplete
	 */
	private void finishNumber() throws JsonParseException {
		List<NumberPos> endStates = Arrays.asList(NumberPos.AFTER_MAIN,
				NumberPos.IN_MAIN, NumberPos.IN_FRACTION,
				NumberPos.IN_EXP_NUMBER);
		if (!endStates.contains(numberPos)) {
			throw new JsonParseException("Invalid number: " + parsedNumber,
					documentLine, documentLinePos);
		}
		Number value;
		if (numberPos == NumberPos.AFTER_MAIN ||
				numberPos == NumberPos.IN_MAIN) {
			long longVal = Long.parseLong(parsedNumber.toString());
			if (longVal >= Integer.MIN_VALUE && longVal <= Integer.MAX_VALUE)
				value = (int)longVal;
			else
				value = longVal;
		} else {
			value = Double.parseDouble(parsedNumber.toString());
		}
		currentToken = new JsonAtomicToken(JsonAtomicToken.Type.NUMBER, value);
		parsedNumber = null;
		numberPos = null;
	}
	
	/**
	 * Called when the end of the stream is reached. If the reader is in a
	 * number token it checks whether the token constitutes a complete number
	 * token. If so, this method sets currentToken and returns true. The
	 * document may still be incomplete. If the number is not complete, this
	 * method throws an exception.
	 * 
	 * <p>If the reader is not in a number token, this method checks whether
	 * the document is complete. If so it sets currentToken to null and returns
	 * false, meaning there are no more tokens. Otherwise it throws an
	 * exception.</p>
	 * 
	 * @return true if the document was in a number token and currentToken was
	 * set to a number token (the document may still be incomplete), false if
	 * the document is complete and there are no more tokens
	 * @throws JsonParseException if the document is incomplete
	 */
	private boolean finishStream() throws JsonParseException {
		if (parsedString != null) {
			throw new JsonParseException(
					"Incomplete string at end of document", documentLine,
					documentLinePos);
		}
		if (parsedNumber != null) {
			finishNumber();
			return true;
		}
		if (parsedBoolean != null) {
			throw new JsonParseException(
					"Incomplete token at end of document: " + parsedBoolean,
					documentLine, documentLinePos);
		}
		if (parsedNull != null) {
			throw new JsonParseException(
					"Incomplete token at end of document: " + parsedNull,
					documentLine, documentLinePos);
		}
		if (!objectListStack.isEmpty()) {
			ObjectList item = objectListStack.get(objectListStack.size() - 1);
			if (item == ObjectList.LIST) {
				throw new JsonParseException(
						"Incomplete list at end of document", documentLine,
						documentLinePos);
			} else {
				throw new JsonParseException(
						"Incomplete object at end of document", documentLine,
						documentLinePos);
			}
		}
		if (currentToken == null) {
			throw new JsonParseException("Empty document", documentLine,
					documentLinePos);
		}
		if (currentToken.getType() == JsonAtomicToken.Type.START_STRING ||
				currentToken.getType() ==
				JsonAtomicToken.Type.STRING_CHARACTER) {
			throw new JsonParseException(
					"Incomplete string at end of document", documentLine,
					documentLinePos);
		}
		tokenStartLine = documentLine;
		tokenStartLinePos = documentLinePos;
		currentToken = null;
		return false;
	}
	
	/**
	 * Consumes the current character in the buffer. It moves the buffer
	 * position one character forward and it updates documentLine and
	 * documentLinePos, based on whether the character starts a new line.
	 * 
	 * @param c the character
	 */
	private void consumeCharacter(char c) {
		bufferLen--;
		if (bufferLen == 0)
			bufferPos = 0;
		else
			bufferPos++;
		if (c == '\r') {
			documentLine++;
			documentLinePos = 1;
			consumedCR = true;
		} else if (c == '\n') {
			if (consumedCR) {
				consumedCR = false;
			} else {
				documentLine++;
				documentLinePos = 1;
			}
		} else {
			if (consumedCR) {
				consumedCR = false;
			} else {
				documentLinePos++;
			}
		}
	}
	
	/**
	 * Object or list.
	 */
	private enum ObjectList {
		OBJECT,
		LIST
	}
}
