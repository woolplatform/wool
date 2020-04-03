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

package nl.rrd.wool.expressions;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import nl.rrd.wool.json.JsonMapper;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class wraps around an elementary value type and provides type mapping,
 * comparison and boolean evaluation. The value should be one of the following:
 * 
 * <p><ul>
 * <li>null</li>
 * <li>{@link String String}</li>
 * <li>{@link Number Number}</li>
 * <li>{@link Boolean Boolean}</li>
 * <li>{@link List List}</li>
 * <li>{@link Map Map}: the keys must be strings</li>
 * </ul></p>
 * 
 * <p>Each element of a list or map should also be one of these types.</p>
 * 
 * @author Dennis Hofs (RRD)
 */
@JsonSerialize(using=Value.ValueSerializer.class)
@JsonDeserialize(using=Value.ValueDeserializer.class)
public class Value {
	private Object value;

	/**
	 * Constructs a new value.
	 * 
	 * <p>If the value is a map, it will be converted to a string map. Each map
	 * key is converted to a string. If the result has duplicate keys, then this
	 * method throws an {@link IllegalArgumentException
	 * IllegalArgumentException}.
	 * 
	 * @param value the value
	 * @throws IllegalArgumentException if the value is a map and it can't be
	 * converted to a string map
	 */
	public Value(Object value) throws IllegalArgumentException {
		this.value = value;
	}

	/**
	 * Returns the value.
	 * 
	 * @return the value
	 */
	public Object getValue() {
		return value;
	}
	
	/**
	 * Returns whether the value is null.
	 * 
	 * @return true if the value is null, false otherwise
	 */
	public boolean isNull() {
		return value == null;
	}
	
	/**
	 * Returns whether the value is a string.
	 * 
	 * @return true if the value is a string, false otherwise
	 */
	public boolean isString() {
		return value instanceof String;
	}
	
	/**
	 * Returns whether the value is a numeric string.
	 * 
	 * @return true if the value is a numeric string, false otherwise
	 */
	public boolean isNumericString() {
		if (!isString())
			return false;
		String s = (String)value;
		try {
			if (s.matches("-?[0-9]+"))
				Long.parseLong(s);
			else
				Double.parseDouble(s);
			return true;
		} catch (IllegalArgumentException ex) {
			return false;
		}
	}

	/**
	 * Returns whether the value is a number.
	 * 
	 * @return true if the value is a number, false otherwise
	 */
	public boolean isNumber() {
		return value instanceof Number;
	}
	
	/**
	 * Returns whether the value is an integer number (byte, short, integer,
	 * long).
	 * 
	 * @return true if the value is an integer number (byte, short, integer,
	 * long), false otherwise
	 */
	public boolean isIntNumber() {
		return isNumber() && isIntNumber((Number)value);
	}
	
	/**
	 * Returns whether the value is a boolean.
	 * 
	 * @return true if the value is a boolean, false otherwise
	 */
	public boolean isBoolean() {
		return value instanceof Boolean;
	}
	
	/**
	 * Returns whether the value is a list.
	 * 
	 * @return true if the value is a list, false otherwise
	 */
	public boolean isList() {
		return value instanceof List;
	}
	
	/**
	 * Returns whether the value is a map.
	 * 
	 * @return true if the value is a map, false otherwise
	 */
	public boolean isMap() {
		return value instanceof Map;
	}
	
	/**
	 * Returns the value as a number. The returned number will always be an
	 * instanceof Integer, Long or Double. The following values can be converted
	 * to numbers.
	 * 
	 * <p><ul>
	 * <li>null: 0</li>
	 * <li>number: normalized to int, long or double</li>
	 * <li>string: parsed as an int, long or double</li>
	 * <li>boolean: true = 1, false = 0</li>
	 * </ul></p>
	 * 
	 * <p>If the value is a list, map or a string that can't be parsed as a
	 * number, then this method throws an {@link EvaluationException
	 * EvaluationException}.</p>
	 * 
	 * @return the number
	 * @throws EvaluationException if the value can't be evaluated as a number
	 */
	public Number asNumber() throws EvaluationException {
		if (value == null) {
			return 0;
		} else if (value instanceof Number) {
			return normalizeNumber((Number)value);
		} else if (value instanceof String) {
			String s = (String)value;
			try {
				if (s.matches("-?[0-9]+")) {
					long num = Long.parseLong(s);
					return normalizeNumber(num);
				} else {
					return Double.parseDouble(s);
				}
			} catch (IllegalArgumentException ex) {
				throw new EvaluationException(
						"Can't convert string to number: " + s);
			}
		} else if (value instanceof Boolean) {
			boolean b = (Boolean)value;
			return b ? 1 : 0;
		} else {
			throw new EvaluationException(String.format(
					"Can't convert %s to number", getTypeString()));
		}
	}
	
