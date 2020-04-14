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
 * A date unit is a unit for durations that span one or more days. The known
 * date units are defined as constants of this class.
 * 
 * @see DateDuration
 * @author Dennis Hofs (RRD)
 */
public class DateUnit {
	public static final DateUnit YEAR = new DateUnit("year", "years");
	public static final DateUnit MONTH = new DateUnit("month", "months");
	public static final DateUnit WEEK = new DateUnit("week", "weeks");
	public static final DateUnit DAY = new DateUnit("day", "days");
	private static Object lock = new Object();
	private static List<DateUnit> dateUnits = null;
	
	private String singular;
	private String plural;
	
	/**
	 * Constructs a new date unit.
	 * 
	 * @param singular the textual representation in singular form
	 * @param plural the textual representation in plural form
	 */
	private DateUnit(String singular, String plural) {
		this.singular = singular;
		this.plural = plural;
	}
	
	/**
	 * Returns a date duration string for the specified number of this date
	 * unit. For example, if this date unit is {@link #MONTH MONTH} and you set
	 * <code>n</code> to 3, this method will return "3 months".
	 * 
	 * @param n the number of date units
	 * @return the date duration string
	 */
	public String getDurationString(int n) {
		if (n == 1)
			return n + " " + singular;
		else
			return n + " " + plural;
	}
	
	/**
	 * Parses a date unit string. The string can be a singular or plural form
	 * of the date unit. You can specify the smallest and largest allowed date
	 * unit.
	 * 
	 * @param s the string
	 * @param min the smallest allowed date unit. You can set this to null for
	 * the smallest known date unit.
	 * @param max the largest allowed date unit. You can set this to null for
	 * the largest known date unit.
	 * @return the date unit
	 * @throws IllegalArgumentException if the string is invalid
	 */
	public static DateUnit parse(String s, DateUnit min, DateUnit max)
	throws IllegalArgumentException {
		String lower = s.toLowerCase();
		DateUnit[] units = getDateUnits(min, max);
		for (DateUnit du : units) {
			if (lower.equals(du.singular) || lower.equals(du.plural))
				return du;
		}
		throw new IllegalArgumentException("Invalid date unit: " + s);
	}
	
	/**
	 * Returns the known date units between the specified smallest and largest
	 * date unit.
	 * 
	 * @param min the smallest date unit to return. You can set this to null
	 * for the smallest known date unit.
	 * @param max the largest date unit to return. You can set this to null for
	 * the largest known date unit.
	 * @return the date units
	 */
	public static DateUnit[] getDateUnits(DateUnit min, DateUnit max) {
		synchronized (lock) {
			if (dateUnits == null) {
				dateUnits = new ArrayList<DateUnit>();
				dateUnits.add(DAY);
				dateUnits.add(WEEK);
				dateUnits.add(MONTH);
				dateUnits.add(YEAR);
			}
		}
		if (min == null)
			min = DateUnit.DAY;
		if (max == null)
			max = DateUnit.YEAR;
		int minIndex = dateUnits.indexOf(min);
		int maxIndex = dateUnits.indexOf(max);
		DateUnit[] result = new DateUnit[maxIndex - minIndex + 1];
		for (int i = 0; i < result.length; i++) {
			result[i] = dateUnits.get(i + minIndex);
		}
		return result;
	}
	
	@Override
	public String toString() {
		return singular;
	}
}
