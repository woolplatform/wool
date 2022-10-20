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

import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;

/**
 * This class specifies a duration that spans one or more days. It consists of
 * a number and a date unit. For example: 5 days or 3 months.
 * 
 * @author Dennis Hofs (RRD)
 */
public class DateDuration {
	private int count;
	private DateUnit unit;
	
	/**
	 * Constructs a new date duration.
	 * 
	 * @param count the number of date units
	 * @param unit the date unit
	 */
	public DateDuration(int count, DateUnit unit) {
		this.count = count;
		this.unit = unit;
	}
	
	/**
	 * Returns the number of date units that defines this duration. The date
	 * unit is obtained with {@link #getUnit() getUnit()}.
	 * 
	 * @return the number of date units
	 */
	public int getCount() {
		return count;
	}
	
	/**
	 * Returns the date unit that, together with the number returned by {@link
	 * #getCount() getCount()}, defines this duration.
	 * 
	 * @return the date unit
	 */
	public DateUnit getUnit() {
		return unit;
	}

	/**
	 * Returns the number of times this date duration completely fits between
	 * the two specified times.
	 * 
	 * @param start the start time
	 * @param end the end time
	 * @return the number of durations between the start and end time
	 */
	public int getCountBetween(Temporal start, Temporal end) {
		int singleCount;
		if (unit == DateUnit.DAY)
			singleCount = (int)ChronoUnit.DAYS.between(start, end);
		else if (unit == DateUnit.WEEK)
			singleCount = (int)ChronoUnit.WEEKS.between(start, end);
		else if (unit == DateUnit.MONTH)
			singleCount = (int)ChronoUnit.MONTHS.between(start, end);
		else
			singleCount = (int)ChronoUnit.YEARS.between(start, end);
		return singleCount / count;
	}

	/**
	 * Returns this date duration as a {@link Period Period}.
	 * 
	 * @return the period
	 */
	public Period toPeriod() {
		if (unit == DateUnit.DAY)
			return Period.ofDays(count);
		else if (unit == DateUnit.WEEK)
			return Period.ofWeeks(count);
		else if (unit == DateUnit.MONTH)
			return Period.ofMonths(count);
		else
			return Period.ofYears(count);
	}
	
	@Override
	public int hashCode() {
		return count;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof DateDuration))
			return false;
		DateDuration cmp = (DateDuration)obj;
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
	 * Parses a date duration from a string. The string should consist of a
	 * number and a date unit, separated by white space. You can specify the
	 * smallest and largest allowed date unit.
	 * 
	 * @param s the string
	 * @param min the smallest allowed date unit. You can set this to null for
	 * the smallest known date unit.
	 * @param max the largest allowed date unit. You can set this to null for
	 * the largest known date unit.
	 * @return the date duration
	 * @throws ParseException if the string is invalid
	 */
	public static DateDuration parse(String s, DateUnit min, DateUnit max)
	throws ParseException {
		String trimmed = s.trim();
		if (trimmed.length() == 0)
			throw new ParseException("Invalid date duration: " + s);
		String[] split = trimmed.split("\\s+");
		if (split.length != 2)
			throw new ParseException("Invalid date duration: " + s);
		int count;
		DateUnit unit;
		try {
			count = Integer.parseInt(split[0]);
			unit = DateUnit.parse(split[1], min, max);
		} catch (IllegalArgumentException ex) {
			throw new ParseException("Invalid date duration: " + s, ex);
		}
		if (count <= 0)
			throw new ParseException("Invalid date duration: " + s);
		return new DateDuration(count, unit);
	}
}
