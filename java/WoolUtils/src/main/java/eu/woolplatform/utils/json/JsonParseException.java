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
