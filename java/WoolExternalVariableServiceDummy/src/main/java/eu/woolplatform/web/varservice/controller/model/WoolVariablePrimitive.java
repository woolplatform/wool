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
package eu.woolplatform.web.varservice.controller.model;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * A representation of a WOOL Variable using only primitive types, which makes it more convenient
 * for serialization / deserialization.
 */
public class WoolVariablePrimitive {

	@Schema(description = "Name of the WOOL Variable",
			example = "woolVariableName",
			required = true)
	private String name;

	@Schema(description = "Value of the WOOL Variable",
			example = "some value",
			required = true)
	private Object value;

	@Schema(description = "UTC Timestamp in milliseconds representing the moment the variable was last updated",
			example = "1665571549000",
			required = true)
	private Long lastUpdatedTime;

	@Schema(description = "The time zone in which the lastUpdatedTimestamp occurred.",
			example = "Europe/Lisbon",
			required = true)
	private String lastUpdatedTimeZone;

	// ----- Constructors

	public WoolVariablePrimitive() { }

	public WoolVariablePrimitive(String name, Object value, Long lastUpdatedTime, String lastUpdatedTimeZone) {
		this.name = name;
		this.value = value;
		this.lastUpdatedTime = lastUpdatedTime;
		this.lastUpdatedTimeZone = lastUpdatedTimeZone;
	}

	// ----- Getters & Setters

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public Long getLastUpdatedTime() {
		return lastUpdatedTime;
	}

	public void setLastUpdatedTime(Long lastUpdatedTime) {
		this.lastUpdatedTime = lastUpdatedTime;
	}

	public String getLastUpdatedTimeZone() {
		return lastUpdatedTimeZone;
	}

	public void setLastUpdatedTimeZone(String lastUpdatedTimeZone) {
		this.lastUpdatedTimeZone = lastUpdatedTimeZone;
	}

	@Override
	public String toString() {
		return "WoolVariableParam{" +
				"name='" + name + "'" +
				", value='" + value + "'" +
				", lastUpdatedTime='" + lastUpdatedTime + "'" +
				", lastUpdatedTimeZone=" + lastUpdatedTimeZone + "'" +
				'}';
	}
}
