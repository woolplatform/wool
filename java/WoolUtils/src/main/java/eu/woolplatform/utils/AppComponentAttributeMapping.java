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

import eu.woolplatform.utils.exception.ParseException;

/**
 * An attribute mapping can be added to {@link AppComponents AppComponents}
 * to register custom attribute types. It is used when a component is built
 * from XML. If an attribute has a matching class, then the mapping will be
 * used to parse the string value of the attribute.
 *
 * @author Dennis Hofs (RRD)
 * @param <T> the attribute class
 */
public interface AppComponentAttributeMapping<T> {

	/**
	 * Returns the attribute class.
	 *
	 * @return the attribute class
	 */
	Class<T> getAttributeClass();

	/**
	 * Parses the string representation of the attribute value.
	 *
	 * @param value string representation of the attribute value
	 * @return the attribute value
	 * @throws ParseException if the string format is invalid
	 */
	T parseValue(String value) throws ParseException;
}
