package nl.rrd.wool.exception;

/**
 * This exception indicates a parse error with a line and column number. The
 * line and column number are automatically added to the exception message at
 * construction.
 * 
 * @author Dennis Hofs (RRD)
 */
public class LineNumberParseException extends ParseException {
	private static final long serialVersionUID = 1L;
	
	private String error;
	private int lineNum;
	private int colNum;

	/**
	 * Constructs a new exception.
	 * 
	 * @param message the error message
	 * @param lineNum the line number (first line is 1)
	 * @param colNum the column or character number in the line (first character
	 * is 1)
	 */
	public LineNumberParseException(String message, int lineNum, int colNum) {
		this(message, lineNum, colNum, null);
	}

	/**
	 * Constructs a new exception.
	 * 
	 * @param message the error message
	 * @param lineNum the line number (first line is 1)
	 * @param colNum the column or character number in the line (first character
	 * is 1)
	 * @param cause a cause or null
	 */
	public LineNumberParseException(String message, int lineNum, int colNum,
			Exception cause) {
		super(message + String.format(" (line %d, column %d)", lineNum,
				colNum), cause);
		this.error = message;
		this.lineNum = lineNum;
		this.colNum = colNum;
	}

	/**
	 * Returns the error message without the line and column number.
	 * 
	 * @return the error message without the line and column number
	 */
	public String getError() {
		return error;
	}

	/**
	 * Returns the line number. The first line is 1.
	 * 
	 * @return the line number (first line is 1)
	 */
	public int getLineNum() {
		return lineNum;
	}

	/**
	 * Returns the column or character number in the line. The first character
	 * is 1.
	 * 
	 * @return the column or character number in the line (first character is 1)
	 */
	public int getColNum() {
		return colNum;
	}
}
