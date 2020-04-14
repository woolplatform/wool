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
