package eu.woolplatform.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This class contains utility methods related to Java reflection.
 *
 * @author Dennis Hofs (RRD)
 */
public class ReflectionUtils {

	/**
	 * Returns the class of elements in a List field. If the field has the raw
	 * List class, this method returns Object. Otherwise it gets the raw type
	 * from the bounded parameter type using {@link
	 * #getGenericTypeClass(java.lang.reflect.Type) getGenericTypeClass()}.
	 *
	 * @param field the field
	 * @return the element type
	 */
	public static Class<?> getGenericListElementType(Field field) {
		Type genericType = field.getGenericType();
		if (genericType instanceof Class)
			return Object.class;
		if (!(genericType instanceof ParameterizedType)) {
			throw new IllegalArgumentException(
					"List field type is not a Class or ParameterizedType: " +
							genericType.getClass().getName());
		}
		Type prmType = ((ParameterizedType)genericType)
				.getActualTypeArguments()[0];
		return getGenericTypeClass(prmType);
	}

	/**
	 * Returns the class of values in a Map field. If the field has the raw
	 * Map class, this method returns Object. Otherwise it gets the raw type
	 * from the bounded parameter type using {@link #getGenericTypeClass(Type)
	 * getGenericTypeClass()}.
	 *
	 * @param field the field
	 * @return the element type
	 */
	public static Class<?> getGenericMapValueType(Field field) {
		Type genericType = field.getGenericType();
		if (genericType instanceof Class)
			return Object.class;
		if (!(genericType instanceof ParameterizedType)) {
			throw new IllegalArgumentException(
					"Map field type is not a Class or ParameterizedType: " +
							genericType.getClass().getName());
		}
		Type prmType = ((ParameterizedType)genericType)
				.getActualTypeArguments()[1];
		return getGenericTypeClass(prmType);
	}

	/**
	 * Returns the raw class of the specified generic type. It supports the
	 * following possibilities:
	 *
	 * <p><ul>
	 * <li>Type: returns Type</li>
	 * <li>Type&lt;...&gt;: returns Type</li>
	 * <li>?: returns Object</li>
	 * <li>? extends Type: returns Type</li>
	 * <li>? extends Type&lt;...&gt;: returns Type</li>
	 * </ul></p>
	 *
	 * @param type the generic type
	 * @return the raw class
	 */
	public static Class<?> getGenericTypeClass(Type type) {
		if (type instanceof Class) {
			// Type
			return (Class<?>)type;
		} else if (type instanceof ParameterizedType) {
			// Type<...>
			ParameterizedType prmType = (ParameterizedType)type;
			return (Class<?>)prmType.getRawType();
		} else if (type instanceof WildcardType) {
			// ?
			// ? extends Type
			// ? extends Type<...>
			WildcardType wildcardType = (WildcardType)type;
			Type upperType = wildcardType.getUpperBounds()[0];
			return getGenericTypeClass(upperType);
		} else {
			throw new IllegalArgumentException(
					"Bounded type parameter is not a Class, ParameterizedType or WildcardType: " +
					type.getClass().getName());
		}
	}

