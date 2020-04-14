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

/**
 * Value parser for string values. It just returns the input string, but it can
 * validate the string length.
 * 
 * @author Dennis Hofs (RRD)
 */
public class StringValueParser implements XMLValueParser<String> {
	private int minLen = 0;
	private int maxLen = -1;
	
	/**
	 * Constructs a new parser without string length validation.
	 */
	public StringValueParser() {
	}
	
	/**
	 * Constructs a new parser with string length validation.
	 * 
	 * @param minLen the minimum length (0 if no minimum)
	 * @param maxLen the maximum length (negative if no maximum)
	 */
	public StringValueParser(int minLen, int maxLen) {
		this.minLen = minLen;
		this.maxLen = maxLen;
	}

	@Override
	public String parse(String xml) throws ParseException {
		if (xml.length() < minLen ||
				(maxLen >= 0 && xml.length() > maxLen)) {
			String error = "String length must be ";
			if (maxLen < 0)
				error += "at least " + minLen;
			else if (minLen <= 0)
				error += "at most " + maxLen;
			else
				error += "between " + minLen + " and " + maxLen;
			error += ": \"" + xml + "\"";
			throw new ParseException(error);
		}
		return xml;
	}
}
