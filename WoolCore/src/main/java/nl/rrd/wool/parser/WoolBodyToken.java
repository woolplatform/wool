package nl.rrd.wool.parser;

public class WoolBodyToken {
	public enum Type {
		/**
		 * Value: text with escaped characters resolved
		 */
		TEXT,
	
		COMMAND_START,
		COMMAND_END,
		REPLY_START,
		REPLY_END,
		REPLY_SEPARATOR,
		
		/**
		 * Value: WoolVariableString
		 */
		QUOTED_STRING,
		
		/**
		 * Value: variable name
		 */
		VARIABLE
	}

	private Type type;
	private int lineNum;
	private int colNum;
	private String text;
	private Object value = null;

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public int getLineNum() {
		return lineNum;
	}

	public void setLineNum(int lineNum) {
		this.lineNum = lineNum;
	}

	public int getColNum() {
		return colNum;
	}

	public void setColNum(int colNum) {
		this.colNum = colNum;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}
}
