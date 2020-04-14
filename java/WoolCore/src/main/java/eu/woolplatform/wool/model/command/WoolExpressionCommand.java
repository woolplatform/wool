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

import eu.woolplatform.utils.CurrentIterator;
import eu.woolplatform.utils.exception.LineNumberParseException;
import eu.woolplatform.utils.expressions.Expression;
import eu.woolplatform.utils.expressions.ExpressionParser;
import eu.woolplatform.utils.expressions.Token;
import eu.woolplatform.utils.expressions.Tokenizer;
import eu.woolplatform.utils.io.LineColumnNumberReader;
import eu.woolplatform.wool.parser.WoolBodyToken;

import java.io.IOException;
import java.io.StringReader;

public abstract class WoolExpressionCommand extends WoolCommand {

	/**
	 * Reads the content of a command as a code string. When this method
	 * returns, the iterator will be positioned after the command end token.
	 * 
	 * @param cmdStartToken the command start token
	 * @param tokens the token iterator positioned after the command start token
	 * @return the content
	 * @throws LineNumberParseException if a parsing error occurs
	 */
	protected static ReadContentResult readCommandContent(
			WoolBodyToken cmdStartToken, CurrentIterator<WoolBodyToken> tokens)
			throws LineNumberParseException {
		ReadContentResult result = new ReadContentResult();
		result.lineNum = tokens.getCurrent().getLineNum();
		result.colNum = tokens.getCurrent().getColNum();
		StringBuilder text = new StringBuilder();
		boolean foundEnd = false;
		while (!foundEnd && tokens.getCurrent() != null) {
			WoolBodyToken token = tokens.getCurrent();
			if (token.getType() == WoolBodyToken.Type.COMMAND_END) {
				foundEnd = true;
			} else {
				text.append(tokens.getCurrent().getText());
			}
			tokens.moveNext();
		}
		if (!foundEnd) {
			throw new LineNumberParseException("Command not terminated",
					cmdStartToken.getLineNum(), cmdStartToken.getColNum());
		}
		result.content = text.toString();
		return result;
	}
	
	protected static class ReadContentResult {
		public String content;
		public int lineNum;
		public int colNum;
	}
	
	/**
	 * Parses the specified command content. This method checks whether the
	 * command name is the specified name, and there is no expression.
	 * 
	 * @param cmdStartToken the command start token
	 * @param content the command content
	 * @param name the command name
	 * @return the parsed content
	 * @throws LineNumberParseException if a parsing error occurs
	 */
	protected static ParseContentResult parseCommandContentName(
			WoolBodyToken cmdStartToken, ReadContentResult content, String name)
			throws LineNumberParseException {
		ParseContentResult result = parseCommandContent(cmdStartToken, content);
		if (!result.name.equals(name)) {
			throw new LineNumberParseException(String.format(
					"Expected command \"%s\", found: %s", name, result.name),
					cmdStartToken.getLineNum(), cmdStartToken.getColNum());
		}
		if (result.expression != null) {
			throw new LineNumberParseException(String.format(
					"Unexpected content after command name \"%s\"", name),
					cmdStartToken.getLineNum(), cmdStartToken.getColNum());
		}
		return result;
	}

	/**
	 * Parses the specified command content. This method checks whether the
	 * command name is the specified name, and there is an expression.
	 * 
	 * @param cmdStartToken the command start token
	 * @param content the command content
	 * @param name the command name
	 * @return the parsed content
	 * @throws LineNumberParseException if a parsing error occurs
	 */
	protected static ParseContentResult parseCommandContentExpression(
			WoolBodyToken cmdStartToken, ReadContentResult content, String name)
			throws LineNumberParseException {
		ParseContentResult result = parseCommandContent(cmdStartToken, content);
		if (!result.name.equals(name)) {
			throw new LineNumberParseException(String.format(
					"Expected command \"%s\", found: %s", name, result.name),
					cmdStartToken.getLineNum(), cmdStartToken.getColNum());
		}
		if (result.expression == null) {
			throw new LineNumberParseException(String.format(
					"Expression not found in command \"%s\"", name),
					cmdStartToken.getLineNum(), cmdStartToken.getColNum());
		}
		return result;
	}
	
	/**
	 * Parses the specified command content. It tries to read a command name and
	 * an expression. If there is no expression, then the expression in the
	 * result will be null.
	 * 
	 * @param cmdStartToken the command start token
	 * @param content the command content
	 * @return the parsed content
	 * @throws LineNumberParseException if a parsing error occurs
	 */
	protected static ParseContentResult parseCommandContent(
			WoolBodyToken cmdStartToken, ReadContentResult content)
			throws LineNumberParseException {
		int lineOff = content.lineNum;
		int colOff = content.colNum;
		LineColumnNumberReader reader = new LineColumnNumberReader(
				new StringReader(content.content));
		Tokenizer tokenizer = new Tokenizer(reader);
		ExpressionParser parser = new ExpressionParser(tokenizer);
		try {
			try {
				parser.getConfig().setAllowDollarVariables(true);
				parser.getConfig().setAllowPlainVariables(false);
				return parseCommandContent(cmdStartToken, content, tokenizer,
						parser, lineOff, colOff);
			} finally {
				parser.close();
			}
		} catch (IOException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

	private static ParseContentResult parseCommandContent(
			WoolBodyToken cmdStartToken, ReadContentResult content,
			Tokenizer tokenizer, ExpressionParser parser, int lineOff,
			int colOff) throws LineNumberParseException, IOException {
		ParseContentResult result = new ParseContentResult();
		Token nameToken;
		try {
			nameToken = tokenizer.readToken();
		} catch (LineNumberParseException ex) {
			throw createParseException("Invalid command name: " +
					ex.getError(), ex, lineOff, colOff);
		}
		if (nameToken == null) {
			throw new LineNumberParseException("Found empty command",
					cmdStartToken.getLineNum(), cmdStartToken.getColNum());
		}
		if (nameToken.getType() != Token.Type.NAME) {
			throw createParseException("Expected command name, found token: " +
					nameToken.getType(), nameToken.getLineNum(),
					nameToken.getColNum(), lineOff, colOff);
		}
		result.name = nameToken.getValue().toString();
		try {
			result.expression = parser.readExpression();
		} catch (LineNumberParseException ex) {
			throw createParseException("Invalid expression in command: " +
					ex.getError(), ex, lineOff, colOff);
		}
		int postExprLine = tokenizer.getLineNum();
		int postExprCol = tokenizer.getColNum();
		Token nextToken;
		try {
			nextToken = tokenizer.readToken();
		} catch (LineNumberParseException ex) {
			throw createParseException(
					"Unexpected content after expression in command",
					postExprLine, postExprCol, lineOff, colOff);
		}
		if (nextToken != null) {
			throw createParseException(
					"Unexpected content after expression in command",
					postExprLine, postExprCol, lineOff, colOff);
		}
		return result;
	}

	private static LineNumberParseException createParseException(String message,
			LineNumberParseException ex, int lineOff, int colOff)
			throws LineNumberParseException {
		return createParseException(message, ex.getLineNum(), ex.getColNum(),
				lineOff, colOff);
	}

	private static LineNumberParseException createParseException(String message,
			int lineNum, int colNum, int lineOff, int colOff)
			throws LineNumberParseException {
		int exLineNum = lineOff - 1 + lineNum;
		int exColNum = colNum;
		if (exLineNum == lineOff)
			exColNum += colOff - 1;
		return new LineNumberParseException(message, exLineNum, exColNum);
	}

	protected static class ParseContentResult {
		public String name;
		public Expression expression;
	}
}
