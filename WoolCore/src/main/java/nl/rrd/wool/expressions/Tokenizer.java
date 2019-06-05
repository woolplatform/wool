package nl.rrd.wool.expressions;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nl.rrd.wool.exception.LineNumberParseException;
import nl.rrd.wool.expressions.Token.Type;
import nl.rrd.wool.io.LineColumnNumberReader;

/**
 * A tokenizer can read tokens from text input. It is used by the {@link
 * ExpressionParser ExpressionParser}. A token is an atomic element of an
 * expression.
 * 
 * <p>After construction, you can read tokens with {@link #readToken()
 * readToken()}. If you construct the tokenizer with a {@link
 * LineColumnNumberReader LineColumnNumberReader}, then you can rewind the input
 * after each call of {@link #readToken() readToken()}, regardless of whether
 * it returns a token or null or a parse error. Rewinding can be useful if
 * the tokens are embedded inside other text input.</p>
 * 
 * @author Dennis Hofs (RRD)
 */
public class Tokenizer {
	private LineColumnNumberReader reader;
	
	private int currTokenLineNum;
	private int currTokenColNum;
	private long currTokenPos;
	private List<NameOrFixedToken> fixedTokens = new ArrayList<>();
	private StringBuilder buffer = new StringBuilder();

	private List<NameOrFixedToken> candidateNameOrFixedTokens;
	
	private StringBuilder stringValue;
	private StringBuilder stringEscape = null;
	
	private NumberPos numberPos = null;
	
	private Object lookAheadState = null;
	
	public Tokenizer(String input) {
		this(new StringReader(input));
	}
	
	public Tokenizer(Reader reader) {
		this(new LineColumnNumberReader(reader));
	}
	
	public Tokenizer(LineColumnNumberReader reader) {
		this.reader = reader;
		fixedTokens.add(new NameOrFixedToken("=", Token.Type.ASSIGN));
		fixedTokens.add(new NameOrFixedToken("||", Token.Type.OR));
		fixedTokens.add(new NameOrFixedToken("&&", Token.Type.AND));
		fixedTokens.add(new NameOrFixedToken("!", Token.Type.NOT));
		fixedTokens.add(new NameOrFixedToken("in", Token.Type.IN));
		fixedTokens.add(new NameOrFixedToken("<", Token.Type.LESS_THAN));
		fixedTokens.add(new NameOrFixedToken("<=", Token.Type.LESS_EQUAL));
		fixedTokens.add(new NameOrFixedToken("==", Token.Type.EQUAL));
		fixedTokens.add(new NameOrFixedToken("!=", Token.Type.NOT_EQUAL));
		fixedTokens.add(new NameOrFixedToken(">=", Token.Type.GREATER_EQUAL));
		fixedTokens.add(new NameOrFixedToken(">", Token.Type.GREATER_THAN));
		fixedTokens.add(new NameOrFixedToken("+", Token.Type.ADD));
		fixedTokens.add(new NameOrFixedToken("-", Token.Type.SUBTRACT));
		fixedTokens.add(new NameOrFixedToken("/", Token.Type.DIVIDE));
		fixedTokens.add(new NameOrFixedToken(".", Token.Type.DOT));
		fixedTokens.add(new NameOrFixedToken("*", Token.Type.MULTIPLY));
		fixedTokens.add(new NameOrFixedToken("[", Token.Type.BRACKET_OPEN));
		fixedTokens.add(new NameOrFixedToken("]", Token.Type.BRACKET_CLOSE));
		fixedTokens.add(new NameOrFixedToken("(", Token.Type.PARENTHESIS_OPEN));
		fixedTokens.add(new NameOrFixedToken(")", Token.Type.PARENTHESIS_CLOSE));
		fixedTokens.add(new NameOrFixedToken("{", Token.Type.BRACE_OPEN));
		fixedTokens.add(new NameOrFixedToken("}", Token.Type.BRACE_CLOSE));
		fixedTokens.add(new NameOrFixedToken(",", Token.Type.COMMA));
		fixedTokens.add(new NameOrFixedToken(":", Token.Type.COLON));
		fixedTokens.add(new NameOrFixedToken("true", Token.Type.BOOLEAN));
		fixedTokens.add(new NameOrFixedToken("false", Token.Type.BOOLEAN));
		fixedTokens.add(new NameOrFixedToken("null", Token.Type.NULL));
	}
	
