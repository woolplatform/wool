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

package eu.woolplatform.utils.log;

/**
 * This factory can create log delegates with default settings.
 * 
 * @author Dennis Hofs
 */
public abstract class LogDelegateFactory {
	private static LogDelegateFactory instance = null;
	private static Object lock = new Object();
	
	/**
	 * Returns the log delegate factory that was set with {@link
	 * #setInstance(LogDelegateFactory) setInstance()}. If no factory was set,
	 * it will return a {@link DefaultLogDelegateFactory
	 * DefaultLogDelegateFactory}.
	 * 
	 * @return the log delegate factory
	 */
	public static LogDelegateFactory getInstance() {
		synchronized (lock) {
			if (instance == null)
				instance = new DefaultLogDelegateFactory();
			return instance;
		}
	}
	
	/**
	 * Sets the log delegate factory that should be returned by {@link
	 * #getInstance() getInstance()}.
	 * 
	 * @param factory the factory
	 */
	public static void setInstance(LogDelegateFactory factory) {
		synchronized (lock) {
			instance = factory;
		}
	}
	
	/**
	 * Creates a new log delegate.
	 * 
	 * @return the log delegate
	 */
	public abstract AbstractLogDelegate createLogDelegate();
}
