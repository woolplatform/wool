package eu.woolplatform.utils.xml;

import org.xml.sax.Attributes;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import eu.woolplatform.utils.exception.ParseException;

/**
 * This abstract implementation of {@link SimpleSAXHandler SimpleSAXHandler}
 * contains convenient methods to parse attribute values.
 * 
 * @author Dennis Hofs
 *
 * @param <T> the type of objects returned by the handler
 */
public abstract class AbstractSimpleSAXHandler<T> implements
SimpleSAXHandler<T> {
	
	/**
	 * Tries to read the value of an attribute.
	 * 
	 * @param atts the attributes
	 * @param name the attribute name
	 * @return the attribute value
	 * @throws ParseException if the attribute doesn't exist
	 */
	protected String readAttribute(Attributes atts, String name)
	throws ParseException {
		String s = atts.getValue(name);
		if (s == null)
			throw new ParseException("Attribute \"" + name + "\" not found");
		return s;
	}

	/**
	 * Tries to read the value of an attribute and validates the length of the
	 * value.
	 *
	 * @param atts the attributes
	 * @param name the attribute name
	 * @param minLength the minimum length or null
	 * @param maxLength the maximum length or null
	 * @return the attribute value
	 * @throws ParseException if the attribute doesn't exist or doesn't have
	 * the required length
	 */
	protected String readAttribute(Attributes atts, String name,
			Integer minLength, Integer maxLength) throws ParseException {
		String s = readAttribute(atts, name);
		if (minLength == null && maxLength == null)
			return s;
		String lengthError;
		if (minLength != null && maxLength != null) {
			lengthError = String.format(
					"Value of attribute \"%s\" must be between %s and %s characters",
					name, minLength, maxLength);
		} else if (minLength != null) {
			lengthError = String.format(
					"Value of attribute \"%s\" must be at least %s characters",
					name, minLength);
		} else {
			lengthError = String.format(
					"Value of attribute \"%s\" must be at most %s characters",
					name, maxLength);
		}
		if (minLength != null && s.length() < minLength)
			throw new ParseException(lengthError);
		if (maxLength != null && s.length() > maxLength)
			throw new ParseException(lengthError);
		return s;
	}
	
	/**
	 * Tries to read an integer value of an attribute.
	 * 
	 * @param atts the attributes
	 * @param name the attribute name
	 * @return the attribute value
	 * @throws ParseException if the attribute doesn't exist or its value is invalid
	 */
	protected int readIntAttribute(Attributes atts, String name)
	throws ParseException {
		String s = readAttribute(atts, name);
		try {
			return Integer.parseInt(s);
		} catch (NumberFormatException ex) {
			throw new ParseException("Invalid value of attribute \"" + name +
					"\": " + s, ex);
		}
	}

	/**
	 * Tries to read a float value of an attribute.
	 *
	 * @param atts the attributes
	 * @param name the attribute name
	 * @return the attribute value
	 * @throws ParseException if the attribute doesn't exist or its value is
	 * invalid
	 */
	protected float readFloatAttribute(Attributes atts, String name)
	throws ParseException {
		String s = readAttribute(atts, name);
		try {
			return Float.parseFloat(s);
		} catch (NumberFormatException ex) {
			throw new ParseException("Invalid value of attribute \"" + name +
					"\": " + s, ex);
		}
	}

	/**
	 * Tries to read a boolean value of an attribute.
	 * 
	 * @param atts the attributes
	 * @param name the attribute name
	 * @return the attribute value
	 * @throws ParseException if the attribute doesn't exist or its value is invalid
	 */
	protected boolean readBooleanAttribute(Attributes atts, String name)
	throws ParseException {
		String s = readAttribute(atts, name);
		switch (s.toLowerCase()) {
			case "true":
				return true;
			case "false":
				return false;
			default:
				throw new ParseException("Invalid value of attribute \"" +
						name + "\": " + s);
		}
	}
	
	/**
	 * Tries to read an enum value of an attribute.
	 * 
	 * @param <U> the enum type
	 * @param clazz the enum class
	 * @param atts the attributes
	 * @param name the attribute name
	 * @return the attribute value
	 * @throws ParseException if the attribute doesn't exist or its value is invalid
	 */
	protected <U extends Enum<U>> U readEnumAttribute(Class<U> clazz,
			Attributes atts, String name) throws ParseException {
		String s = readAttribute(atts, name);
		Object[] array;
		try {
			Method method = clazz.getMethod("values");
			array = (Object[])method.invoke(null);
		} catch (NoSuchMethodException ex) {
			throw new ParseException("Can't get enum values: " +
					ex.getMessage(), ex);
		} catch (InvocationTargetException ex) {
			throw new ParseException("Can't get enum values: " +
					ex.getMessage(), ex);
		} catch (IllegalAccessException ex) {
			throw new ParseException("Can't get enum values: " +
					ex.getMessage(), ex);
		}
		for (Object item : array) {
			if (item.toString().toLowerCase().equals(s.toLowerCase())) {
				return clazz.cast(item);
			}
		}
		throw new ParseException("Invalid value of attribute \"" + name +
				"\": " + s);
	}
	
	/**
	 * Tries to read an attribute with the path to a file or directory. This
	 * method tries to return a canonical path. If that fails, it will print
	 * an error and return an absolute path. If the attribute value is a
	 * relative path, it will be made absolute with respect to the current
	 * directory.
	 * 
	 * @param atts the attributes
	 * @param name the attribute name
	 * @return the attribute value
	 * @throws ParseException if the attribute doesn't exist or its value is
	 * invalid
	 */
	protected File readFileAttribute(Attributes atts, String name)
	throws ParseException {
		return readFileAttribute(atts, name, null);
	}

	/**
	 * Tries to read an attribute with the path to a file or directory. This
	 * method tries to return a canonical path. If that fails, it will print
	 * an error and return an absolute path. If the attribute value is a
	 * relative path, it will be made absolute with respect to the specified
	 * parent or the current directory (if the parent is not specified).
	 * 
	 * @param atts the attributes
	 * @param name the attribute name
	 * @param parent a directory to resolve a relative path or null
	 * @return the attribute value
	 * @throws ParseException if the attribute doesn't exist or its value is
	 * invalid
	 */
	protected File readFileAttribute(Attributes atts, String name,
			File parent) throws ParseException {
		String s = readAttribute(atts, name);
		File path = new File(s);
		if (!path.isAbsolute() && parent != null) {
			path = new File(parent, s);
		}
		File absPath;
		try {
			absPath = path.getCanonicalFile();
		} catch (Exception ex) {
			absPath = path.getAbsoluteFile();
			System.err.println("WARNING: Can't get canonical path for file: " +
					absPath.getAbsolutePath());
		}
		return absPath;
	}
	
	/**
	 * Reads an attribute value and validates whether it matches the specified
	 * regular expression.
	 * 
	 * @param atts the attributes
	 * @param name the attribute name
	 * @param regex the regular expression
	 * @return the attribute value
	 * @throws ParseException if the attribute doesn't exist or its value is
	 * invalid
	 */
	protected String readRegexAttribute(Attributes atts, String name,
			String regex) throws ParseException {
		String s = readAttribute(atts, name);
		if (!s.matches(regex)) {
			throw new ParseException("Invalid value of attribute \"" + name +
					"\": " + s);
		}
		return s;
	}
}