	/**
	 * Returns the reader used by this tokenizer.
	 * 
	 * @return the reader
	 */
	public LineColumnNumberReader getReader() {
		return reader;
	}
	
	/**
	 * Closes the tokenizer and the input reader.
	 * 
	 * @throws IOException if the reader can't be closed
	 */
	public void close() throws IOException {
		reader.close();
	}
	
	/**
	 * Tries to read the next token. If the end of input is reached, this method
	 * returns null.
	 * 
	 * @return the token or null
	 * @throws LineNumberParseException if a parse error occurs
	 * @throws IOException if a reading error occurs
	 */
	public Token readToken() throws LineNumberParseException, IOException {
		if (lookAheadState != null)
			reader.clearRestoreState(lookAheadState);
		lookAheadState = reader.getRestoreState();
		skipWhitespace();
		currTokenLineNum = reader.getLineNum();
		currTokenColNum = reader.getColNum();
		currTokenPos = reader.getPosition();
		Object restoreState = reader.getRestoreState(1);
		try {
			int next = reader.read();
			if (next == -1)
				return null;
			char c = (char)next;
			candidateNameOrFixedTokens = getCandidateNameOrFixedTokens(c);
			if (!candidateNameOrFixedTokens.isEmpty()) {
				buffer.append(c);
				return readNameOrFixedToken();
			} else if (c == '"') {
				buffer.append(c);
				return readStringToken();
			} else if (c == '-' || (c >= '0' && c <= '9')) {
				reader.restoreState(restoreState);
				return readNumberToken();
			} else if (c == '$') {
				buffer.append(c);
				return readDollarVariableToken();
			} else {
				buffer.append(c);
				throw createParseException("Invalid character: " + c,
						currTokenLineNum, currTokenColNum);
			}
		} finally {
			reader.clearRestoreState(restoreState);
		}
	}
	
	/**
	 * Returns the current line number.
	 * 
	 * @return the current line number
	 */
	public int getLineNum() {
		return reader.getLineNum();
	}
	
	/**
	 * Returns the current column number.
	 * 
	 * @return the current column number
	 */
	public int getColNum() {
		return reader.getColNum();
	}
	
	/**
	 * Rewinds the {@link LineColumnNumberReader LineColumnNumberReader} so it
	 * is reset to the same position as before the last call of {@link
	 * #readToken() readToken()}.
	 * 
	 * @throws IOException if an I/O error occurs
	 */
	public void rewind() throws IOException {
		if (lookAheadState == null) {
			throw new IOException(
					"Rewind can only be executed once after readToken()");
		}
		reader.restoreState(lookAheadState);
		lookAheadState = null;
	}
	
	private List<NameOrFixedToken> getCandidateNameOrFixedTokens(char c) {
		List<NameOrFixedToken> result = new ArrayList<>();
		String s = buffer.toString() + c;
		for (NameOrFixedToken fixedToken : fixedTokens) {
			if (fixedToken.text.startsWith(s))
				result.add(fixedToken);
		}
		if (s.matches("[_a-zA-Z][_a-zA-Z0-9]*")) {
			result.add(new NameOrFixedToken(null, Type.NAME));
		}
		return result;
	}
	
	/**
	 * Called when the start character of a name or fixed token has been read.
	 * It will read the rest of the token and return it.
	 * 
	 * <p>This method tries to complete a token when the end of input is
	 * reached, or when a character is found that can't be added to the token.
	 * If the token can't be completed at that point, then this method throws
	 * a parse exception.</p>
	 * 
	 * @return the token
	 * @throws LineNumberParseException if a parse error occurs
	 * @throws IOException if a reading error occurs
	 */
	private Token readNameOrFixedToken() throws LineNumberParseException,
			IOException {
		NameOrFixedToken completeFixedToken = findCompleteFixedToken();
		if (candidateNameOrFixedTokens.size() == 1 &&
				completeFixedToken != null) {
			return completeNameOrFixedToken(completeFixedToken);
		}
		while (true) {
			Object restoreState = reader.getRestoreState(1);
			try {
				int next = reader.read();
				if (next == -1)
					return tryCompleteNameOrFixedToken();
				NameOrFixedToken completeToken = findCompleteNameOrFixedToken();
				char c = (char)next;
				List<NameOrFixedToken> remainTokens =
						getCandidateNameOrFixedTokens(c);
				if (!remainTokens.isEmpty()) {
					candidateNameOrFixedTokens = remainTokens;
					buffer.append(c);
				} else if (completeToken != null) {
					reader.restoreState(restoreState);
					return completeNameOrFixedToken(completeToken);
				} else {
					buffer.append(c);
					throw createParseException("Invalid token: " + buffer,
							currTokenLineNum, currTokenColNum);
				}
				completeFixedToken = findCompleteFixedToken();
				if (candidateNameOrFixedTokens.size() == 1 &&
						completeFixedToken != null) {
					return completeNameOrFixedToken(completeFixedToken);
				}
			} finally {
				reader.clearRestoreState(restoreState);
			}
		}
	}
	
