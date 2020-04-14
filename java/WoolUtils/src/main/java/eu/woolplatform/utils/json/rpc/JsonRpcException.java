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

package eu.woolplatform.utils.json.rpc;

/**
 * This exception is thrown when a JSON-RPC request results in a response with
 * an error.
 * 
 * @author Dennis Hofs
 */
public class JsonRpcException extends Exception {
	private static final long serialVersionUID = 1L;
	
	private int code;
	private Object data;

	/**
	 * Constructs a new JSON-RPC exception.
	 * 
	 * @param message the error message
	 * @param code the error code
	 */
	public JsonRpcException(String message, int code) {
		this(message, code, null);
	}
	
	/**
	 * Constructs a new JSON-RPC exception. The value of "data" can be
	 * converted to a JSON string.
	 * 
	 * @param message the error message
	 * @param code the error code
	 * @param data additional data about the error or null
	 */
	public JsonRpcException(String message, int code, Object data) {
		super(message);
		this.code = code;
		this.data = data;
	}
	
	/**
	 * Returns the error code.
	 * 
	 * @return the error code
	 */
	public int getCode() {
		return code;
	}
	
	/**
	 * Returns additional data about the error. This may be null. The data can
	 * be converted to a JSON string.
	 * 
	 * @return additional data about the error or null
	 */
	public Object getData() {
		return data;
	}
}
