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

package eu.woolplatform.wool.model.command;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import eu.woolplatform.utils.CurrentIterator;
import eu.woolplatform.utils.exception.LineNumberParseException;
import eu.woolplatform.wool.model.WoolVariableString;
import eu.woolplatform.wool.parser.WoolBodyToken;

public abstract class WoolAttributesCommand extends WoolCommand {

	/**
	 * Parses a command that is formatted like:<br />
	 * &lt;&lt;command attr1="value1" attr2="value2"&gt;&gt;
	 * 
	 * <p>It returns a map where the keys are attribute names and the values
	 * are tokens with type QUOTED_STRING.</p>
	 * 
	 * @param cmdStartToken the command start token
	 * @param tokens the token iterator, positioned after the command start
	 * token
	 * @return the attributes
	 * @throws LineNumberParseException if a parsing error occurs
	 */
	protected static Map<String,WoolBodyToken> parseAttributesCommand(
			WoolBodyToken cmdStartToken, CurrentIterator<WoolBodyToken> tokens)
			throws LineNumberParseException {
		Map<String,WoolBodyToken> result = new LinkedHashMap<>();
		boolean first = true;
		while (tokens.getCurrent() != null) {
			WoolBodyToken token = tokens.getCurrent();
			String text;
			if (first) {
				first = false;
				text = ((String)token.getValue()).trim();
				String[] split = text.split("\\s+", 2);
				if (split.length < 2) {
					tokens.moveNext();
					continue;
				}
				text = split[1];
			} else if (token.getType() == WoolBodyToken.Type.COMMAND_END) {
				tokens.moveNext();
				return result;
			} else if (token.getType() != WoolBodyToken.Type.TEXT) {
				throw new LineNumberParseException(
						"Expected attribute name, found token: " +
						token.getType(), token.getLineNum(), token.getColNum());
			} else {
				text = ((String)token.getValue()).trim();
			}
			// text is a trimmed string, length > 0
			// expect: attribute=
			int sep = text.indexOf('=');
			String attr;
			if (sep != -1)
				attr = text.substring(0, sep).trim();
			else
				attr = text;
			if (!attr.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
				throw new LineNumberParseException("Invalid attribute name: " +
						attr, token.getLineNum(), token.getColNum());
			}
			if (sep == -1) {
				throw new LineNumberParseException(
						"Character = not found after attribute name",
						token.getLineNum(), token.getColNum());
			}
			String post = text.substring(sep + 1).trim();
			if (!post.isEmpty()) {
				throw new LineNumberParseException("Unexpected text after =",
						token.getLineNum(), token.getColNum());
			}
			tokens.moveNext();
			WoolBodyToken.skipWhitespace(tokens);
			token = tokens.getCurrent();
			if (token == null) {
				throw new LineNumberParseException("Command not terminated",
						cmdStartToken.getLineNum(), cmdStartToken.getColNum());
			}
			if (token.getType() != WoolBodyToken.Type.QUOTED_STRING) {
				throw new LineNumberParseException(
						"Expected quoted string, found token: " +
						token.getType(), token.getLineNum(), token.getColNum());
			}
			result.put(attr, token);
			tokens.moveNext();
			WoolBodyToken.skipWhitespace(tokens);
		}
		throw new LineNumberParseException("Command not terminated",
				cmdStartToken.getLineNum(), cmdStartToken.getColNum());
	}
	
	protected static WoolVariableString readAttr(String name,
			Map<String,WoolBodyToken> attrs, WoolBodyToken cmdStartToken,
			boolean require) throws LineNumberParseException {
		if (!attrs.containsKey(name)) {
			if (!require)
				return null;
			throw new LineNumberParseException(String.format(
					"Required attribute \"%s\" not found", name),
					cmdStartToken.getLineNum(), cmdStartToken.getColNum());
		}
		WoolBodyToken token = attrs.get(name);
		return (WoolVariableString)token.getValue();
	}
	