	private NameOrFixedToken findCompleteFixedToken() {
		for (NameOrFixedToken token : candidateNameOrFixedTokens) {
			if (token.token != Token.Type.NAME &&
					buffer.length() == token.text.length()) {
				return token;
			}
		}
		return null;
	}

	private NameOrFixedToken findCompleteNameOrFixedToken() {
		NameOrFixedToken fixedToken = findCompleteFixedToken();
		if (fixedToken != null)
			return fixedToken;
		for (NameOrFixedToken token : candidateNameOrFixedTokens) {
			if (token.token == Token.Type.NAME)
				return token;
		}
		return null;
	}
	
	private Token tryCompleteNameOrFixedToken()
			throws LineNumberParseException {
		String text = buffer.toString();
		NameOrFixedToken completeToken = findCompleteNameOrFixedToken();
		if (completeToken == null) {
			throw createParseException("Invalid token: " + text,
					currTokenLineNum, currTokenColNum);
		}
		return completeNameOrFixedToken(completeToken);
	}
	
	private Token completeNameOrFixedToken(NameOrFixedToken completeToken) {
		Token.Type type = completeToken.token;
		String text = buffer.toString();
		Token result;
		if (type == Token.Type.NAME) {
			result = new Token(type, text, currTokenLineNum, currTokenColNum,
					currTokenPos, new Value(text));
		} else {
			Value value = null;
			if (type == Token.Type.BOOLEAN)
				value = new Value(text.equals("true"));
			else if (type == Token.Type.NULL)
				value = new Value(null);
			result = new Token(type, text, currTokenLineNum, currTokenColNum,
					currTokenPos, value);
		}
		buffer = new StringBuilder();
		candidateNameOrFixedTokens = null;
		return result;
	}
	
	/**
	 * Called when a $ character has been read. It will read the rest of the
	 * token and return it.
	 * 
	 * <p>This method tries to complete a token when the end of input is
	 * reached, or when a character is found that can't be added to the token.
	 * If the token can't be completed at that point, then this method throws
	 * a parse exception.</p>
	 * 
	 * @return the token
	 * @throws LineNumberParseException if a parse error occurs
	 * @throws IOException if a reading error occurs
	 */
	private Token readDollarVariableToken() throws LineNumberParseException,
			IOException {
		Pattern regex = Pattern.compile("\\$[_a-zA-Z][_a-zA-Z0-9]*");
		while (true) {
			Object restoreState = reader.getRestoreState(1);
			try {
				Matcher m = regex.matcher(buffer);
				boolean currentOk = m.matches();
				int next = reader.read();
				if (next == -1) {
					if (!currentOk) {
						throw createParseException("Invalid token: " + buffer,
								currTokenLineNum, currTokenColNum);
					}
					return completeDollarVariableToken();
				}
				char c = (char)next;
				m = regex.matcher(buffer.toString() + c);
				boolean newOk = m.matches();
				if (newOk) {
					buffer.append(c);
				} else if (currentOk) {
					reader.restoreState(restoreState);
					return completeDollarVariableToken();
				} else {
					throw createParseException("Invalid token: " + buffer,
							currTokenLineNum, currTokenColNum);
				}
			} finally {
				reader.clearRestoreState(restoreState);
			}
		}
	}
	
	private Token completeDollarVariableToken() {
		Token result = new Token(Token.Type.DOLLAR_VARIABLE, buffer.toString(),
				currTokenLineNum, currTokenColNum, currTokenPos,
				new Value(buffer.substring(1)));
		buffer = new StringBuilder();
		return result;
	}
	
