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

import java.util.ArrayList;
import java.util.List;

/**
 * A time unit is a unit for time durations at a precision of milliseconds. The
 * known time units are defined as constants of this class.
 * 
 * @see TimeDuration
 * @author Dennis Hofs (RRD)
 */
public class TimeUnit {
	public static final TimeUnit HOUR = new TimeUnit(3600000, "hour", "hours");
	public static final TimeUnit MINUTE = new TimeUnit(60000, "minute",
			"minutes");
	public static final TimeUnit SECOND = new TimeUnit(1000, "second",
			"seconds");
	public static final TimeUnit MILLISECOND = new TimeUnit(1, "millisecond",
			"milliseconds");
	private static List<TimeUnit> timeUnits = null;
	private static final Object lock = new Object();
	
	private int ms;
	private String singular;
	private String plural;
	
	/**
	 * Constructs a new time unit.
	 *
	 * @param ms the number of milliseconds for this time unit
	 * @param singular the textual representation in singular form
	 * @param plural the textual representation in plural form
	 */
	private TimeUnit(int ms, String singular, String plural) {
		this.ms = ms;
		this.singular = singular;
		this.plural = plural;
	}
	
	/**
	 * Returns a time duration string for the specified number of this time
	 * unit. For example, if this time unit is {@link #MINUTE MINUTE} and you
	 * set <code>n</code> to 30, this method will return "30 minutes".
	 * 
	 * @param n the number of time units
	 * @return the time duration string
	 */
	public String getDurationString(int n) {
		if (n == 1)
			return n + " " + singular;
		else
			return n + " " + plural;
	}
	
	/**
	 * Returns the duration in milliseconds for the specified number of this
	 * time unit.
	 * 
	 * @param n the number of time units
	 * @return the duration in milliseconds
	 */
	public long getDuration(int n) {
		return n * (long)ms;
	}
	
	/**
	 * Parses a time unit string. The string can be a singular or plural form
	 * of the time unit. You can specify the smallest and largest allowed time
	 * unit.
	 * 
	 * @param s the string
	 * @param min the smallest allowed time unit. You can set this to null for
	 * the smallest known time unit.
	 * @param max the largest allowed time unit. You can set this to null for
	 * the largest known time unit.
	 * @return the time unit
	 * @throws IllegalArgumentException if the string is invalid
	 */
	public static TimeUnit parse(String s, TimeUnit min, TimeUnit max)
	throws IllegalArgumentException {
		String lower = s.toLowerCase();
		TimeUnit[] units = getTimeUnits(min, max);
		for (TimeUnit tu : units) {
			if (lower.equals(tu.singular) || lower.equals(tu.plural))
				return tu;
		}
		throw new IllegalArgumentException("Invalid time unit: " + s);
	}
	
	/**
	 * Returns the known time units between the specified smallest and largest
	 * time unit.
	 * 
	 * @param min the smallest time unit to return. You can set this to null
	 * for the smallest known time unit.
	 * @param max the largest time unit to return. You can set this to null for
	 * the largest known time unit.
	 * @return the time units
	 */
	public static TimeUnit[] getTimeUnits(TimeUnit min, TimeUnit max) {
		synchronized (lock) {
			if (timeUnits == null) {
				timeUnits = new ArrayList<>();
				timeUnits.add(MILLISECOND);
				timeUnits.add(SECOND);
				timeUnits.add(MINUTE);
				timeUnits.add(HOUR);
			}
		}
		if (min == null)
			min = TimeUnit.MILLISECOND;
		if (max == null)
			max = TimeUnit.HOUR;
		int minIndex = timeUnits.indexOf(min);
		int maxIndex = timeUnits.indexOf(max);
		TimeUnit[] result = new TimeUnit[maxIndex - minIndex + 1];
		for (int i = 0; i < result.length; i++) {
			result[i] = timeUnits.get(i + minIndex);
		}
		return result;
	}
	
	@Override
	public String toString() {
		return singular;
	}
}
