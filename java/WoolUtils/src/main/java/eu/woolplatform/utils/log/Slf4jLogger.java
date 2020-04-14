package eu.woolplatform.utils.log;

import org.slf4j.Marker;

/**
 * This SLF4J logger forwards calls to the RRD {@link Logger Logger}. It
 * ignores markers. It uses the following mapping between SLF4J levels and
 * RRD Logger levels.
 * 
 * <style type="text/css">
 *   table.bordered {
 *     border-collapse: collapse;
 *   }
 *   table.bordered th, table.bordered td {
 *     text-align: left;
 *     border: 1px solid black;
 *     padding: 4px 8px;
 *     vertical-align: top;
 *   }
 * </style>
 * <p><table style="bordered">
 * <caption>Log levels</caption>
 * <tbody><tr>
 * <th>SLF4J level</th>
 * <th>RRD Logger level</th>
 * </tr><tr>
 * <td>trace</td>
 * <td>{@link Logger#VERBOSE VERBOSE}</td>
 * </tr><tr>
 * <td>debug</td>
 * <td>{@link Logger#DEBUG DEBUG}</td>
 * </tr><tr>
 * <td>info</td>
 * <td>{@link Logger#INFO INFO}</td>
 * </tr><tr>
 * <td>warn</td>
 * <td>{@link Logger#WARN WARN}</td>
 * </tr><tr>
 * <td>error</td>
 * <td>{@link Logger#ERROR ERROR}</td>
 * </tr></tbody>
 * </table></p>
 * 
 * @author Dennis Hofs (RRD)
 */
public class Slf4jLogger implements org.slf4j.Logger {
	private String name;
	
