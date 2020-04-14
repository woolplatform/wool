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
 * A log delegate can be set to the {@link Logger Logger}. It determines how
 * log messages are written (e.g. to a file, to standard output, to a
 * database) and what levels of log messages are written.
 * 
 * @author Dennis Hofs
 */
public interface LogDelegate {
	/**
	 * Writes a message at level {@link Logger#DEBUG DEBUG}.
	 * 
	 * @param tag the tag
	 * @param msg the message
	 * @param tr an exception
	 * @return 0 if no error occurred, an error code otherwise
	 */
	public int d(String tag, String msg, Throwable tr);

	/**
	 * Writes a message at level {@link Logger#DEBUG DEBUG}.
	 * 
	 * @param tag the tag
	 * @param msg the message
	 * @return 0 if no error occurred, an error code otherwise
	 */
	public int d(String tag, String msg);

	/**
	 * Writes a message at level {@link Logger#ERROR ERROR}.
	 * 
	 * @param tag the tag
	 * @param msg the message
	 * @return 0 if no error occurred, an error code otherwise
	 */
	public int e(String tag, String msg);

	/**
	 * Writes a message at level {@link Logger#ERROR ERROR}.
	 * 
	 * @param tag the tag
	 * @param msg the message
	 * @param tr an exception
	 * @return 0 if no error occurred, an error code otherwise
	 */
	public int e(String tag, String msg, Throwable tr);

	/**
	 * Returns the stack trace string for the specified exception. The returned
	 * string probably contains new lines, but it does not have a trailing new
	 * line.
	 * 
	 * @param tr the exception
	 * @return the stack trace string
	 */
	public String getStackTraceString(Throwable tr);

	/**
	 * Writes a message at level {@link Logger#INFO INFO}.
	 * 
	 * @param tag the tag
	 * @param msg the message
	 * @param tr an exception
	 * @return 0 if no error occurred, an error code otherwise
	 */
	public int i(String tag, String msg, Throwable tr);

	/**
	 * Writes a message at level {@link Logger#INFO INFO}.
	 * 
	 * @param tag the tag
	 * @param msg the message
	 * @return 0 if no error occurred, an error code otherwise
	 */
	public int i(String tag, String msg);

	/**
	 * Determines if messages at the specified level are logged for the
	 * specified tag.
	 * 
	 * @param tag the tag
	 * @param level the level
	 * @return true if the message will be logged, false otherwise
	 */
	public boolean isLoggable(String tag, int level);

	/**
	 * Writes a log message.
	 * 
	 * @param priority the log level
	 * @param tag the tag
	 * @param msg the message
	 * @return 0 if no error occurred, an error code otherwise
	 */
	public int println(int priority, String tag, String msg);

	/**
	 * Writes a message at level {@link Logger#VERBOSE VERBOSE}.
	 * 
	 * @param tag the tag
	 * @param msg the message
	 * @param tr an exception or null
	 * @return 0 if no error occurred, an error code otherwise
	 */
	public int v(String tag, String msg, Throwable tr);

	/**
	 * Writes a message at level {@link Logger#VERBOSE VERBOSE}.
	 * 
	 * @param tag the tag
	 * @param msg the message
	 * @return 0 if no error occurred, an error code otherwise
	 */
	public int v(String tag, String msg);

	/**
	 * Writes a message at level {@link Logger#WARN WARN}.
	 * 
	 * @param tag the tag
	 * @param msg the message
	 * @return 0 if the message was written successfully, an error code
	 * otherwise
	 */
	public int w(String tag, String msg);

	/**
	 * Writes a message at level {@link Logger#WARN WARN}.
	 * 
	 * @param tag the tag
	 * @param tr an exception or null
	 * @return 0 if no error occurred, an error code otherwise
	 */
	public int w(String tag, Throwable tr);

	/**
	 * Writes a message at level {@link Logger#WARN WARN}.
	 * 
	 * @param tag the tag
	 * @param msg the message
	 * @param tr an exception or null
	 * @return 0 if no error occurred, an error code otherwise
	 */
	public int w(String tag, String msg, Throwable tr);

	/**
	 * Writes a message at level {@link Logger#ASSERT ASSERT}.
	 * 
	 * @param tag the tag
	 * @param msg the message
	 * @return 0 if no error occurred, an error code otherwise
	 */
	public int wtf(String tag, String msg);

	/**
	 * Writes a message at level {@link Logger#ASSERT ASSERT}.
	 * 
	 * @param tag the tag
	 * @param tr an exception or null
	 * @return 0 if no error occurred, an error code otherwise
	 */
	public int wtf(String tag, Throwable tr);

	/**
	 * Writes a message at level {@link Logger#ASSERT ASSERT}.
	 * 
	 * @param tag the tag
	 * @param msg the message
	 * @param tr an exception or null
	 * @return 0 if no error occurred, an error code otherwise
	 */
	public int wtf(String tag, String msg, Throwable tr);
}
