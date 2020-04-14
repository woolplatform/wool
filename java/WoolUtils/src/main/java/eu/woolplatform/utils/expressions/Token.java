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

import eu.woolplatform.utils.json.JsonObject;

public class Token {
	public enum Type {
		// operator tokens
		ASSIGN,
		OR,
		AND,
		NOT,
		IN,
		LESS_THAN,
		LESS_EQUAL,
		EQUAL,
		NOT_EQUAL,
		STRICT_EQUAL,
		NOT_STRICT_EQUAL,
		GREATER_EQUAL,
		GREATER_THAN,
		ADD,
		SUBTRACT,
		MULTIPLY,
		DIVIDE,
		DOT,
		
		// group tokens
		BRACKET_OPEN,
		BRACKET_CLOSE,
		PARENTHESIS_OPEN,
		PARENTHESIS_CLOSE,
		BRACE_OPEN,
		BRACE_CLOSE,
		COMMA,
		COLON,

		// atom tokens
		STRING,
		BOOLEAN,
		NUMBER,
		NULL,
		NAME,
		DOLLAR_VARIABLE
	}
	
	private Type type;
	private String text;
	private int lineNum;
	private int colNum;
	private long position;
	private Value value;
	
	public Token(Type type, String text, int lineNum, int colNum, long position,
			Value value) {
		this.type = type;
		this.text = text;
		this.lineNum = lineNum;
		this.colNum = colNum;
		this.position = position;
		this.value = value;
	}

	public Type getType() {
		return type;
	}

	public String getText() {
		return text;
	}

	public int getLineNum() {
		return lineNum;
	}

	public int getColNum() {
		return colNum;
	}
	
	public long getPosition() {
		return position;
	}

	public Value getValue() {
		return value;
	}
	
	public String toString() {
		return JsonObject.toString(this);
	}
}
