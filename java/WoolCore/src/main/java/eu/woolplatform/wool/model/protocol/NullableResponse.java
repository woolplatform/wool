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

package eu.woolplatform.wool.model.protocol;

import nl.rrd.utils.json.JsonObject;

/**
 * This class is used when a web service wants to return a value or null. This
 * is to ensure that the Spring Framework returns a valid JSON string. Normally
 * if a Spring method returns null, it results in an empty response.
 * 
 * @author Dennis Hofs (RRD)
 *
 * @param <T> the value type
 */
public class NullableResponse<T> extends JsonObject {
	private T value = null;

	/**
	 * Constructs a new response with value null. This default constructor is
	 * needed for JSON serialization.
	 */
	public NullableResponse() {
	}
	
	/**
	 * Constructs a new response with the specified value.
	 * 
	 * @param value the value or null
	 */
	public NullableResponse(T value) {
		this.value = value;
	}

	/**
	 * Returns the value. This can be null.
	 * 
	 * @return the value or null
	 */
	public T getValue() {
		return value;
	}

	/**
	 * Sets the value. This can be null.
	 * 
	 * @param value the value or null
	 */
	public void setValue(T value) {
		this.value = value;
	}
}