	protected static String readPlainTextAttr(String name,
			Map<String,WoolBodyToken> attrs, WoolBodyToken cmdStartToken,
			boolean require) throws LineNumberParseException {
		WoolVariableString varStr = readAttr(name, attrs, cmdStartToken,
				require);
		if (varStr == null)
			return null;
		WoolBodyToken token = attrs.get(name);
		if (!varStr.isPlainText()) {
			throw new LineNumberParseException(String.format(
					"Value for attribute \"%s\" is not plain text", name) +
					": " + token.getText(), token.getLineNum(),
					token.getColNum());
		}
		return varStr.evaluate(null);
	}
	
	protected static String readVariableAttr(String name,
			Map<String,WoolBodyToken> attrs, WoolBodyToken cmdStartToken,
			boolean require) throws LineNumberParseException {
		WoolVariableString varStr = readAttr(name, attrs, cmdStartToken,
				require);
		if (varStr == null)
			return null;
		WoolBodyToken token = attrs.get(name);
		List<WoolVariableString.Segment> segments = varStr.getSegments();
		if (segments.size() != 1 || !(segments.get(0) instanceof
				WoolVariableString.VariableSegment)) {
			throw new LineNumberParseException(String.format(
					"Value for attribute \"%s\" is not a variable", name) +
					": " + token.getText(), token.getLineNum(),
					token.getColNum());
		}
		WoolVariableString.VariableSegment segment =
				(WoolVariableString.VariableSegment)segments.get(0);
		return segment.getVariableName();
	}
	
	protected static Integer readIntAttr(String name,
			Map<String,WoolBodyToken> attrs, WoolBodyToken cmdStartToken,
			boolean require, Integer min, Integer max)
			throws LineNumberParseException {
		String s = readPlainTextAttr(name, attrs, cmdStartToken, require);
		if (s == null)
			return null;
		WoolBodyToken token = attrs.get(name);
		int result;
		try {
			result = Integer.parseInt(s);
		} catch (NumberFormatException ex) {
			throw new LineNumberParseException(String.format(
					"Invalid value for attribute \"%s\"", name) + ": " + s,
					token.getLineNum(), token.getColNum());
		}
		if (min != null && result < min) {
			throw new LineNumberParseException(String.format(
					"Value for attribute \"%s\" < %s", name, min) + ": " +
					result, token.getLineNum(), token.getColNum());
		}
		if (max != null && result > max) {
			throw new LineNumberParseException(String.format(
					"Value for attribute \"%s\" > %s", name, max) + ": " +
					result, token.getLineNum(), token.getColNum());
		}
		return result;
	}

	protected static Float readFloatAttr(String name,
			Map<String,WoolBodyToken> attrs, WoolBodyToken cmdStartToken,
			boolean require, Float min, Float max)
			throws LineNumberParseException {
		String s = readPlainTextAttr(name, attrs, cmdStartToken, require);
		if (s == null)
			return null;
		WoolBodyToken token = attrs.get(name);
		float result;
		try {
			result = Float.parseFloat(s);
		} catch (NumberFormatException ex) {
			throw new LineNumberParseException(String.format(
					"Invalid value for attribute \"%s\"", name) + ": " + s,
					token.getLineNum(), token.getColNum());
		}
		if (min != null && result < min) {
			throw new LineNumberParseException(String.format(
					"Value for attribute \"%s\" < %s", name, min) + ": " +
					result, token.getLineNum(), token.getColNum());
		}
		if (max != null && result > max) {
			throw new LineNumberParseException(String.format(
					"Value for attribute \"%s\" > %s", name, max) + ": " +
					result, token.getLineNum(), token.getColNum());
		}
		return result;
	}

	protected static Boolean readBooleanAttr(String name,
		 Map<String,WoolBodyToken> attrs, WoolBodyToken cmdStartToken,
		 boolean require) throws LineNumberParseException {

		String s = readPlainTextAttr(name, attrs, cmdStartToken, require);
		if (s == null)
			return null;

		WoolBodyToken token = attrs.get(name);

		if(s.toLowerCase().equals("true") || s.toLowerCase().equals("false")) {
			return Boolean.parseBoolean(s.toLowerCase());
		} else {
			throw new LineNumberParseException(String.format(
					"Invalid value for attribute \"%s\" (please use \"true\" or \"false\"", name) + ": " + s,
					token.getLineNum(), token.getColNum());
		}

	}
}