	/**
	 * Constructs a new logger. The name will be used as the tag string in the
	 * RRD Logger.
	 * 
	 * @param name the name
	 */
	public Slf4jLogger(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean isTraceEnabled() {
		return Logger.isLoggable(name, Logger.VERBOSE);
	}

	@Override
	public void trace(String msg) {
		Logger.v(name, msg);
	}

	@Override
	public void trace(String format, Object arg) {
		if (isTraceEnabled())
			Logger.v(name, formatString(format, arg));
	}

	@Override
	public void trace(String format, Object arg1, Object arg2) {
		if (isTraceEnabled())
			Logger.v(name, formatString(format, arg1, arg2));
	}

	@Override
	public void trace(String format, Object... arguments) {
		if (isTraceEnabled())
			Logger.v(name, formatString(format, arguments));
	}

	@Override
	public void trace(String msg, Throwable t) {
		Logger.v(name, msg, t);
	}

	@Override
	public boolean isTraceEnabled(Marker marker) {
		return isTraceEnabled();
	}

	@Override
	public void trace(Marker marker, String msg) {
		trace(msg);
	}

	@Override
	public void trace(Marker marker, String format, Object arg) {
		trace(format, arg);
	}

	@Override
	public void trace(Marker marker, String format, Object arg1, Object arg2) {
		trace(format, arg1, arg2);
	}

	@Override
	public void trace(Marker marker, String format, Object... argArray) {
		trace(format, argArray);
	}

	@Override
	public void trace(Marker marker, String msg, Throwable t) {
		trace(msg, t);
	}

	@Override
	public boolean isDebugEnabled() {
		return Logger.isLoggable(name, Logger.DEBUG);
	}

	@Override
	public void debug(String msg) {
		Logger.d(name, msg);
	}

	@Override
	public void debug(String format, Object arg) {
		Logger.d(name, formatString(format, arg));
	}

	@Override
	public void debug(String format, Object arg1, Object arg2) {
		Logger.d(name, formatString(format, arg1, arg2));
	}

	@Override
	public void debug(String format, Object... arguments) {
		Logger.d(name, formatString(format, arguments));
	}

	@Override
	public void debug(String msg, Throwable t) {
		Logger.d(name, msg, t);
	}

	@Override
	public boolean isDebugEnabled(Marker marker) {
		return isDebugEnabled();
	}

	@Override
	public void debug(Marker marker, String msg) {
		debug(msg);
	}

	@Override
	public void debug(Marker marker, String format, Object arg) {
		debug(format, arg);
	}

	@Override
	public void debug(Marker marker, String format, Object arg1, Object arg2) {
		debug(format, arg1, arg2);
	}

	@Override
	public void debug(Marker marker, String format, Object... arguments) {
		debug(format, arguments);
	}

	@Override
	public void debug(Marker marker, String msg, Throwable t) {
		debug(msg, t);
	}

	@Override
	public boolean isInfoEnabled() {
		return Logger.isLoggable(name, Logger.INFO);
	}

	@Override
	public void info(String msg) {
		Logger.i(name, msg);
	}

	@Override
	public void info(String format, Object arg) {
		Logger.i(name, formatString(format, arg));
	}

	@Override
	public void info(String format, Object arg1, Object arg2) {
		Logger.i(name, formatString(format, arg1, arg2));
	}

	@Override
	public void info(String format, Object... arguments) {
		Logger.i(name, formatString(format, arguments));
	}

	@Override
	public void info(String msg, Throwable t) {
		Logger.i(name, msg, t);
	}

	@Override
	public boolean isInfoEnabled(Marker marker) {
		return isInfoEnabled();
	}

	@Override
	public void info(Marker marker, String msg) {
		info(msg);
	}

	@Override
	public void info(Marker marker, String format, Object arg) {
		info(format, arg);
	}

	@Override
	public void info(Marker marker, String format, Object arg1, Object arg2) {
		info(format, arg1, arg2);
	}

	@Override
	public void info(Marker marker, String format, Object... arguments) {
		info(format, arguments);
	}

	@Override
	public void info(Marker marker, String msg, Throwable t) {
		info(msg, t);
	}

	@Override
	public boolean isWarnEnabled() {
		return Logger.isLoggable(name, Logger.WARN);
	}

	@Override
	public void warn(String msg) {
		Logger.w(name, msg);
	}

	@Override
	public void warn(String format, Object arg) {
		Logger.w(name, formatString(format, arg));
	}

	@Override
	public void warn(String format, Object arg1, Object arg2) {
		Logger.w(name, formatString(format, arg1, arg2));
	}

	@Override
	public void warn(String format, Object... arguments) {
		Logger.w(name, formatString(format, arguments));
	}

	@Override
	public void warn(String msg, Throwable t) {
		Logger.w(name, msg, t);
	}

	@Override
	public boolean isWarnEnabled(Marker marker) {
		return isWarnEnabled();
	}

	@Override
	public void warn(Marker marker, String msg) {
		warn(msg);
	}

	@Override
	public void warn(Marker marker, String format, Object arg) {
		warn(format, arg);
	}

	@Override
	public void warn(Marker marker, String format, Object arg1, Object arg2) {
		warn(format, arg1, arg2);
	}

	@Override
	public void warn(Marker marker, String format, Object... arguments) {
		warn(format, arguments);
	}

	@Override
	public void warn(Marker marker, String msg, Throwable t) {
		warn(msg, t);
	}

	@Override
	public boolean isErrorEnabled() {
		return Logger.isLoggable(name, Logger.ERROR);
	}

	@Override
	public void error(String msg) {
		Logger.e(name, msg);
	}

	@Override
	public void error(String format, Object arg) {
		Logger.e(name, formatString(format, arg));
	}

	@Override
	public void error(String format, Object arg1, Object arg2) {
		Logger.e(name, formatString(format, arg1, arg2));
	}

	@Override
	public void error(String format, Object... arguments) {
		Logger.e(name, formatString(format, arguments));
	}

	@Override
	public void error(String msg, Throwable t) {
		Logger.e(name, msg, t);
	}

	@Override
	public boolean isErrorEnabled(Marker marker) {
		return isErrorEnabled();
	}

	@Override
	public void error(Marker marker, String msg) {
		error(msg);
	}

	@Override
	public void error(Marker marker, String format, Object arg) {
		error(format, arg);
	}

	@Override
	public void error(Marker marker, String format, Object arg1, Object arg2) {
		error(format, arg1, arg2);
	}

	@Override
	public void error(Marker marker, String format, Object... arguments) {
		error(format, arguments);
	}

	@Override
	public void error(Marker marker, String msg, Throwable t) {
		error(msg, t);
	}

	/**
	 * Replaces the first occurrence of {} (if any) with arg.
	 * 
	 * @param format the format string
	 * @param arg the argument or null
	 * @return the formatted string
	 */
	private String formatString(String format, Object arg) {
		int index = format.indexOf("{}");
		if (index == -1)
			return format;
		return format.substring(0, index) +
				(arg == null ? "null" : arg.toString()) +
				format.substring(index + 2);
	}
	
	/**
	 * Replaces the first and second occurrence of {} (if any) with arg1 and
	 * arg2.
	 * 
	 * @param format the format string
	 * @param arg1 the first argument or null
	 * @param arg2 the second argument or null
	 * @return the formatted string
	 */
	private String formatString(String format, Object arg1, Object arg2) {
		StringBuilder builder = new StringBuilder();
		int index = format.indexOf("{}");
		if (index == -1)
			return format;
		builder.append(format.substring(0, index));
		builder.append(arg1 == null ? "null" : arg1.toString());
		int start = index + 2;
		index = format.indexOf("{}", start);
		if (index == -1) {
			builder.append(format.substring(start));
			return builder.toString();
		}
		builder.append(format.substring(start, index));
		builder.append(arg2 == null ? "null" : arg2.toString());
		builder.append(format.substring(index + 2));
		return builder.toString();
	}
	
	/**
	 * Replaces occurrences of {} with the specified arguments.
	 * 
	 * @param format the format string
	 * @param arguments the arguments (each argument may be null)
	 * @return the formatted string
	 */
	private String formatString(String format, Object... arguments) {
		StringBuilder builder = new StringBuilder();
		int start = 0;
		for (Object arg : arguments) {
			int index = format.indexOf("{}", start);
			if (index == -1)
				break;
			builder.append(format.substring(start, index));
			builder.append(arg == null ? "null" : arg.toString());
			start = index + 2;
		}
		builder.append(format.substring(start));
		return builder.toString();
	}
}