	/**
	 * Tries to invoke a constructor of the specified class and return the
	 * result.
	 *
	 * @param clazz the class that should be constructed
	 * @param params the parameters
	 * @param <T> the class type
	 * @return the new instance
	 * @throws InstantiationException if the constructor can't be invoked (for
	 * example the class or constructor is not public or the class is abstract
	 * or no matching constructor for the specified parameters is found)
	 * @throws InvocationTargetException if the constructor throws an exception
	 */
	public static <T> T newInstance(Class<T> clazz, Object... params)
			throws InstantiationException, InvocationTargetException {
		Constructor<?> constr = findConstructor(clazz, params);
		Object result;
		try {
			result = constr.newInstance(params);
		} catch (IllegalAccessException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
		return clazz.cast(result);
	}

	/**
	 * Tries to invoke a method on the specified object and return the result.
	 * It checks whether the method is public, non-static and non-abstract.
	 * If no method is found that matches the specified result class, method
	 * name and parameter list, this method throws a {@link
	 * NoSuchMethodException NoSuchMethodException}. If you set the result
	 * class to null, this method returns null, even if the target method
	 * returns a value. If the target method is a void method, you must set
	 * the result class to null.
	 *
	 * @param object the object on which the method should be called
	 * @param resultClass the desired result or null
	 * @param methodName the method name
	 * @param params the parameters
	 * @param <T> the result type
	 * @return the result or null
	 * @throws NoSuchMethodException if no matching method is found
	 * @throws InvocationTargetException if the target method throws an
	 * exception
	 */
	public static <T> T invokeMethod(Object object, Class<T> resultClass,
			String methodName, Object... params) throws NoSuchMethodException,
			InvocationTargetException {
		Method method = findMethod(object.getClass(), false, resultClass,
				methodName, params);
		Object result;
		try {
			result = method.invoke(object, params);
		} catch (IllegalAccessException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
		if (resultClass == null || result == null)
			return null;
		else
			return resultClass.cast(result);
	}

	/**
	 * Tries to invoke a static method on the specified class and return the
	 * result. It checks whether the method is public, static and non-abstract.
	 * If no method is found that matches the specified result class, method
	 * name and parameter list, this method throws a {@link
	 * NoSuchMethodException NoSuchMethodException}. If you set the result
	 * class to null, this method returns null, even if the target method
	 * returns a value. If the target method is a void method, you must set
	 * the result class to null.
	 *
	 * @param clazz the class on which the method should be called
	 * @param resultClass the desired result or null
	 * @param methodName the method name
	 * @param params the parameters
	 * @param <T> the result type
	 * @return the result or null
	 * @throws NoSuchMethodException if no matching method is found
	 * @throws InvocationTargetException if the target method throws an
	 * exception
	 */
	public static <T> T invokeStaticMethod(Class<?> clazz, Class<T> resultClass,
			String methodName, Object... params) throws NoSuchMethodException,
			InvocationTargetException {
		Method method = findMethod(clazz, true, resultClass, methodName,
				params);
		Object result;
		try {
			result = method.invoke(null, params);
		} catch (IllegalAccessException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
		if (resultClass == null || result == null)
			return null;
		else
			return resultClass.cast(result);
	}

	/**
	 * Tries to get the value of a field from the specified object. It checks
	 * whether the field is public and non-static. If no field is found that
	 * matches the specified result class and field name, this method throws a
	 * {@link NoSuchFieldException NoSuchFieldException}.
	 *
	 * @param object the object from which the field value should be retrieved
	 * @param resultClass the desired result type
	 * @param fieldName the field name
	 * @param <T> the result type
	 * @return the field value
	 * @throws NoSuchFieldException if no matching field is found
	 */
	public static <T> T getField(Object object, Class<T> resultClass,
			String fieldName) throws NoSuchFieldException {
		Field field = findField(object.getClass(), false, resultClass,
				fieldName);
		Object result;
		try {
			result = field.get(object);
		} catch (IllegalAccessException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
		if (result == null)
			return null;
		else
			return resultClass.cast(result);
	}

	/**
	 * Tries to get the value of a static field from the specified class. It
	 * checks whether the field is public and static. If no field is found that
	 * matches the specified result class and field name, this method throws a
	 * {@link NoSuchFieldException NoSuchFieldException}.
	 *
	 * @param clazz the class from which the field value should be retrieved
	 * @param resultClass the desired result type
	 * @param fieldName the field name
	 * @param <T> the result type
	 * @return the field value
	 * @throws NoSuchFieldException if no matching field is found
	 */
	public static <T> T getStaticField(Class<?> clazz, Class<T> resultClass,
			String fieldName) throws NoSuchFieldException {
		Field field = findField(clazz, true, resultClass, fieldName);
		Object result;
		try {
			result = field.get(null);
		} catch (IllegalAccessException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
		if (result == null)
			return null;
		else
			return resultClass.cast(result);
	}

	/**
	 * Tries to find a constructor for the specified criteria. It will check
	 * whether the class is public and non-abstract and the constructor is
	 * public.
	 *
	 * @param clazz the class that should have the constructor
	 * @param params the parameters
	 * @return the constructor
	 * @throws InstantiationException if the class is not public and
	 * non-abstract, or no matching constructor is found
	 */
	private static Constructor<?> findConstructor(Class<?> clazz,
			Object... params) throws InstantiationException {
		int modifiers = clazz.getModifiers();
		if ((modifiers & Modifier.PUBLIC) == 0) {
			throw new InstantiationException("Class " + clazz.getName() +
					" is not public");
		}
		if ((modifiers & Modifier.ABSTRACT) != 0) {
			throw new InstantiationException("Class " + clazz.getName() +
					" is abstract");
		}
		Constructor<?>[] constrs = clazz.getConstructors();
		for (Constructor<?> constr : constrs) {
			modifiers = constr.getModifiers();
			if ((modifiers & Modifier.PUBLIC) == 0)
				continue;
			if ((modifiers & Modifier.ABSTRACT) != 0)
				continue;
			Class<?>[] paramTypes = constr.getParameterTypes();
			if (paramTypes.length != params.length)
				continue;
			boolean paramsMatch = true;
			for (int i = 0; paramsMatch && i < params.length; i++) {
				Object param = params[i];
				Class<?> paramType = paramTypes[i];
				if (param == null && paramType.isPrimitive())
					paramsMatch = false;
				else if (param != null && !paramType.isInstance(param))
					paramsMatch = false;
			}
			if (!paramsMatch)
				continue;
			return constr;
		}
		StringBuilder paramString = new StringBuilder();
		for (Object param : params) {
			if (paramString.length() > 0)
				paramString.append(", ");
			if (param == null)
				paramString.append("null");
			else
				paramString.append(param.getClass().getName());
		}
		throw new InstantiationException("Class " + clazz.getName() +
				" does not have public constructor for parameters (" +
				paramString + ")");
	}

	/**
	 * Tries to find a method for the specified criteria. It will check whether
	 * the method is public and non-abstract. If you specify a result class,
	 * it will check whether the return type of the method is assignable to
	 * that class.
	 *
	 * @param clazz the class that should have the method
	 * @param isStatic true if the method must be static, false if it must be
	 * non-static
	 * @param resultClass the desired result class or null to accept any result
	 * including void
	 * @param methodName the method name
	 * @param params the parameters
	 * @return the method
	 * @throws NoSuchMethodException if no matching method is found
	 */
	private static Method findMethod(Class<?> clazz, boolean isStatic,
			Class<?> resultClass, String methodName, Object... params)
			throws NoSuchMethodException {
		Method[] methods = clazz.getMethods();
		for (Method method : methods) {
			int modifiers = method.getModifiers();
			if (!method.getName().equals(methodName))
				continue;
			if ((modifiers & Modifier.PUBLIC) == 0)
				continue;
			boolean currentIsStatic = (modifiers & Modifier.STATIC) != 0;
			if (isStatic != currentIsStatic)
				continue;
			if ((modifiers & Modifier.ABSTRACT) != 0)
				continue;
			Class<?>[] paramTypes = method.getParameterTypes();
			if (paramTypes.length != params.length)
				continue;
			boolean paramsMatch = true;
			for (int i = 0; paramsMatch && i < params.length; i++) {
				Object param = params[i];
				Class<?> paramType = paramTypes[i];
				if (param == null && paramType.isPrimitive())
					paramsMatch = false;
				else if (param != null && !paramType.isInstance(param))
					paramsMatch = false;
			}
			if (!paramsMatch)
				continue;
			if (resultClass != null) {
				if (method.getReturnType() == null)
					continue;
				Class<?> resultObjClass = primitiveTypeToObjectClass(
						resultClass);
				Class<?> returnObjClass = primitiveTypeToObjectClass(
						method.getReturnType());
				if (!resultObjClass.isAssignableFrom(returnObjClass))
					continue;
			}
			return method;
		}
		StringBuilder paramString = new StringBuilder();
		for (Object param : params) {
			if (paramString.length() > 0)
				paramString.append(", ");
			if (param == null)
				paramString.append("null");
			else
				paramString.append(param.getClass().getName());
		}
		String error = "Class " + clazz.getName() + " does not have public, " +
				(isStatic ? "static" : "non-static") +
				", non-abstract method " + methodName + " for parameters (" +
				paramString + ")";
		if (resultClass != null)
			error += " with result assignable to " + resultClass.getName();
		throw new NoSuchMethodException(error);
	}

	/**
	 * Tries to find a field for the specified criteria. It will check whether
	 * the field is public.
	 *
	 * @param clazz the class that should have the field
	 * @param isStatic true if the field must be static, false if it must be
	 * non-static
	 * @param resultClass the desired result class (it must be assignable from
	 * the field type)
	 * @param fieldName the field name
	 * @return the field
	 * @throws NoSuchFieldException if no matching field is found
	 */
	private static Field findField(Class<?> clazz, boolean isStatic,
			Class<?> resultClass, String fieldName) throws NoSuchFieldException {
		Field[] fields = clazz.getFields();
		for (Field field : fields) {
			int modifiers = field.getModifiers();
			if (!field.getName().equals(fieldName))
				continue;
			if ((modifiers & Modifier.PUBLIC) == 0)
				continue;
			boolean currentIsStatic = (modifiers & Modifier.STATIC) != 0;
			if (isStatic != currentIsStatic)
				continue;
			if (resultClass != null) {
				Class<?> resultObjClass = primitiveTypeToObjectClass(
						resultClass);
				Class<?> fieldObjClass = primitiveTypeToObjectClass(
						field.getType());
				if (!resultObjClass.isAssignableFrom(fieldObjClass))
					continue;
			}
			return field;
		}
		String error = "Class " + clazz.getName() + " does not have public, " +
				(isStatic ? "static" : "non-static") + " field " + fieldName;
		if (resultClass != null)
			error += " assignable to type " + resultClass.getName();
		throw new NoSuchFieldException(error);
	}

	/**
	 * If the specified class is a primitive type, this method returns its class
	 * equivalent. For example if the specified class is "int", it returns
	 * "Integer". If the specified class is not a primitive type, this method
	 * just returns the class.
	 *
	 * @param clazz the class or primitive type
	 * @return the class
	 */
	public static Class<?> primitiveTypeToObjectClass(Class<?> clazz) {
		if (clazz == Byte.TYPE)
			return Byte.class;
		if (clazz == Short.TYPE)
			return Short.class;
		if (clazz == Integer.TYPE)
			return Integer.class;
		if (clazz == Long.TYPE)
			return Long.class;
		if (clazz == Float.TYPE)
			return Float.class;
		if (clazz == Double.TYPE)
			return Double.class;
		if (clazz == Boolean.TYPE)
			return Boolean.class;
		return clazz;
	}

	/**
	 * Returns a map with all integer constants whose names start with the
	 * specified prefix.
	 *
	 * @param clazz the class where the constants are searched
	 * @param prefix the prefix of the constant names
	 * @return a map from constant name to constant value
	 */
	public static Map<String,Integer> getConstantIntMap(Class<?> clazz,
			String prefix) {
		return getConstantMap(clazz, prefix, Integer.class);
	}

	/**
	 * Returns a map with all string constants whose names start with the
	 * specified prefix.
	 *
	 * @param clazz the class where the constants are searched
	 * @param prefix the prefix of the constant names
	 * @return a map from constant name to constant value
	 */
	public static Map<String,String> getConstantStringMap(Class<?> clazz,
			String prefix) {
		return getConstantMap(clazz, prefix, String.class);
	}
	
	/**
	 * Returns a map with constant names and values that are defined in the
	 * "constantClass", whose names start with the specified prefix and
	 * whose values can be assigned to the specified class.
	 * 
	 * @param constantClass the class that defines the constants
	 * @param prefix the prefix of the constant names
	 * @param valueClass the value class
	 * @param <T> the type of values
	 * @return a map from constant name to constant value
	 */
	public static <T> Map<String,T> getConstantMap(Class<?> constantClass,
			String prefix, Class<T> valueClass) {
		Map<String,T> map = new LinkedHashMap<>();
		Field[] fields = constantClass.getFields();
		for (Field field : fields) {
			int modifiers = field.getModifiers();
			if ((modifiers & Modifier.PUBLIC) == 0 ||
					(modifiers & Modifier.STATIC) == 0 ||
					(modifiers & Modifier.FINAL) == 0 ||
					!field.getName().startsWith(prefix)) {
				continue;
			}
			Class<?> fieldObjClass = primitiveTypeToObjectClass(field.getType());
			if (!valueClass.isAssignableFrom(fieldObjClass))
				continue;
			Object fieldObjValue;
			try {
				fieldObjValue = field.get(null);
			} catch (IllegalAccessException ex) {
				throw new RuntimeException("Can't read field: " +
						field.getName() + ": " + ex.getMessage(), ex);
			}
			T fieldValue = valueClass.cast(fieldObjValue);
			map.put(field.getName(), fieldValue);
		}
		return map;
	}

	/**
	 * Tries to find an int constant with the specified value whose name starts
	 * with the specified prefix. It returns the name of the constant. If no
	 * such constant is found, it returns the integer value as a string.
	 *
	 * @param clazz the class where the constant is searched
	 * @param prefix the prefix of the constant name
	 * @param value the constant value
	 * @return the constant name or the integer value as a string
	 */
	public static String findConstantInt(Class<?> clazz, String prefix,
			int value) {
		Map<String,Integer> constants = getConstantIntMap(clazz, prefix);
		for (String key : constants.keySet()) {
			if (constants.get(key) == value)
				return key;
		}
		return Integer.toString(value);
	}
}
