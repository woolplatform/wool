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

package eu.woolplatform.utils.beans;

import java.lang.reflect.InvocationTargetException;

/**
 * This class can read the value of a property from a JavaBeans-like object. A
 * property may be accessed by a public field or getter and setter methods.
 * 
 * @see PropertyScanner
 * @author Dennis Hofs (RRD)
 */
public class PropertyReader {
	
	/**
	 * Reads the value of the specified property.
	 * 
	 * @param obj the object
	 * @param property the property name
	 * @return the property value
	 */
	public static Object readProperty(Object obj, String property) {
		PropertySpec propSpec = PropertyScanner.getProperty(obj.getClass(),
				property);
		return readProperty(obj, propSpec);
	}

	/**
	 * Reads the value of the specified property.
	 * 
	 * @param obj the object
	 * @param propSpec the property specification
	 * @return the property value
	 */
	public static Object readProperty(Object obj, PropertySpec propSpec) {
		Object value = null;
		Exception exception = null;
		try {
			if (propSpec.isPublic()) {
				value = propSpec.getField().get(obj);
			} else {
				value = propSpec.getGetMethod().invoke(obj);
			}
		} catch (IllegalAccessException ex) {
			exception = ex;
		} catch (IllegalArgumentException ex) {
			exception = ex;
		} catch (InvocationTargetException ex) {
			exception = ex;
		}
		if (exception != null) {
			throw new RuntimeException("Can't read property \"" +
					propSpec.getName() + "\": " + exception.getMessage(),
					exception);
		}
		return value;
	}
}
