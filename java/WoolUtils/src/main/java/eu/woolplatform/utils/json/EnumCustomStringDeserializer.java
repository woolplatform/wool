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

package eu.woolplatform.utils.json;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * This deserializer can read a string value or null and convert it to an enum
 * using a static method T fromStringValue().
 * 
 * @author Dennis Hofs (RRD)
 *
 * @param <T> the enum type
 */
public class EnumCustomStringDeserializer<T extends Enum<?>> extends
JsonDeserializer<T> {
	private Class<T> enumClass;
	
	/**
	 * Constructs a new instance. The enum class must have a static method
	 * fromStringValue(String s).
	 * 
	 * @param enumClass the enum class
	 */
	public EnumCustomStringDeserializer(Class<T> enumClass) {
		this.enumClass = enumClass;
	}
	
	@Override
	public T deserialize(JsonParser p, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		if (!p.getCurrentToken().isScalarValue()) {
			throw new JsonParseException(p,
					"Expected string, found non-scalar value");
		}
		String s = p.getValueAsString();
		if (s == null)
			return null;
		Exception exception = null;
		T result = null;
		try {
			Method method = enumClass.getMethod("fromStringValue",
					String.class);
			Object resultObj = method.invoke(null, s);
			result = enumClass.cast(resultObj);
		} catch (NoSuchMethodException | InvocationTargetException |
				IllegalAccessException | IllegalArgumentException ex) {
			exception = ex;
		}
		if (exception != null) {
			throw new RuntimeException("Can't invoke fromStringValue(): " +
					exception.getMessage(), exception);
		}
		return result;
	}
}
