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

package eu.woolplatform.utils.exception;

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
	 * Returns the error message that caused this {@link
	 * LineNumberParseException} without the line and column number.
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
