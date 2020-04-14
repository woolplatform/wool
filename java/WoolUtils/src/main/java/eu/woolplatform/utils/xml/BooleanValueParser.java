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
 * Values parser for boolean values. It accepts strings "0" or "false" for
 * false, and "1" or "true" for true. The string comparison is
 * case-insensitive.
 * 
 * @author Dennis Hofs (RRD)
 */
public class BooleanValueParser implements XMLValueParser<Boolean> {

	@Override
	public Boolean parse(String xml) throws ParseException {
		String lower = xml.toLowerCase();
		if (lower.equals("0") || lower.equals("false"))
			return false;
		else if (lower.equals("1") || lower.equals("true"))
			return true;
		throw new ParseException("Value is not a boolean: \"" + xml + "\"");
	}
}
