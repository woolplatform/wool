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

package nl.rrd.wool.expressions;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import nl.rrd.wool.exception.LineNumberParseException;
import nl.rrd.wool.exception.ParseException;
import nl.rrd.wool.expressions.types.AddExpression;
import nl.rrd.wool.expressions.types.AndExpression;
import nl.rrd.wool.expressions.types.AssignExpression;
import nl.rrd.wool.expressions.types.DivideExpression;
import nl.rrd.wool.expressions.types.DotExpression;
import nl.rrd.wool.expressions.types.EqualExpression;
import nl.rrd.wool.expressions.types.GreaterEqualExpression;
import nl.rrd.wool.expressions.types.GreaterThanExpression;
import nl.rrd.wool.expressions.types.GroupExpression;
import nl.rrd.wool.expressions.types.InExpression;
import nl.rrd.wool.expressions.types.IndexExpression;
import nl.rrd.wool.expressions.types.LessEqualExpression;
import nl.rrd.wool.expressions.types.LessThanExpression;
import nl.rrd.wool.expressions.types.ListExpression;
import nl.rrd.wool.expressions.types.MultiplyExpression;
import nl.rrd.wool.expressions.types.NotEqualExpression;
import nl.rrd.wool.expressions.types.NotExpression;
import nl.rrd.wool.expressions.types.ObjectExpression;
import nl.rrd.wool.expressions.types.OrExpression;
import nl.rrd.wool.expressions.types.SubtractExpression;
import nl.rrd.wool.expressions.types.ValueExpression;
import nl.rrd.wool.io.LineColumnNumberReader;

/**
 * An expression parser can read {@link Expression Expression}s from text input.
 * After construction, you can read expressions with {@link #readExpression()
 * readExpression()}. If you construct the parser with a {@link
 * LineColumnNumberReader LineColumnNumberReader}, then you can rewind the input
 * after each call of {@link #readExpression() readExpression()}, regardless of
 * whether it returns a token or null or a parse error. Rewinding can be useful
 * if an expression is embedded inside other text input.
 * 
 * @author Dennis Hofs (RRD)
 */
public class ExpressionParser {
	private ExpressionParserConfig config = new ExpressionParserConfig();
	private LineColumnNumberReader reader;
	private Tokenizer tokenizer;
	
	private Object lookAheadState = null;

	public static final Token.Type[][] PRECEDENCE = new Token.Type[][] {
		new Token.Type[] { Token.Type.ASSIGN },
		new Token.Type[] { Token.Type.OR },
		new Token.Type[] { Token.Type.AND },
		new Token.Type[] { Token.Type.IN },
		new Token.Type[] { Token.Type.EQUAL, Token.Type.NOT_EQUAL },
		new Token.Type[] { Token.Type.LESS_THAN, Token.Type.LESS_EQUAL,
				Token.Type.GREATER_EQUAL, Token.Type.GREATER_THAN },
		new Token.Type[] { Token.Type.ADD, Token.Type.SUBTRACT },
		new Token.Type[] { Token.Type.MULTIPLY, Token.Type.DIVIDE },
	};
	
	public ExpressionParser(String input) {
		this(new StringReader(input));
	}
	
	public ExpressionParser(Reader reader) {
		this(new LineColumnNumberReader(reader));
	}
	
	public ExpressionParser(LineColumnNumberReader reader) {
		this(new Tokenizer(reader));
	}
	
	public ExpressionParser(Tokenizer tokenizer) {
		this.tokenizer = tokenizer;
		this.reader = tokenizer.getReader();
	}
	
	/**
	 * Closes the parser and the input reader.
	 * 
	 * @throws IOException if the reader can't be closed
	 */
	public void close() throws IOException {
		tokenizer.close();
	}
	
	public ExpressionParserConfig getConfig() {
		return config;
	}

	public void setConfig(ExpressionParserConfig config) {
		this.config = config;
	}

	/**
	 * Tries to read the next expression. If the end of input is reached, this
	 * method returns null.
	 * 
	 * @return the expression or null
	 * @throws LineNumberParseException if a parse error occurs
	 * @throws IOException if a reading error occurs
	 */
	public Expression readExpression() throws LineNumberParseException,
			IOException {
		if (lookAheadState != null)
			reader.clearRestoreState(lookAheadState);
		lookAheadState = reader.getRestoreState();
		return doReadExpression(false);
	}
	
