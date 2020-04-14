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

package eu.woolplatform.utils.validation;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.woolplatform.utils.exception.ParseException;

/**
 * This class can perform various type conversions.
 * 
 * @author Dennis Hofs (RRD)
 */
public class TypeConversion {

	/**
	 * Converts the specified object to a string. If the object is null, this
	 * method returns null. Otherwise it will call toString().
	 * 
	 * @param obj the object or null
	 * @return the string or null
	 */
	public static String getString(Object obj) {
		return obj == null ? null : obj.toString();
	}

	/**
	 * Converts the specified object to an integer. If the object is null, this
	 * method returns null. If it's a Number, it will return the integer value.
	 * Otherwise it will call toString() and try to parse the string value.
	 * 
	 * @param obj the object or null
	 * @return the integer or null
	 * @throws ParseException if a string value can't be parsed as an integer
	 */
	public static Integer getInt(Object obj) throws ParseException {
		if (obj == null)
			return null;
		if (obj instanceof Number)
			return ((Number)obj).intValue();
		String s = obj.toString();
		try {
			return Integer.valueOf(s);
		} catch (NumberFormatException ex) {
			throw new ParseException(String.format("Invalid integer: %s", s));
		}
	}

	/**
	 * Converts the specified object to a long. If the object is null, this
	 * method returns null. If it's a Number, it will return the long value.
	 * Otherwise it will call toString() and try to parse the string value.
	 * 
	 * @param obj the object or null
	 * @return the long or null
	 * @throws ParseException if a string value can't be parsed as a long
	 */
	public static Long getLong(Object obj) throws ParseException {
		if (obj == null)
			return null;
		if (obj instanceof Number)
			return ((Number)obj).longValue();
		String s = obj.toString();
		try {
			return Long.valueOf(s);
		} catch (NumberFormatException ex) {
			throw new ParseException(String.format("Invalid long: %s", s));
		}
	}

	/**
	 * Converts the specified object to a double. If the object is null, this
	 * method returns null. If it's a Number, it will return the double value.
	 * Otherwise it will call toString() and try to parse the string value.
	 * 
	 * @param obj the object or null
	 * @return the double or null
	 * @throws ParseException if a string value can't be parsed as a double
	 */
	public static Double getDouble(Object obj) throws ParseException {
		if (obj == null)
			return null;
		if (obj instanceof Number)
			return ((Number)obj).doubleValue();
		String s = obj.toString();
		try {
			return Double.valueOf(s);
		} catch (NumberFormatException ex) {
			throw new ParseException(String.format("Invalid double: %s", s));
		}
	}

	/**
	 * Converts the specified object to a boolean. If the object is null, this
	 * method returns null. If it's a Boolean, it will return the boolean
	 * value. Otherwise it will call toString() and try to parse the string
	 * value.
	 * 
	 * @param obj the object or null
	 * @return the boolean or null
	 * @throws ParseException if a string value can't be parsed as a boolean
	 */
	public static Boolean getBoolean(Object obj) throws ParseException {
		if (obj == null)
			return null;
		if (obj instanceof Boolean)
			return (Boolean)obj;
		String s = obj.toString();
		String lower = s.toLowerCase();
		if (lower.equals("true") || lower.equals("1")) {
			return true;
		} else if (lower.equals("false") || lower.equals("0")) {
			return false;
		} else {
			throw new ParseException(String.format("Invalid boolean: %s", s));
		}
	}
	
	/**
	 * Converts the specified object to an enum. If the object is null, this
	 * method returns null. If it's an instance of the specified enum class,
	 * it just returns the object. Otherwise it gets the string value and does
	 * a case-insensitive comparison with the possible enum values. 
	 * 
	 * @param obj the object or null
	 * @param enumClass the enum class
	 * @param <T> the type of enum to return
	 * @return the enum or null
	 * @throws ParseException if the enum value is invalid
	 */
	public static <T extends Enum<?>> T getEnum(Object obj, Class<T> enumClass)
			throws ParseException {
		if (obj == null)
			return null;
		if (enumClass.isInstance(obj))
			return enumClass.cast(obj);
		String findLower = obj.toString().toLowerCase();
		Object array;
		try {
			Method method = enumClass.getMethod("values");
			array = method.invoke(null);
		} catch (NoSuchMethodException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		} catch (InvocationTargetException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		} catch (IllegalAccessException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
		int len = Array.getLength(array);
		for (int i = 0; i < len; i++) {
			Object candidate = Array.get(array, i);
			if (candidate.toString().toLowerCase().equals(findLower)) {
				return enumClass.cast(candidate);
			}
		}
		throw new ParseException("Unknown value " + obj + " for enum class " +
				enumClass.getName());
	}
	
	/**
	 * Converts an object to the specified type using the Jackson ObjectMapper.
	 * For example a Map could be converted to an object. This method does not
	 * parse JSON strings. If the object is null, this method returns null.
	 * 
	 * @param obj the object or null
	 * @param clazz the return type
	 * @return the converted object or null
	 * @throws ParseException if the object can't be converted to the specified
	 * type
	 * @param <T> the return type
	 */
	public static <T> T getJson(Object obj, Class<T> clazz)
			throws ParseException {
		if (obj == null)
			return null;
		if (clazz.isInstance(obj))
			return clazz.cast(obj);
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.convertValue(obj, clazz);
		} catch (IllegalArgumentException ex) {
			throw new ParseException("Invalid JSON content: " +
					ex.getMessage(), ex);
		}
	}

	/**
	 * Converts an object to the specified type using the Jackson ObjectMapper.
	 * For example a Map could be converted to an object. This method does not
	 * parse JSON strings. If the object is null, this method returns null.
	 * 
	 * <p>If you want to convert to MyObject, you can specify:<br />
	 * new TypeReference&lt;MyObject&gt;() {}</p>
	 * 
	 * @param obj the object or null
	 * @param typeRef the return type
	 * @return the converted object or null
	 * @throws ParseException if the object can't be converted to the specified
	 * type
	 * @param <T> the return type
	 */
	public static <T> T getJson(Object obj, TypeReference<T> typeRef)
			throws ParseException {
		if (obj == null)
			return null;
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.convertValue(obj, typeRef);
		} catch (IllegalArgumentException ex) {
			throw new ParseException("Invalid JSON content: " +
					ex.getMessage(), ex);
		}
	}
}
