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

package eu.woolplatform.utils.log;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 * This class can tag lines in a log message before the message is written. The
 * tags include the log level, the message tag (identification of the source)
 * and the current date and time.
 * 
 * @author Dennis Hofs
 */
public class LogLineTagger {

	/**
	 * Tags every line in the specified message. It will prefix every line with
	 * the log level, the tag and the current date and time. Every line will
	 * end with a new line character, including the last line.
	 * 
	 * @param level the log level
	 * @param tag the tag
	 * @param time the current date and time
	 * @param msg the log message
	 * @return the log message with tagged lines
	 */
	public static String tagLines(int level, String tag, DateTime time,
			String msg) {
		String newline = System.getProperty("line.separator");
		DateTimeFormatter formatter = ISODateTimeFormat.dateTime();
		String timeStr = formatter.print(time);
		String[] lines = msg.split("\r\n|\r|\n", -1);
		StringBuffer buf = new StringBuffer();
		for (String line : lines) {
			buf.append("[");
			buf.append(levelToString(level));
			buf.append("] [");
			buf.append(tag);
			buf.append("] [");
			buf.append(timeStr);
			buf.append("] ");
			buf.append(line);
			buf.append(newline);
		}
		return buf.toString();
	}
	
	/**
	 * Returns a string representation of the specified log level.
	 * 
	 * @param level the log level
	 * @return the string representation
	 */
	private static String levelToString(int level) {
		switch (level) {
		case Logger.ASSERT:
			return "ASSERT";
		case Logger.ERROR:
			return "ERROR";
		case Logger.WARN:
			return "WARN";
		case Logger.INFO:
			return "INFO";
		case Logger.DEBUG:
			return "DEBUG";
		case Logger.VERBOSE:
			return "VERBOSE";
		default:
			return "UNKNOWN";
		}
	}
}
