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

package eu.woolplatform.utils.schedule;

import eu.woolplatform.utils.exception.ParseException;

/**
 * This class specifies a duration with a precision of milliseconds. It
 * consists of a number and a time unit. For example: 30 minutes or 2 hours.
 * 
 * @author Dennis Hofs (RRD)
 */
public class TimeDuration {
	private int count;
	private TimeUnit unit;

	/**
	 * Constructs a new time duration.
	 *
	 * @param count the number of time units
	 * @param unit the time unit
	 */
	public TimeDuration(int count, TimeUnit unit) {
		this.count = count;
		this.unit = unit;
	}

	/**
	 * Returns the number of time units that defines this duration. The time
	 * unit is obtained with {@link #getUnit() getUnit()}.
	 *
	 * @return the number of time units
	 */
	public int getCount() {
		return count;
	}

	/**
	 * Returns the time unit that, together with the number returned by {@link
	 * #getCount() getCount()}, defines this duration.
	 *
	 * @return the time unit
	 */
	public TimeUnit getUnit() {
		return unit;
	}

	/**
	 * Returns the duration in milliseconds.
	 *
	 * @return the duration in milliseconds
	 */
	public long getDuration() {
		return unit.getDuration(count);
	}

	@Override
	public int hashCode() {
		return count;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof TimeDuration))
			return false;
		TimeDuration cmp = (TimeDuration)obj;
		if (count != cmp.count)
			return false;
		if (unit != cmp.unit)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return unit.getDurationString(count);
	}

	/**
	 * Parses a time duration from a string. The string should consist of a
	 * number and a time unit, separated by white space. You can specify the
	 * smallest and largest allowed time unit.
	 *
	 * @param s the string
	 * @param min the smallest allowed time unit. You can set this to null for
	 * the smallest known time unit.
	 * @param max the largest allowed time unit. You can set this to null for
	 * the largest known time unit.
	 * @return the time duration
	 * @throws ParseException if the string is invalid
	 */
	public static TimeDuration parse(String s, TimeUnit min, TimeUnit max)
	throws ParseException {
		String trimmed = s.trim();
		if (trimmed.length() == 0)
			throw new ParseException("Invalid time duration: " + s);
		String[] split = trimmed.split("\\s+");
		if (split.length != 2)
			throw new ParseException("Invalid time duration: " + s);
		int count;
		TimeUnit unit;
		try {
			count = Integer.parseInt(split[0]);
			unit = TimeUnit.parse(split[1], min, max);
		} catch (NumberFormatException ex) {
			throw new ParseException("Invalid time duration: " + s, ex);
		} catch (IllegalArgumentException ex) {
			throw new ParseException("Invalid time duration: " + s, ex);
		}
		if (count < 0)
			throw new ParseException("Invalid time duration: " + s);
		return new TimeDuration(count, unit);
	}
}
