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
 * Value parser for double values. It validates whether the input string is a
 * double, and it can validate whether the value is within a specified range.
 * 
 * @author Dennis Hofs (RRD)
 */
public class DoubleValueParser implements XMLValueParser<Double> {
	private Double minVal;
	private Double maxVal;
	
	/**
	 * Constructs a new parser without range validation.
	 */
	public DoubleValueParser() {
	}
	
	/**
	 * Constructs a new parser with range validation.
	 * 
	 * @param minVal the minimum value or null
	 * @param maxVal the maximum value or null
	 */
	public DoubleValueParser(Double minVal, Double maxVal) {
		this.minVal = minVal;
		this.maxVal = maxVal;
	}

	@Override
	public Double parse(String xml) throws ParseException {
		double val;
		try {
			val = Double.parseDouble(xml);
		} catch (NumberFormatException ex) {
			throw new ParseException("Value is not a double: \"" + xml +
					"\"", ex);
		}
		if ((minVal != null && val < minVal) ||
				(maxVal != null && val > maxVal)) {
			String error = "Double value must be ";
			if (maxVal == null)
				error += "at least " + minVal;
			else if (minVal == null)
				error += "at most " + maxVal;
			else
				error += "between " + minVal + " and " + maxVal;
			error += ": " + val;
			throw new ParseException(error);
		}
		return val;
	}
}
