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

package eu.woolplatform.utils.expressions;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.woolplatform.utils.exception.LineNumberParseException;
import eu.woolplatform.utils.exception.ParseException;
import eu.woolplatform.utils.io.LineColumnNumberReader;

public class StringExpression implements Expression {
	private List<Segment> segments;
	
	public StringExpression(String s) throws ParseException {
		segments = parse(new StringExpressionParser(s));
	}
	
	@Override
	public Value evaluate(Map<String,Object> variables)
			throws EvaluationException {
		StringBuilder result = new StringBuilder();
		for (Segment segment : segments) {
			if (segment instanceof LiteralSegment) {
				LiteralSegment literal = (LiteralSegment)segment;
				result.append(literal.string);
			} else {
				ExpressionSegment expr = (ExpressionSegment)segment;
				Value val = expr.expression.evaluate(variables);
				result.append(val.toString());
			}
		}
		return new Value(result.toString());
	}
	
	@Override
	public List<Expression> getChildren() {
		List<Expression> result = new ArrayList<>();
		for (Segment segment : segments) {
			if (segment instanceof ExpressionSegment) {
				ExpressionSegment expr = (ExpressionSegment)segment;
				result.add(expr.expression);
			}
		}
		return result;
	}

	@Override
	public List<Expression> getDescendants() {
		List<Expression> result = new ArrayList<>();
		for (Expression child : getChildren()) {
			result.add(child);
			result.addAll(child.getDescendants());
		}
		return result;
	}

	@Override
	public Set<String> getVariableNames() {
		Set<String> result = new HashSet<>();
		for (Expression child : getChildren()) {
			result.addAll(child.getVariableNames());
		}
		return result;
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		for (Segment segment : segments) {
			if (segment instanceof LiteralSegment) {
				LiteralSegment literal = (LiteralSegment)segment;
				result.append(literal.string);
			} else {
				ExpressionSegment expr = (ExpressionSegment)segment;
				result.append("${" + expr.expression.toString() + "}");
			}
		}
		return result.toString();
	}
	
	private class Segment {
	}
	
	private class LiteralSegment extends Segment {
		private String string;
		
		private LiteralSegment(String string) {
			this.string = string;
		}
	}
	
	private class ExpressionSegment extends Segment {
		private Expression expression;
		
		private ExpressionSegment(Expression expression) {
			this.expression = expression;
		}
	}
	
	private List<Segment> parse(StringExpressionParser parser)
			throws ParseException {
		while (parser.pos < parser.input.length) {
			char c = parser.input[parser.pos];
			if (parser.prevSpecialChar == 0) {
				switch (c) {
					case '\\':
					case '$':
						parser.prevSpecialChar = c;
						break;
				}
				parser.pos++;
			} else {
				switch (parser.prevSpecialChar) {
					case '\\':
						parseEscape(parser, c);
						break;
					case '$':
						parseDollar(parser, c);
						break;
				}
			}
		}
		completeCurrentSegment(parser, parser.pos, parser.pos);
		return parser.result;
	}
	
	private void parseEscape(StringExpressionParser parser, char c) {
		switch (c) {
			case '\\':
			case '$':
				parser.currSegment.append(parser.input, parser.currSegmentStart,
						parser.pos - 1);
				parser.currSegment.append(c);
				parser.pos++;
				parser.currSegmentStart = parser.pos;
				break;
			default:
				parser.pos++;
				break;
		}
		parser.prevSpecialChar = 0;
	}
	
	private void completeCurrentSegment(StringExpressionParser parser,
			int end, int nextStart) {
		if (parser.currSegmentStart < end) {
			parser.currSegment.append(parser.input, parser.currSegmentStart,
					end - parser.currSegmentStart);
		}
		if (parser.currSegment.length() > 0) {
			parser.result.add(new LiteralSegment(
					parser.currSegment.toString()));
		}
		parser.currSegment = new StringBuilder();
		parser.currSegmentStart = nextStart;
	}
	
	private void parseDollar(StringExpressionParser parser, char c)
			throws ParseException {
		parser.prevSpecialChar = 0;
		if (c != '{')
			return;
		int off = parser.pos + 1;
		int len = parser.input.length - off;
		StringReader stringReader = new StringReader(new String(
				parser.input, off, len));
		LineColumnNumberReader reader = new LineColumnNumberReader(
				stringReader);
		Tokenizer tokenizer = new Tokenizer(reader);
		ExpressionParser exprParser = new ExpressionParser(tokenizer);
		Expression expression;
		int endExpr;
		try {
			try {
				expression = parseExpression(parser, tokenizer, exprParser);
				endExpr = off + (int)reader.getPosition();
			} finally {
				exprParser.close();
			}
		} catch (LineNumberParseException ex) {
			throw new ParseException(String.format(
					"Invalid expression after ${ at index %s",
					parser.pos + 1) + ": " + ex.getMessage(), ex);
		} catch (IOException ex) {
			throw new RuntimeException("I/O exception in string reader: " +
					ex.getMessage(), ex);
		}
		completeCurrentSegment(parser, parser.pos - 1, endExpr);
		parser.result.add(new ExpressionSegment(expression));
		parser.pos = endExpr;
	}
	
	private Expression parseExpression(StringExpressionParser parser,
			Tokenizer tokenizer, ExpressionParser exprParser)
			throws ParseException, IOException {
		Expression expression;
		try {
			expression = exprParser.readExpression();
		} catch (LineNumberParseException ex) {
			throw new ParseException(String.format(
					"Invalid expression after ${ at index %s",
					parser.pos + 1) + ": " + ex.getMessage(), ex);
		}
		if (expression == null) {
			throw new ParseException(String.format(
					"Incomplete ${expression} sequence at index %s",
					parser.pos - 1));
		}
		Token token;
		try {
			token = tokenizer.readToken();
		} catch (LineNumberParseException ex) {
			throw new ParseException(String.format(
					"Invalid expression after ${ at index %s",
					parser.pos + 1) + ": " + ex.getMessage(), ex);
		}
		if (token == null) {
			throw new ParseException(String.format(
					"Incomplete ${expression} sequence at index %s",
					parser.pos - 1));
		}
		if (token.getType() != Token.Type.BRACE_CLOSE) {
			throw new ParseException(String.format(
					"Expected '}' at index %s, found: ",
					parser.pos + 1 + token.getPosition()) + token.getText());
		}
		return expression;
	}
	
	private class StringExpressionParser {
		private char[] input;
		
		private StringBuilder currSegment = new StringBuilder();
		private int currSegmentStart = 0;
		private int pos = 0;
		private char prevSpecialChar = 0;
		
		private List<Segment> result = new ArrayList<>();
		
		private StringExpressionParser(String s) {
			input = s.toCharArray();
		}
	}
}
