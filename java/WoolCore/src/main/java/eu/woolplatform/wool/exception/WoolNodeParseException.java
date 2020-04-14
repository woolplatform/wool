/*
 * Copyright 2019-2020 Roessingh Research and Development.
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

package eu.woolplatform.wool.exception;

import eu.woolplatform.utils.exception.LineNumberParseException;
import eu.woolplatform.utils.exception.ParseException;

/**
 * This exception indicates a parse error within a node.
 * 
 * @author Dennis Hofs (Roessingh Research and Development)
 */
public class WoolNodeParseException extends ParseException {
	private static final long serialVersionUID = 5095774410463182542L;

	private String nodeTitle;

	/**
	 * Constructs a new exception in the specified node. If the node title is
	 * unknown, it can be set to null.
	 * 
	 * @param message the message
	 * @param nodeTitle the node title or null
	 * @param cause the parse error
	 */
	public WoolNodeParseException(String message, String nodeTitle,
			LineNumberParseException cause) {
		super(message, cause);
		this.nodeTitle = nodeTitle;
	}

	/**
	 * Returns the node title. If the title is unknown, this method returns
	 * null.
	 * 
	 * @return the node title or null
	 */
	public String getNodeTitle() {
		return nodeTitle;
	}

	/**
	 * Returns the parse error in the node.
	 * 
	 * @return the parse error in the node
	 */
	public LineNumberParseException getLineNumberParseException() {
		return (LineNumberParseException)getCause();
	}
}
