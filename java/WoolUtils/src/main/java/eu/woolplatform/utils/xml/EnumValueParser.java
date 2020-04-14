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

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import eu.woolplatform.utils.exception.ParseException;

/**
 * Value parser for enum values. It performs a case-insensitive search of a
 * value in a specified enum class.
 * 
 * @author Dennis Hofs (RRD)
 * @param <T> the enum type
 */
public class EnumValueParser<T extends Enum<T>> implements XMLValueParser<T> {
	private Class<T> enumClass;

	/**
	 * Constructs a new parser.
	 * 
	 * @param enumClass the enum class
	 */
	public EnumValueParser(Class<T> enumClass) {
		this.enumClass = enumClass;
	}

	@Override
	public T parse(String xml) throws ParseException {
		String lowerXml = xml.toLowerCase();
		Object array;
		String invokeError = "Can't invoke method values() on enum class";
		try {
			Method method = enumClass.getMethod("values");
			array = method.invoke(null);
		} catch (InvocationTargetException ex) {
			Throwable targetEx = ex.getTargetException();
			if (targetEx == null)
				targetEx = ex;
			throw new RuntimeException(invokeError + ": " +
					targetEx.getMessage(), targetEx);
		} catch (Exception ex) {
			throw new RuntimeException(invokeError + ": " + ex.getMessage(),
					ex);
		}
		int len = Array.getLength(array);
		for (int i = 0; i < len; i++) {
			Object objItem = Array.get(array, i);
			if (objItem.toString().toLowerCase().equals(lowerXml))
				return enumClass.cast(objItem);
		}
		throw new ParseException("Value of enum class " + enumClass.getName() +
				" not found: " + xml);
	}
}
