package eu.woolplatform.utils.beans;

import java.lang.reflect.InvocationTargetException;

/**
 * This class can write the value of a property in a JavaBeans-like object. A
 * property may be accessed by a public field or getter and setter methods.
 * 
 * @see PropertyScanner
 * @author Dennis Hofs (RRD)
 */
public class PropertyWriter {
	
	/**
	 * Writes the value of the specified property.
	 * 
	 * @param obj the object
	 * @param property the property name
	 * @param value the property value
	 */
	public static void writeProperty(Object obj, String property,
			Object value) {
		PropertySpec propSpec = PropertyScanner.getProperty(obj.getClass(),
				property);
		writeProperty(obj, propSpec, value);
	}

	/**
	 * Writes the value of the specified property.
	 * 
	 * @param obj the object
	 * @param propSpec the property specification
	 * @param value the property value
	 */
	public static void writeProperty(Object obj, PropertySpec propSpec,
			Object value) {
		Exception exception = null;
		try {
			if (propSpec.isPublic()) {
				propSpec.getField().set(obj, value);
			} else {
				propSpec.getSetMethod().invoke(obj, value);
			}
		} catch (IllegalAccessException ex) {
			exception = ex;
		} catch (IllegalArgumentException ex) {
			exception = ex;
		} catch (InvocationTargetException ex) {
			exception = ex;
		}
		if (exception != null) {
			throw new RuntimeException("Can't write property \"" +
					propSpec.getName() + "\": " + exception.getMessage(),
					exception);
		}
	}
}
