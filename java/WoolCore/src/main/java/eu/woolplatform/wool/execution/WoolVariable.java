/*
 * Copyright 2019-2022 WOOL Foundation.
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
package eu.woolplatform.wool.execution;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.ZonedDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WoolVariable {

	private String name;
	private Object value;
	private ZonedDateTime lastUpdated;

	public WoolVariable() { }

	public WoolVariable(String name, Object value, ZonedDateTime lastUpdated) {
		this.name = name;
		this.value = value;
		this.lastUpdated = lastUpdated;
	}

	// ----- Getters & Setters

	/**
	 * Returns the name of the WOOL Variable as a String.
	 * @return the name of the WOOL Variable as a String.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of the WOOL Variable.
	 * @param name the name of the WOOL Variable.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns the value of the WOOL Variable as an {@link Object}.
	 * @return the value of the WOOL Variable as an {@link Object}.
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * Sets the value of the WOOL Variable as an {@link Object}.
	 * @param value the value of the WOOL Variable as an {@link Object}.
	 */
	public void setValue(Object value) {
		this.value = value;
	}

	/**
	 * Returns the timestamp of when this WOOL Variable was last updated (in the timezone of the user).
	 * @return the UTC timestamp of when this WOOL Variable was last updated (in the timezone of the user).
	 */
	public ZonedDateTime getLastUpdated() {
		return lastUpdated;
	}

	/**
	 * Sets the timestamp of when this WOOL Variable was last updated (in the timezone of the user).
	 * @param lastUpdated the timestamp of when this WOOL Variable was last updated (in the timezone of the user).
	 */
	public void setLastUpdated(ZonedDateTime lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	public String toString() {
		return "WoolVariable[" +
				"name:'"+getName()+"',"+
				"value:'"+getValue()+"',"+
				"lastUpdated:'"+getLastUpdated()+"']";
	}

}