	/**
	 * Called when the start of a string token (") has been read. It will read
	 * the rest of the token and return it.
	 * 
	 * @return the token
	 * @throws LineNumberParseException if a parse error occurs
	 * @throws IOException if a reading error occurs
	 */
	private Token readStringToken() throws LineNumberParseException,
			IOException {
		if (stringValue == null)
			stringValue = new StringBuilder();
		while (true) {
			if (stringEscape != null)
				readStringEscape();
			int charLineNum = reader.getLineNum();
			int charColNum = reader.getColNum();
			char special = 0;
			while (special == 0) {
				int next = reader.read();
				if (next == -1) {
					throw createParseException("Incomplete string",
							currTokenLineNum, currTokenColNum);
				}
				char c = (char)next;
				buffer.append(c);
				if (Character.isISOControl(c)) {
					throw createParseException(
							"Found control character in string", charLineNum,
							charColNum);
				} else if (c == '"' || c == '\\') {
					special = c;
					break;
				} else {
					charColNum++;
					stringValue.append(c);
				}
			}
			if (special == '"') {
				Token result = new Token(Token.Type.STRING, buffer.toString(),
						currTokenLineNum, currTokenColNum, currTokenPos,
						new Value(stringValue.toString()));
				buffer = new StringBuilder();
				stringValue = null;
				return result;
			} else {
				stringEscape = new StringBuilder();
			}
		}
	}
	
	private void readStringEscape() throws LineNumberParseException,
			IOException {
		while (true) {
			int charLineNum = reader.getLineNum();
			int charColNum = reader.getColNum();
			int next = reader.read();
			if (next == -1)
				return;
			char c = (char)next;
			buffer.append(c);
			if (stringEscape.length() == 0) {
				switch (c) {
					case '"':
					case '\\':
					case '/':
						stringValue.append(c);
						stringEscape = null;
						return;
					case 'b':
						stringValue.append('\b');
						stringEscape = null;
						return;
					case 'f':
						stringValue.append('\f');
						stringEscape = null;
						return;
					case 'n':
						stringValue.append('\n');
						stringEscape = null;
						return;
					case 'r':
						stringValue.append('\r');
						stringEscape = null;
						return;
					case 't':
						stringValue.append('\t');
						stringEscape = null;
						return;
					case 'u':
						stringEscape.append(c);
						break;
					default:
						throw createParseException(
								"Invalid string escape character: " + c,
								charLineNum, charColNum);
				}
			} else if (stringEscape.length() < 5) {
				if (!Character.toString(c).matches("[A-Fa-f0-9]")) {
					throw createParseException(
							"Invalid hexadecimal character in unicode escape: " +
							c, charLineNum, charColNum);
				}
				stringEscape.append(c);
				if (stringEscape.length() == 5) {
					int codePoint = Integer.parseInt(stringEscape.substring(1),
							16);
					char[] unicode = Character.toChars(codePoint);
					stringValue.append(unicode);
					stringEscape = null;
					return;
				}
			}
		}
	}
	
	/**
	 * Called when the reader is before a number token. It will read the number
	 * token and return it.
	 * 
	 * <p>This method tries to complete a number token when the end of input is
	 * reached, or when a character is found that can't be added to the number
	 * token. If the token can't be completed at that point, then this method
	 * throws a parse exception.</p>
	 * 
	 * @return the token
	 * @throws LineNumberParseException if a parse error occurs
	 * @throws IOException if a reading error occurs
	 */
	private Token readNumberToken() throws LineNumberParseException,
			IOException {
		while (true) {
			Object restoreState = reader.getRestoreState(1);
			try {
				int next = reader.read();
				if (next == -1)
					return tryCompleteNumber();
				char c = (char)next;
				Token token = parseNumberChar(c);
				if (token != null) {
					reader.restoreState(restoreState);
					return token;
				}
				buffer.append(c);
			} finally {
				reader.clearRestoreState(restoreState);
			}
		}
	}

