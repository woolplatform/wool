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

package eu.woolplatform.wool.execution;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WoolVariable {

	private String name;
	private Object value;
	private Long updatedTime;
	private String updatedTimeZone;

	// ----- Constructors

	public WoolVariable(
			@JsonProperty("name")
			String name,
			@JsonProperty("value")
			Object value,
			@JsonProperty("updatedTime")
			Long updatedTime,
			@JsonProperty("updatedTimeZone")
			String updatedTimeZone) {
		this.name = name;
		this.value = value;
		this.updatedTime = updatedTime;
		this.updatedTimeZone = updatedTimeZone;
	}

	@JsonIgnore
	public WoolVariable(String name, Object value, ZonedDateTime lastUpdated) {
		this.name = name;
		this.value = value;
		this.updatedTime = lastUpdated.toInstant().toEpochMilli();
		this.updatedTimeZone = lastUpdated.getZone().toString();
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
	 * Returns the value of the WOOL Variable as an {@link Object}.
	 * @return the value of the WOOL Variable as an {@link Object}.
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * Returns the timestamp of when this WOOL Variable was last updated (as epoch time in milliseconds).
	 * @return the UTC timestamp of when this WOOL Variable was last updated (as epoch time in milliseconds).
	 */
	public Long getUpdatedTime() {
		return updatedTime;
	}

	public String getUpdatedTimeZone() {
		return updatedTimeZone;
	}

	// ---------- Convenience

	/**
	 * Returns a {@link ZonedDateTime} object representing the date/time that this {@link WoolVariable} was last updated
	 * in the timezone of the user.
	 * @return the last updated time for this variable in the timezone of the user.
	 */
	@JsonIgnore
	public ZonedDateTime getZonedUpdatedTime() {
		ZoneId timeZone;

		if (this.getUpdatedTimeZone() == null || this.getUpdatedTimeZone().length() == 0) {
			timeZone = ZoneId.systemDefault();
		} else {
			timeZone = ZoneId.of(this.getUpdatedTimeZone());
		}

		if(this.getUpdatedTime() == null) {
			return ZonedDateTime.ofInstant(Instant.ofEpochMilli(0), timeZone);
		} else {
			return ZonedDateTime.ofInstant(Instant.ofEpochMilli(this.getUpdatedTime()), timeZone);
		}
	}

	@Override
	public String toString() {
		return "WoolVariable{" +
				"name='" + name + '\'' +
				", value=" + value +
				", lastUpdatedTime=" + updatedTime +
				", lastUpdatedTimeZone='" + updatedTimeZone + '\'' +
				'}';
	}
}