	/**
	 * Returns the boolean evaluation of the value as follows.
	 * 
	 * <p><ul>
	 * <li>null: false</li>
	 * <li>boolean: the boolean value</li>
	 * <li>string: true if length &gt; 0, false if length == 0</li>
	 * <li>number: true if value != 0, false if value == 0</li>
	 * <li>list: true if not empty, false if empty</li>
	 * <li>map: true if not empty, false if empty</li>
	 * </ul></p>
	 * 
	 * @return the boolean evaluation of the value
	 */
	public boolean asBoolean() {
		if (value == null) {
			return false;
		} else if (value instanceof Boolean) {
			return (Boolean)value;
		} else if (value instanceof String) {
			String s = (String)value;
			return s.length() > 0;
		} else if (value instanceof Number) {
			Number n = (Number)value;
			if (isIntNumber(n))
				return n.longValue() != 0;
			else
				return n.doubleValue() != 0;
		} else if (value instanceof List) {
			List<?> list = (List<?>)value;
			return !list.isEmpty();
		} else if (value instanceof Map) {
			Map<?,?> map = (Map<?,?>)value;
			return !map.isEmpty();
		} else {
			throw new RuntimeException("Invalid value class: " +
					value.getClass().getName());
		}
	}

	/**
	 * Returns whether this value equals another value. Equality is tested as
	 * follows.
	 * 
	 * <p><b>If one of the values is null</b></p>
	 * 
	 * <p>Equal if boolean evaluation of other value is false (see {@link
	 * #asBoolean() asBoolean()}).</p>
	 * 
	 * <p><b>Else if one of the values is a boolean</b></p>
	 * 
	 * <p>Compare boolean value to boolean evaluation of other value (see {@link
	 * #asBoolean() asBoolean()}).</p>
	 * 
	 * <p><b>Else if one of the values is a map</b></p>
	 * 
	 * <p>The other value can be string, number, list or map. If the other value
	 * is not a map, this method returns false.<br />
	 * If the other value is a map, this method compares the string keys and it
	 * compares the values using this method.</p>
	 * 
	 * <p><b>Else if one of the values is a list</b></p>
	 * 
	 * <p>The other value can be a string, number or list. If the other value is
	 * a string or number, convert it to a list with one element, so we have two
	 * lists. The list elements are compared using this method.</p>
	 * 
	 * <p><b>Else if one of the values is a string</b></p>
	 * 
	 * <p>The other value can be a string or number. If the other value is a
	 * number, convert it to a string, so we have two strings. Then compare the
	 * strings.</p>
	 * 
	 * <p><b>Otherwise both values are a number</b></p>
	 * 
	 * <p>Compare the number values.</p>
	 * 
	 * @param other the other value
	 * @return true if the values are equal, false otherwise
	 */
	public boolean isEqual(Value other) {
		return isEqual(this, other);
	}
	
	/**
	 * Returns whether this value is strictly equal to another value. In
	 * contrast to {@link #isEqual(Value) isEqual()}, this method also checks
	 * whether the two values have the same type. In the case of lists and maps,
	 * the elements are also tested for strict equality.
	 * 
	 * @param other the other value
	 * @return true if the values are equal, false otherwise
	 */
	public boolean isStrictEqual(Value other) {
		return isStrictEqual(this, other);
	}
	
	/**
	 * Returns a string that describes the value type. This is one of: null,
	 * string, number, boolean, list, map.
	 * 
	 * @return the type string
	 */
	public String getTypeString() {
		if (value == null) {
			return "null";
		} else if (value instanceof String) {
			return "string";
		} else if (value instanceof Number) {
			return "number";
		} else if (value instanceof Boolean) {
			return "boolean";
		} else if (value instanceof List) {
			return "list";
		} else if (value instanceof Map) {
			return "map";
		} else {
			throw new RuntimeException("Invalid value class: " +
					value.getClass().getName());
		}
	}
	
	@Override
	public String toString() {
		if (value == null) {
			return "null";
		} else if (value instanceof String) {
			return (String)value;
		} else if (value instanceof List) {
			return JsonMapper.generate(value);
		} else if (value instanceof Map) {
			return JsonMapper.generate(value);
		} else {
			return value.toString();
		}
	}
	
	/**
	 * Returns whether the specified number is an integer number (byte, short,
	 * int or long).
	 * 
	 * @param number the number
	 * @return if the number is a byte, short, int or long
	 */
	public static boolean isIntNumber(Number number) {
		return (number instanceof Byte) || (number instanceof Short) ||
				(number instanceof Integer) || (number instanceof Long);
	}

	/**
	 * Normalizes a number to Integer, Long or Double, depending on the value.
	 * 
	 * @param number the number
	 * @return the normalized number
	 */
	public static Number normalizeNumber(Number number) {
		if (isIntNumber(number)) {
			long val = number.longValue();
			if (val >= Integer.MIN_VALUE && val <= Integer.MAX_VALUE)
				return (int)val;
			else
				return val;
		} else {
			return number.doubleValue();
		}
	}
	