	/**
	 * Position in a number token.
	 */
	private enum NumberPos {
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
	 * Tries to parse the next character as part of a number token. If the
	 * character is accepted, this method returns null.
	 * Otherwise it will try to complete the number token with the current
	 * buffer. If it fails, it throws a parse exception. Otherwise it returns
	 * the token.
	 * 
	 * @param c the character
	 * @return the token or null
	 * @throws LineNumberParseException if the character is not accepted and
	 * the number token can't be completed
	 */
	private Token parseNumberChar(char c) throws LineNumberParseException {
		if (numberPos == null) {
			if (c == '-') {
				numberPos = NumberPos.AFTER_MAIN_SIGN;
				return null;
			} else if (c == '0') {
				numberPos = NumberPos.AFTER_MAIN;
				return null;
			} else {
				numberPos = NumberPos.IN_MAIN;
				return null;
			}
		}
		switch (numberPos) {
			case AFTER_MAIN_SIGN:
				if (c == '0') {
					numberPos = NumberPos.AFTER_MAIN;
					return null;
				} else if (c >= '1' && c <= '9') {
					numberPos = NumberPos.IN_MAIN;
					return null;
				} else {
					throw createParseException("Invalid number: " +
							buffer + c, currTokenLineNum, currTokenColNum);
				}
			case AFTER_MAIN:
				if (c == '.') {
					numberPos = NumberPos.AFTER_POINT;
					return null;
				} else if (c == 'e' || c == 'E') {
					numberPos = NumberPos.AFTER_EXP;
					return null;
				} else {
					return tryCompleteNumber();
				}
			case IN_MAIN:
				if (c >= '0' && c <= '9') {
					return null;
				} else if (c == '.') {
					numberPos = NumberPos.AFTER_POINT;
					return null;
				} else if (c == 'e' || c == 'E') {
					numberPos = NumberPos.AFTER_EXP;
					return null;
				} else {
					return tryCompleteNumber();
				}
			case AFTER_POINT:
				if (c >= '0' && c <= '9') {
					numberPos = NumberPos.IN_FRACTION;
					return null;
				} else {
					throw createParseException("Invalid number: " +
							buffer + c, currTokenLineNum, currTokenColNum);
				}
			case IN_FRACTION:
				if (c >= '0' && c <= '9') {
					return null;
				} else if (c == 'e' || c == 'E') {
					numberPos = NumberPos.AFTER_EXP;
					return null;
				} else {
					return tryCompleteNumber();
				}
			case AFTER_EXP:
				if (c == '+' || c == '-') {
					numberPos = NumberPos.AFTER_EXP_SIGN;
					return null;
				} else if (c >= '0' && c <= '9') {
					numberPos = NumberPos.IN_EXP_NUMBER;
					return null;
				} else {
					throw createParseException("Invalid number: " +
							buffer + c, currTokenLineNum, currTokenColNum);
				}
			case AFTER_EXP_SIGN:
				if (c >= '0' && c <= '9') {
					numberPos = NumberPos.IN_EXP_NUMBER;
					return null;
				} else {
					throw createParseException("Invalid number: " +
							buffer + c, currTokenLineNum, currTokenColNum);
				}
			case IN_EXP_NUMBER:
				if (c >= '0' && c <= '9') {
					numberPos = NumberPos.IN_EXP_NUMBER;
					return null;
				} else {
					return tryCompleteNumber();
				}
		}
		throw new RuntimeException("Unknown number position: " + numberPos);
	}
	
	private Token tryCompleteNumber() throws LineNumberParseException {
		List<NumberPos> endStates = Arrays.asList(NumberPos.AFTER_MAIN,
				NumberPos.IN_MAIN, NumberPos.IN_FRACTION,
				NumberPos.IN_EXP_NUMBER);
		if (!endStates.contains(numberPos)) {
			throw createParseException("Invalid number: " + buffer,
					currTokenLineNum, currTokenColNum);
		}
		Number value;
		if (numberPos == NumberPos.AFTER_MAIN ||
				numberPos == NumberPos.IN_MAIN) {
			long longVal = Long.parseLong(buffer.toString());
			if (longVal >= Integer.MIN_VALUE && longVal <= Integer.MAX_VALUE)
				value = (int)longVal;
			else
				value = longVal;
		} else {
			value = Double.parseDouble(buffer.toString());
		}
		Token result = new Token(Token.Type.NUMBER, buffer.toString(),
				currTokenLineNum, currTokenColNum, currTokenPos,
				new Value(value));
		buffer = new StringBuilder();
		numberPos = null;
		return result;
	}
	
	private void skipWhitespace() throws IOException {
		while (true) {
			Object restoreState = reader.getRestoreState(1);
			try {
				int next = reader.read();
				if (next == -1)
					return;
				char c = (char)next;
				if (!Character.isWhitespace(c)) {
					reader.restoreState(restoreState);
					return;
				}
			} finally {
				reader.clearRestoreState(restoreState);
			}
		}
	}
	
	private LineNumberParseException createParseException(String message,
			int lineNum, int colNum) {
		return new LineNumberParseException(message, lineNum, colNum);
	}
	
	private class NameOrFixedToken {
		public String text;
		public Token.Type token;
		
		public NameOrFixedToken(String text, Token.Type token) {
			this.text = text;
			this.token = token;
		}
	}
}
