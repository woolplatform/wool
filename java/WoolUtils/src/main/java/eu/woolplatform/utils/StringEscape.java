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

package eu.woolplatform.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu.woolplatform.utils.exception.ParseException;

/**
 * <p>This class can resolve escaped characters in a string and it can replace
 * special characters with escaped characters. It supports the following
 * escapes:</p>
 * 
 * <ul>
 * <li>\n -&gt; new line</li>
 * <li>\r -&gt; carriage return</li>
 * <li>\t -&gt; tab</li>
 * <li>\<i>c</i> (where <i>c</i> is any other character including \ and ")
 * -&gt; <i>c</i></li>
 * </ul>
 * 
 * @author Dennis Hofs
 */
public class StringEscape {
	
	/**
	 * Replaces escaped characters in the specified string. This method throws
	 * an exception if it finds a lone \.
	 * 
	 * @param s a string
	 * @return the string without escapes
	 * @throws ParseException if a lone \ is found
	 */
	public static String resolveEscapes(String s) throws ParseException {
		return resolveEscapes(s, new char[0]);
	}

	/**
	 * Replaces escaped characters in the specified string. This method throws
	 * an exception if it finds a lone \ or if one of the specified illegal
	 * characters is not escaped.
	 * 
	 * @param s a string
	 * @param illegals characters that must be escaped
	 * @return the string without escapes
	 * @throws ParseException if a lone \ is found or if one of the illegal
	 * characters is not escaped
	 */
	public static String resolveEscapes(String s, char[] illegals)
	throws ParseException {
		StringBuilder result = new StringBuilder();
		String patStr = "\\\\";
		for (char c : illegals) {
			patStr += "|" + c;
		}
		Pattern pattern = Pattern.compile(patStr);
		Matcher m = pattern.matcher(s);
		int start = 0;
		while (start < s.length() && m.find(start)) {
			int index = m.start();
			char c = s.charAt(index);
			if (c != '\\' || index == s.length() - 1) {
				throw new ParseException("Invalid escaped string: \"" + s +
						"\"");
			}
			c = s.charAt(index + 1);
			result.append(s, start, index);
			switch (c) {
			case 'n':
				c = '\n';
				break;
			case 'r':
				c = '\r';
				break;
			case 't':
				c = '\t';
				break;
			}
			result.append(c);
			start = index + 2;
		}
		if (start < s.length())
			result.append(s.substring(start));
		return result.toString();
	}
	
	/**
	 * Replaces special characters with an escape sequence. It escapes the
	 * characters listed at the top of this page.
	 * 
	 * @param s a string
	 * @return the escaped string
	 */
	public static String escape(String s) {
		return escape(s, new char[0]);
	}

	/**
	 * Replaces special characters with an escape sequence. It escapes the
	 * characters listed at the top of this page as well as the specified
	 * illegal characters (which are considered illegal if they are not
	 * escaped).
	 * 
	 * @param s a string
	 * @param illegals characters that must be escaped
	 * @return the escaped string
	 */
	public static String escape(String s, char[] illegals) {
		String escaped = s.replaceAll("\\\\", "\\\\\\\\");
		escaped = escaped.replaceAll("\n", "\\\\n");
		escaped = escaped.replaceAll("\r", "\\\\r");
		escaped = escaped.replaceAll("\t", "\\\\t");
		for (char c : illegals) {
			escaped = escaped.replaceAll(Character.toString(c), "\\\\" + c);
		}
		return escaped;
	}
}