	private static boolean isEqual(Value val1, Value val2) {
		if (val1.isNull() || val2.isNull()) {
			return val1.isNull() && val2.isNull();
		} else if (val1.isBoolean() || val2.isBoolean()) {
			return val1.isBoolean() && val2.isBoolean() &&
					val1.asBoolean() == val2.asBoolean();
		} else if (val1.isMap() || val2.isMap()) {
			// one of the values can be string, number or list
			if (!val1.isMap() || !val2.isMap())
				return false;
			return isEqualMaps((Map<?,?>)val1.value,(Map<?,?>)val2.value);
		} else if (val1.isList() || val2.isList()) {
			// one of the values can be string or number
			List<?> list1;
			if (val1.isList())
				list1 = (List<?>)val1.value;
			else
				list1 = Collections.singletonList(val1.value);
			List<?> list2;
			if (val2.isList())
				list2 = (List<?>)val2.value;
			else
				list2 = Collections.singletonList(val2.value);
			return isEqualLists(list1, list2);
		} else if (val1.isNumber() || val2.isNumber()) {
			// one of the values can be string
			try {
				return isEqualNumbers(val1.asNumber(), val2.asNumber());
			} catch (EvaluationException ex) {
				// one of the value is a string that can't be evaluated as a
				// number
				return false;
			}
		} else {
			// both values are string
			return val1.toString().equals(val2.toString());
		}
	}

	private static boolean isEqualNumbers(Number num1, Number num2) {
		if (isIntNumber(num1) && isIntNumber(num2))
			return num1.longValue() == num2.longValue();
		else
			return num1.doubleValue() == num2.doubleValue();
	}
	
	private static boolean isEqualLists(List<?> list1, List<?> list2) {
		if (list1.size() != list2.size())
			return false;
		Iterator<?> it1 = list1.iterator();
		Iterator<?> it2 = list2.iterator();
		while (it1.hasNext()) {
			Value val1 = new Value(it1.next());
			Value val2 = new Value(it2.next());
			if (!val1.isEqual(val2))
				return false;
		}
		return true;
	}
	
	private static boolean isEqualMaps(Map<?,?> map1, Map<?,?> map2) {
		if (map1.size() != map2.size())
			return false;
		for (Object key : map1.keySet()) {
			if (!map2.containsKey(key))
				return false;
			Value val1 = new Value(map1.get(key));
			Value val2 = new Value(map2.get(key));
			if (!val1.isEqual(val2))
				return false;
		}
		return true;
	}
	
	private static boolean isStrictEqual(Value val1, Value val2) {
		if (val1.isNull()) {
			return val2.isNull();
		} else if (val1.isBoolean()) {
			return val2.isBoolean() && val1.asBoolean() == val2.asBoolean();
		} else if (val1.isString()) {
			return val2.isString() && val1.toString().equals(val2.toString());
		} else if (val1.isNumber()) {
			try {
				return val2.isNumber() && isEqualNumbers(val1.asNumber(),
						val2.asNumber());
			} catch (EvaluationException ex) {
				throw new RuntimeException("Unexpected error: " +
						ex.getMessage(), ex);
			}
		} else if (val1.isList()) {
			return val2.isList() && isStrictEqualLists((List<?>)val1.value,
					(List<?>)val2.value);
		} else {
			// val1 is a map
			return val2.isMap() && isStrictEqualMaps((Map<?,?>)val1.value,
					(Map<?,?>)val2.value);
		}
	}
	
	private static boolean isStrictEqualLists(List<?> list1, List<?> list2) {
		if (list1.size() != list2.size())
			return false;
		Iterator<?> it1 = list1.iterator();
		Iterator<?> it2 = list2.iterator();
		while (it1.hasNext()) {
			Value val1 = new Value(it1.next());
			Value val2 = new Value(it2.next());
			if (!val1.isStrictEqual(val2))
				return false;
		}
		return true;
	}
	
	private static boolean isStrictEqualMaps(Map<?,?> map1, Map<?,?> map2) {
		if (map1.size() != map2.size())
			return false;
		for (Object key : map1.keySet()) {
			if (!map2.containsKey(key))
				return false;
			Value val1 = new Value(map1.get(key));
			Value val2 = new Value(map2.get(key));
			if (!val1.isStrictEqual(val2))
				return false;
		}
		return true;
	}
	
	static class ValueSerializer extends JsonSerializer<Value> {
		@Override
		public void serialize(Value value, JsonGenerator gen,
				SerializerProvider serializers) throws IOException {
			gen.writeObject(value.value);
		}
	}
	
	static class ValueDeserializer extends JsonDeserializer<Value> {
		@Override
		public Value deserialize(JsonParser p, DeserializationContext ctxt)
				throws IOException, JsonProcessingException {
			Object value = p.readValueAs(Object.class);
			return new Value(value);
		}
	}
}
