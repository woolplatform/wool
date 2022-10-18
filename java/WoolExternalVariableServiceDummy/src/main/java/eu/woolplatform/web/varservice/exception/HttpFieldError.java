/*
 * Copyright 2019-2022 WOOL Foundation - Licensed under the MIT License:
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package eu.woolplatform.web.varservice.exception;

import eu.woolplatform.utils.json.JsonObject;

/**
 * This class defines an error in the user input for a specified field.
 * 
 * @author Dennis Hofs (RRD)
 */
public class HttpFieldError extends JsonObject {
	private String field = null;
	private String message = null;

	/**
	 * Constructs a new empty field error.
	 */
	public HttpFieldError() {
	}
	
	/**
	 * Constructs a new HTTP field error without an error code and message.
	 * 
	 * @param field the field name
	 */
	public HttpFieldError(String field) {
		this.field = field;
	}

	/**
	 * Constructs a new HTTP field error without an error code.
	 * 
	 * @param field the field name
	 * @param message the error message (can be an empty string or null)
	 */
	public HttpFieldError(String field, String message) {
		this.field = field;
		this.message = message;
	}

	/**
	 * Returns the field name.
	 * 
	 * @return the field name
	 */
	public String getField() {
		return field;
	}

	/**
	 * Sets the field name.
	 * 
	 * @param field the field name
	 */
	public void setField(String field) {
		this.field = field;
	}

	/**
	 * Returns the error message.
	 * 
	 * @return the error message (can be an empty string or null)
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Sets the error message.
	 * 
	 * @param message the error message (can be an empty string or null)
	 */
	public void setMessage(String message) {
		this.message = message;
	}
}