	/**
	 * Tries to read an operand. That is an expression with any operators. If
	 * the end of input is reached, this method returns null.
	 * 
	 * @return the operand or null
	 * @throws LineNumberParseException if a parse error occurs
	 * @throws IOException if a reading error occurs
	 */
	public Expression readOperand() throws LineNumberParseException,
			IOException {
		if (lookAheadState != null)
			reader.clearRestoreState(lookAheadState);
		lookAheadState = reader.getRestoreState();
		return doReadOperand(false);
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
	 * #readExpression() readExpression()}.
	 * 
	 * @throws IOException if an I/O error occurs
	 */
	public void rewind() throws IOException {
		if (lookAheadState == null) {
			throw new IOException(
					"Rewind can only be executed once after read");
		}
		reader.restoreState(lookAheadState);
		lookAheadState = null;
	}
	
	private Expression doReadExpression(boolean require)
			throws LineNumberParseException, IOException {
		Token token = tokenizer.readToken();
		if (token == null) {
			if (!require) {
				return null;
			} else {
				throw createParseException("Unexpected end of expression",
						tokenizer.getLineNum(), tokenizer.getColNum());
			}
		}
		Object lastCompleteState = null;
		List<ExpressionElement> elements = new ArrayList<>();
		Expression afterOperand = null;
		boolean foundEnd = false;
		try {
			while (token != null && !foundEnd) {
				if (afterOperand == null) {
					tokenizer.rewind();
					afterOperand = doReadOperand(true);
					if (lastCompleteState != null)
						reader.clearRestoreState(lastCompleteState);
					lastCompleteState = reader.getRestoreState();
					elements.add(new ExpressionElement(afterOperand));
					token = tokenizer.readToken();
					if (token == null)
						break;
				}
				switch (token.getType()) {
					// infix operators
					case ASSIGN:
					case OR:
					case AND:
					case IN:
					case LESS_THAN:
					case LESS_EQUAL:
					case EQUAL:
					case NOT_EQUAL:
					case GREATER_EQUAL:
					case GREATER_THAN:
					case ADD:
					case SUBTRACT:
					case MULTIPLY:
					case DIVIDE:
						elements.add(new ExpressionElement(token));
						afterOperand = null;
						token = tokenizer.readToken();
						break;
						
					// postfix operator
					case DOT:
					case BRACKET_OPEN:
						tokenizer.rewind();
						afterOperand = readPostfixOperator(token.getType(),
								afterOperand);
						elements.remove(elements.size() - 1);
						elements.add(new ExpressionElement(afterOperand));
						if (lastCompleteState != null)
							reader.clearRestoreState(lastCompleteState);
						lastCompleteState = reader.getRestoreState();
						token = tokenizer.readToken();
						break;
	
					case NOT:
					case BRACKET_CLOSE:
					case PARENTHESIS_OPEN:
					case PARENTHESIS_CLOSE:
					case BRACE_OPEN:
					case BRACE_CLOSE:
					case COMMA:
					case COLON:
					case STRING:
					case BOOLEAN:
					case NUMBER:
					case NULL:
					case NAME:
					case DOLLAR_VARIABLE:
						foundEnd = true;
						break;
				}
			}
		} catch (LineNumberParseException ex) {
			if (lastCompleteState == null)
				throw ex;
		}
		if (lastCompleteState != null)
			reader.restoreState(lastCompleteState);
		ExpressionElement lastElem = elements.get(elements.size() - 1);
		if (lastElem.operand == null)
			elements.remove(elements.size() - 1);
		return mergeExpressionElements(elements, 0, elements.size());
	}
	
	private Expression mergeExpressionElements(List<ExpressionElement> elements,
			int start, int end) throws LineNumberParseException {
		if (end - start == 1) {
			return elements.get(start).operand;
		}
		for (Token.Type[] level : PRECEDENCE) {
			int op1 = findOperator(elements, start, end, level);
			if (op1 == -1)
				continue;
			Expression merged = null;
			while (op1 != -1) {
				Token operator = elements.get(op1).operator;
				int op2 = findOperator(elements, op1 + 1, end, level);
				Expression operand1;
				if (merged != null)
					operand1 = merged;
				else
					operand1 = mergeExpressionElements(elements, start, op1);
				Expression operand2 = mergeExpressionElements(elements,
						op1 + 1, op2 == -1 ? end : op2);
				merged = createOperatorExpression(operator, operand1, operand2);
				op1 = op2;
			}
			return merged;
		}
		throw new RuntimeException("No operator found");
	}
	
	private int findOperator(List<ExpressionElement> elements, int start,
			int end, Token.Type... types) {
		for (int i = start + 1; i < end; i += 2) {
			ExpressionElement elem = elements.get(i);
			for (Token.Type type : types) {
				if (elem.operator.getType() == type)
					return i;
			}
		}
		return -1;
	}
	
	private Expression createOperatorExpression(Token operator,
			Expression operand1, Expression operand2)
			throws LineNumberParseException {
		switch (operator.getType()) {
			case ASSIGN:
				return new AssignExpression(operand1, operator, operand2);
			case OR:
				return new OrExpression(operand1, operand2);
			case AND:
				return new AndExpression(operand1, operand2);
			case IN:
				return new InExpression(operand1, operand2);
			case LESS_THAN:
				return new LessThanExpression(operand1, operand2);
			case LESS_EQUAL:
				return new LessEqualExpression(operand1, operand2);
			case EQUAL:
				return new EqualExpression(operand1, operand2);
			case NOT_EQUAL:
				return new NotEqualExpression(operand1, operand2);
			case GREATER_EQUAL:
				return new GreaterEqualExpression(operand1, operand2);
			case GREATER_THAN:
				return new GreaterThanExpression(operand1, operand2);
			case ADD:
				return new AddExpression(operand1, operand2);
			case SUBTRACT:
				return new SubtractExpression(operand1, operand2);
			case MULTIPLY:
				return new MultiplyExpression(operand1, operand2);
			case DIVIDE:
				return new DivideExpression(operand1, operand2);
			default:
				throw new RuntimeException("Unknown operator");
		}
	}
	
	private Expression doReadOperand(boolean require)
			throws LineNumberParseException, IOException {
		return doReadOperand(require, false);
	}
	
	private Expression doReadOperand(boolean require, boolean overrideAllowName)
			throws LineNumberParseException, IOException {
		Token token = tokenizer.readToken();
		if (token == null) {
			if (!require) {
				return null;
			} else {
				throw createParseException("Unexpected end of expression",
						tokenizer.getLineNum(), tokenizer.getColNum());
			}
		}
		switch (token.getType()) {
			case ASSIGN:
			case OR:
			case AND:
			case IN:
			case LESS_THAN:
			case LESS_EQUAL:
			case EQUAL:
			case NOT_EQUAL:
			case GREATER_EQUAL:
			case GREATER_THAN:
			case ADD:
			case MULTIPLY:
			case DIVIDE:
			case DOT:
			case BRACKET_CLOSE:
			case PARENTHESIS_CLOSE:
			case BRACE_CLOSE:
			case COMMA:
			case COLON:
				throw createParseException(
						"Invalid token at start of expression: " +
						token.getText(), token);
	
			case NOT:
				tokenizer.rewind();
				return readNot();
			case SUBTRACT:
				tokenizer.rewind();
				return readNegativeNumber();
			case BRACKET_OPEN:
				tokenizer.rewind();
				return readList();
			case PARENTHESIS_OPEN:
				tokenizer.rewind();
				return readGroup();
			case BRACE_OPEN:
				tokenizer.rewind();
				return readObject();
			case STRING:
				try {
					return new StringExpression(token.getValue().toString());
				} catch (ParseException ex) {
					throw new LineNumberParseException(
							"Invalid expression in string: " + token.getText() +
							": " + ex.getMessage(), token.getLineNum(),
							token.getColNum(), ex);
				}
			case BOOLEAN:
			case NUMBER:
			case NULL:
				return new ValueExpression(token);
			case NAME:
				if (overrideAllowName || config.isAllowPlainVariables()) {
					return new ValueExpression(token);
				} else {
					throw createParseException(
							"Variable token without $ not allowed: " +
							token.getText(), token);
				}
			case DOLLAR_VARIABLE:
				if (config.isAllowDollarVariables()) {
					return new ValueExpression(token);
				} else {
					throw createParseException(
							"Variable token with $ not allowed: " +
							token.getText(), token);
				}
		}
		throw new RuntimeException("Unknown token type: " + token.getType());
	}
	
	private Expression readNot() throws LineNumberParseException, IOException {
		Token token = tokenizer.readToken();
		if (token == null) {
			throw createParseException("Expected '!', found end of expression",
					tokenizer.getLineNum(), tokenizer.getColNum());
		}
		if (token.getType() != Token.Type.NOT) {
			throw createParseException("Expected '!', found: " + token.getText(),
					token);
		}
		Expression operand = doReadOperand(true);
		return new NotExpression(operand);
	}
	
	private Expression readNegativeNumber() throws LineNumberParseException,
			IOException {
		Token subtractToken = tokenizer.readToken();
		if (subtractToken == null) {
			throw createParseException("Expected '-', found end of expression",
					tokenizer.getLineNum(), tokenizer.getColNum());
		}
		if (subtractToken.getType() != Token.Type.SUBTRACT) {
			throw createParseException("Expected '-', found: " +
					subtractToken.getText(), subtractToken);
		}
		Token numToken = tokenizer.readToken();
		if (numToken == null) {
			throw createParseException(
					"Expected number, found end of expression",
					tokenizer.getLineNum(), tokenizer.getColNum());
		}
		if (numToken.getType() != Token.Type.NUMBER) {
			throw createParseException("Expected number, found: " +
					numToken.getText(), numToken);
		}
		Number num;
		try {
			num = numToken.getValue().asNumber();
		} catch (EvaluationException ex) {
			throw new RuntimeException("Unexpected error: " + ex.getMessage(),
					ex);
		}
		Number negNum;
		if (Value.isIntNumber(num))
			negNum = Value.normalizeNumber(-num.longValue());
		else
			negNum = -num.doubleValue();
		Token token = new Token(Token.Type.NUMBER, "-" + numToken.getText(),
				subtractToken.getLineNum(), subtractToken.getColNum(),
				subtractToken.getPosition(), new Value(negNum));
		return new ValueExpression(token);
	}
	
	private Expression readPostfixOperator(Token.Type token,
			Expression parentOperand) throws LineNumberParseException,
			IOException {
		switch (token) {
			case DOT:
				return readDot(parentOperand);
			case BRACKET_OPEN:
				return readIndex(parentOperand);
			default:
				throw new RuntimeException("Unknown postfix operator: " +
						token);
		}
	}
	
	private Expression readDot(Expression parentOperand)
			throws LineNumberParseException, IOException {
		Token token = tokenizer.readToken();
		if (token == null) {
			throw createParseException("Expected '.', found end of expression",
					tokenizer.getLineNum(), tokenizer.getColNum());
		}
		if (token.getType() != Token.Type.DOT) {
			throw createParseException("Expected '.', found: " +
					token.getText(), token);
		}
		Expression parsed = doReadOperand(true, true);
		return new DotExpression(parentOperand, parsed);
	}
	
	private Expression readIndex(Expression parentOperand)
			throws LineNumberParseException, IOException {
		Token token = tokenizer.readToken();
		if (token == null) {
			throw createParseException("Expected '[', found end of expression",
					tokenizer.getLineNum(), tokenizer.getColNum());
		}
		if (token.getType() != Token.Type.BRACKET_OPEN) {
			throw createParseException("Expected '[', found: " +
					token.getText(), token);
		}
		Expression parsed = doReadExpression(true);
		token = tokenizer.readToken();
		if (token == null) {
			throw createParseException("Incomplete index expression",
					tokenizer.getLineNum(), tokenizer.getColNum());
		}
		if (token.getType() != Token.Type.BRACKET_CLOSE) {
			throw createParseException("Expected ']', found: " +
					token.getText(), token);
		}
		return new IndexExpression(parentOperand, parsed);
	}
	
	private Expression readList() throws LineNumberParseException, IOException {
		Token token = tokenizer.readToken();
		if (token == null) {
			throw createParseException("Expected '[', found end of expression",
					tokenizer.getLineNum(), tokenizer.getColNum());
		}
		if (token.getType() != Token.Type.BRACKET_OPEN) {
			throw createParseException("Expected '[', found: " + token.getText(),
					token);
		}
		boolean prevIsComma = false;
		List<Expression> elements = new ArrayList<>();
		while ((token = tokenizer.readToken()) != null) {
			if (prevIsComma) {
				tokenizer.rewind();
				Expression parsed = doReadExpression(true);
				elements.add(parsed);
				prevIsComma = false;
			} else if (token.getType() == Token.Type.COMMA) {
				if (elements.isEmpty()) {
					throw createParseException(
							"Expected expression or ']', found ','", token);
				}
				prevIsComma = true;
			} else if (token.getType() == Token.Type.BRACKET_CLOSE) {
				return new ListExpression(elements);
			} else {
				if (!elements.isEmpty()) {
					throw createParseException("Expected ',' or ']', found: " +
							token.getText(), token);
				}
				tokenizer.rewind();
				Expression parsed = doReadExpression(true);
				elements.add(parsed);
			}
		}
		throw createParseException("Incomplete list", tokenizer.getLineNum(),
				tokenizer.getColNum());
	}
	
	private Expression readGroup() throws LineNumberParseException,
			IOException {
		Token token = tokenizer.readToken();
		if (token == null) {
			throw createParseException("Expected '(', found end of expression",
					tokenizer.getLineNum(), tokenizer.getColNum());
		}
		if (token.getType() != Token.Type.PARENTHESIS_OPEN) {
			throw createParseException("Expected '(', found: " +
					token.getText(), token);
		}
		Expression expression = doReadExpression(true);
		token = tokenizer.readToken();
		if (token == null) {
			throw createParseException("Incomplete group",
					tokenizer.getLineNum(), tokenizer.getColNum());
		}
		if (token.getType() != Token.Type.PARENTHESIS_CLOSE) {
			throw createParseException("Expected ')', found: " +
					token.getText(), token);
		}
		return new GroupExpression(expression);
	}
	
	private Expression readObject() throws LineNumberParseException,
			IOException {
		Token token = tokenizer.readToken();
		if (token == null) {
			throw createParseException("Expected '{', found end of expression",
					tokenizer.getLineNum(), tokenizer.getColNum());
		}
		if (token.getType() != Token.Type.BRACE_OPEN) {
			throw createParseException("Expected '{', found: " + token.getText(),
					token);
		}
		List<ObjectExpression.KeyValue> properties = new ArrayList<>();
		Expression currentKey = null;
		boolean prevIsComma = false;
		boolean prevIsColon = false;
		while ((token = tokenizer.readToken()) != null) {
			if (prevIsColon) {
				tokenizer.rewind();
				Expression parsed = doReadExpression(true);
				properties.add(new ObjectExpression.KeyValue(currentKey,
						parsed));
				prevIsColon = false;
				currentKey = null;
			} else if (prevIsComma) {
				tokenizer.rewind();
				currentKey = doReadExpression(true);
				prevIsComma = false;
			} else if (currentKey != null) {
				if (token.getType() != Token.Type.COLON) {
					throw createParseException("Expected ':', found: " +
							token.getText(), token);
				}
				prevIsColon = true;
			} else if (token.getType() == Token.Type.COLON) {
				if (properties.isEmpty()) {
					throw createParseException(
							"Expected expression or '}', found ':'", token);
				} else {
					throw createParseException(
							"Expected ',' or '}', found ':'", token);
				}
			} else if (token.getType() == Token.Type.COMMA) {
				if (properties.isEmpty()) {
					throw createParseException(
							"Expected expression or '}', found ','", token);
				}
				prevIsComma = true;
			} else if (token.getType() == Token.Type.BRACE_CLOSE) {
				return new ObjectExpression(properties);
			} else {
				if (!properties.isEmpty()) {
					throw createParseException("Expected ',' or '}', found: " +
							token.getText(), token);
				}
				tokenizer.rewind();
				currentKey = doReadExpression(true);
			}
		}
		throw createParseException("Incomplete object", tokenizer.getLineNum(),
				tokenizer.getColNum());
	}
	
	private class ExpressionElement {
		public Expression operand = null;
		public Token operator = null;
		
		public ExpressionElement(Expression operand) {
			this.operand = operand;
		}
		
		public ExpressionElement(Token operator) {
			this.operator = operator;
		}
	}
	
	private LineNumberParseException createParseException(String message,
			Token token) {
		return new LineNumberParseException(message, token.getLineNum(),
				token.getColNum());
	}
	
	private LineNumberParseException createParseException(String message,
			int lineNum, int colNum) {
		return new LineNumberParseException(message, lineNum, colNum);
	}
}
