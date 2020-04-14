package eu.woolplatform.utils.json;

/**
 * This exception is thrown when a JSON string can't be parsed.
 * 
 * @author Dennis Hofs (RRD)
 */
public class JsonParseException extends Exception {
	private static final long serialVersionUID = 1L;
	
	private int line;
	private int linePos;

	/**
	 * Constructs a new exception. The line number and character number will be
	 * appended to the message.
	 * 
	 * @param message the error message
	 * @param line the line number (first line is 1)
	 * @param linePos the character number in the line (first character is 1)
	 */
	public JsonParseException(String message, int line, int linePos) {
		super(message + String.format(" (line %s, character %s)", line,
				linePos));
		this.line = line;
		this.linePos = linePos;
	}

	/**
	 * Constructs a new message with another JsonParseException as the cause.
	 * The cause cannot be null. This constructor does not append the line
	 * number and character number to the message.
	 * 
	 * @param message the message
	 * @param cause the cause (not null)
	 */
	public JsonParseException(String message, JsonParseException cause) {
		super(message, cause);
		line = cause.getLine();
		linePos = cause.getLinePos();
	}

	/**
	 * Returns the line number. The first line is 1.
	 * 
	 * @return the line number (first line is 1)
	 */
	public int getLine() {
		return line;
	}

	/**
	 * Returns the character number in the line. The first character is 1.
	 * 
	 * @return the character number in the line (first character is 1)
	 */
	public int getLinePos() {
		return linePos;
	}
}
