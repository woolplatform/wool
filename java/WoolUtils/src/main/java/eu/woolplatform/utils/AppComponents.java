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

import eu.woolplatform.utils.exception.BuildException;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

/**
 * This class defines a set of application-wide components. You may create
 * and add components yourself, or this class can create default instances on
 * the fly.
 *
 * <p>The method {@link #getComponent(Class) getComponent()} returns components.
 * If a component is requested that does not exist yet, it will try to create a
 * default instance. It will try the default constructor or static method
 * getInstance().</p>
 *
 * @author Dennis Hofs (RRD)
 */
public class AppComponents {
	private static AppComponents instance = null;
	private static final Object lock = new Object();

	/**
	 * Returns the singleton instance of this class.
	 * 
	 * @return the singleton instance of this class
	 */
	public static AppComponents getInstance() {
		synchronized (lock) {
			if (instance == null)
				instance = new AppComponents();
			return instance;
		}
	}

	/**
	 * This is a shortcut method to get a logger. It will try to get an app
	 * component of class {@link ILoggerFactory ILoggerFactory} from SLF4J.
	 * If it doesn't exist, it will get the default SLF4J factory (which is
	 * then added as an app component). Then it gets the logger with the
	 * specified name from that factory.
	 *
	 * @param name the logger name
	 * @return the logger
	 */
	public static Logger getLogger(String name) {
		ILoggerFactory factory = AppComponents.getInstance().getComponent(
				ILoggerFactory.class, LoggerFactory.getILoggerFactory());
		return factory.getLogger(name);
	}

	/**
	 * This is a shortcut method for getInstance().getComponent(). It tries to
	 * find a component of the specified class or a subclass. If no such
	 * component is found, it will try to create a default instance of the
	 * specified class. The new component will then be added to the collection
	 * so the same instance can be retrieved later.
	 *
	 * @param clazz the component class
	 * @param <T> the type of component
	 * @return the component
	 * @throws RuntimeException if a default instance cannot be created
	 */
	public static <T> T get(Class<T> clazz) throws RuntimeException {
		return getInstance().getComponent(clazz);
	}
	
	private Set<Object> components = new HashSet<>();

	/**
	 * This private constructor is used in {@link #getInstance()
	 * getInstance()}.
	 */
	private AppComponents() {
	}

	/**
	 * Adds the specified component.
	 * 
	 * @param component the component
	 */
	public void addComponent(Object component) {
		synchronized (lock) {
			components.add(component);
		}
	}
	
	/**
	 * Returns whether there is a component of the specified class or a
	 * subclass.
	 * 
	 * @param clazz the component class
	 * @return true if there is a component, false otherwise
	 */
	public boolean hasComponent(Class<?> clazz) {
		return findComponent(clazz) != null;
	}
	
	/**
	 * Tries to find a component of the specified class or a subclass. If no
	 * such component is found, this method returns null.
	 * 
	 * @param clazz the component class
	 * @param <T> the type of component
	 * @return the component or null
	 */
	public <T> T findComponent(Class<T> clazz) {
		synchronized (lock) {
			for (Object comp : components) {
				Class<?> compClass = comp.getClass();
				if (clazz.isAssignableFrom(compClass))
					return clazz.cast(comp);
			}
			return null;
		}
	}
	
	/**
	 * Tries to find a component of the specified class or a subclass. If no
	 * such component is found, it will try to create a default instance of
	 * the specified class. The new component will then be added to the
	 * collection so the same instance can be retrieved later.
	 * 
	 * @param clazz the component class
	 * @param <T> the type of component
	 * @return the component
	 * @throws RuntimeException if a default instance cannot be created
	 */
	public <T> T getComponent(Class<T> clazz) throws RuntimeException {
		return getComponent(clazz, null);
	}

	/**
	 * Tries to find a component of the specified class or a subclass. If no
	 * such component is found, it will try to create a default instance of
	 * the specified class. The new component will be added to the collection so
	 * the same instance can be retrieved later.
	 *
	 * <p>If creating a default instance fails and a default value is specified,
	 * it will return that default value and also add it to the collection. If
	 * no default value is specified, it throws a runtime exception.</p>
	 *
	 * @param clazz the component class
	 * @param defaultVal the default value to return if there is no component of
	 * the specified class and no default instance can be created. Set this to
	 * null if you want a runtime exception in that case.
	 * @param <T> the type of component
	 * @return the component
	 * @throws RuntimeException if a default instance cannot be created and
	 * no default value is specified
	 */
	public <T> T getComponent(Class<T> clazz, T defaultVal) {
		synchronized (lock) {
			T comp = findComponent(clazz);
			if (comp != null)
				return comp;
			try {
				comp = getDefaultComponent(clazz);
				components.add(comp);
				return comp;
			} catch (BuildException ex) {
				if (defaultVal != null) {
					components.add(defaultVal);
					return defaultVal;
				} else {
					throw new RuntimeException(
							"Can't create default component of class " +
							clazz.getName() + ": " + ex.getMessage(), ex);
				}
			}
		}
	}
	
	/**
	 * Creates a default instance of a component of the specified class. It
	 * will try the default constructor or static method getInstance().
	 * 
	 * @param clazz the component class
	 * @return the default component
	 * @throws BuildException if the default component cannot be created
	 */
	private <T> T getDefaultComponent(Class<T> clazz) throws BuildException {
		boolean isAbstract = (clazz.getModifiers() & Modifier.ABSTRACT) != 0;
		Constructor<T> cstr = null;
		if (!isAbstract) {
			try {
				cstr = clazz.getConstructor();
			} catch (NoSuchMethodException ex) {
				// try getInstance()
			}
		}
		if (cstr != null) {
			try {
				return cstr.newInstance();
			} catch (InvocationTargetException ex) {
				Throwable targetEx = ex.getTargetException();
				throw new BuildException("Default constructor of class " +
						clazz.getName() + " throws exception: " +
						targetEx.getMessage(), targetEx);
			} catch (Exception ex) {
				throw new BuildException("Can't instantiate class " +
						clazz.getName() + " with default constructor: " +
						ex.getMessage(), ex);
			}
		}
		// class is abstract or default constructor is not available
		Method method;
		try {
			method = clazz.getMethod("getInstance");
		} catch (NoSuchMethodException ex) {
			throw new BuildException("Class " + clazz.getName() +
					" cannot be instantiated and does not have static method getInstance(): " +
					ex.getMessage(), ex);
		}
		boolean isStatic = (method.getModifiers() & Modifier.STATIC) != 0;
		if (!isStatic) {
			throw new BuildException("Class " + clazz.getName() +
					" cannot be instantiated and does not have static method getInstance()");
		}
		Object result;
		try { 
			result = method.invoke(null);
		} catch (InvocationTargetException ex) {
			Throwable targetEx = ex.getTargetException();
			throw new BuildException("Method getInstance() of class " +
					clazz.getName() + " throws exception: " +
					targetEx.getMessage(), targetEx);
		} catch (Exception ex) {
			throw new BuildException(
					"Can't invoke method getInstance() of class " +
					clazz.getName() + ": " + ex.getMessage(), ex);
		}
		if (result == null) {
			throw new BuildException("Method getInstance() of class " +
					clazz.getName() + " returns null");
		}
		try {
			return clazz.cast(result);
		} catch (ClassCastException ex) {
			throw new BuildException("Class " + result.getClass().getName() +
					" cannot be cast to " + clazz.getName());
		}
	}
}
