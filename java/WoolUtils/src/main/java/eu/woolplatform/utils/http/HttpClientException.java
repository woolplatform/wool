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

package eu.woolplatform.utils.http;

/**
 * This exception is thrown when a HTTP request results in an error response.
 * 
 * @author Dennis Hofs (RRD)
 */
public class HttpClientException extends Exception {
	private static final long serialVersionUID = 1L;

	private int statusCode;
	private String statusMessage;
	private String errorContent;

	/**
	 * Constructs a new HTTP client exception.
	 * 
	 * @param statusCode the HTTP status code
	 * @param statusMessage the HTTP status message
	 * @param errorContent the error string from the HTTP content
	 */
	public HttpClientException(int statusCode, String statusMessage,
			String errorContent) {
		super(statusCode + " " + statusMessage + ": " + errorContent);
		this.statusCode = statusCode;
		this.statusMessage = statusMessage;
		this.errorContent = errorContent;
	}

	/**
	 * Returns the HTTP status code.
	 * 
	 * @return the HTTP status code
	 */
	public int getStatusCode() {
		return statusCode;
	}

	/**
	 * Returns the HTTP status message.
	 * 
	 * @return the HTTP status message
	 */
	public String getStatusMessage() {
		return statusMessage;
	}

	/**
	 * Returns the error string from the HTTP content.
	 * 
	 * @return the error string from the HTTP content
	 */
	public String getErrorContent() {
		return errorContent;
	}
}
