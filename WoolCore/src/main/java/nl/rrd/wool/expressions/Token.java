package nl.rrd.wool.expressions;

import nl.rrd.wool.json.JsonObject;

public class Token {
	public enum Type {
		// operator tokens
		OR,
		AND,
		NOT,
		IN,
		LESS_THAN,
		LESS_EQUAL,
		EQUAL,
		NOT_EQUAL,
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
		NAME
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
