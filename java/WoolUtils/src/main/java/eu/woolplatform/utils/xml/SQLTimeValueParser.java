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

package eu.woolplatform.utils.xml;

import eu.woolplatform.utils.exception.ParseException;

import org.joda.time.LocalTime;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * Values parser for SQL time values. It accepts strings formatted as HH:mm:ss.
 * 
 * @author Dennis Hofs (RRD)
 */
public class SQLTimeValueParser implements XMLValueParser<LocalTime> {

	@Override
	public LocalTime parse(String xml) throws ParseException {
		try {
			DateTimeFormatter formatter = DateTimeFormat.forPattern(
					"HH:mm:ss");
			return formatter.parseLocalTime(xml);
		} catch (Exception ex) {
			throw new ParseException("Value is not an SQL time string: \"" +
					xml + "\": " + ex.getMessage(), ex);
		}
	}
}
