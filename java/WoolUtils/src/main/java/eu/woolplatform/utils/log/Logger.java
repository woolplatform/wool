package eu.woolplatform.utils.log;

/**
 * <p>This class can be used for writing log output. It has a number of static
 * methods to send a log message at a specific level. The supported levels
 * in order from most urgent to least urgent are:</p>
 * 
 * <ul>
 * <li>{@link #ASSERT ASSERT}</li>
 * <li>{@link #ERROR ERROR}</li>
 * <li>{@link #WARN WARN}</li>
 * <li>{@link #INFO INFO}</li>
 * <li>{@link #DEBUG DEBUG}</li>
 * <li>{@link #VERBOSE VERBOSE}</li>
 * </ul>
 * 
 * <p>The actual behaviour of the logger is determined by the {@link
 * LogDelegate LogDelegate} which is set with {@link
 * #setLogDelegate(LogDelegate) setLogDelegate()}. The default is a {@link
 * DefaultLogDelegate DefaultLogDelegate}, which writes messages to standard
 * output or standard error. Normally the log delegate should be set and
 * configured once at the start of the application.</p>
 * 
 * <p>Each log method takes a <code>tag</code> parameter. This should be a tag
 * that identifies the source of the log. A log delegate may apply different
 * log levels for different tags. For example one tag may be logged up to the
 * level {@link #DEBUG DEBUG}, while another tag is only logged up to the level
 * {@link #WARN WARN}.</p>
 * 
 * <p>Note that it may require computation time and resources to construct
 * a log message, and this is not necessary if eventually the message is not
 * written because of the log level. To avoid the construction costs, you may
 * check {@link #isLoggable(String, int) isLoggable()} first.</p>
 * 
 * @author Dennis Hofs
 */
public class Logger {
	private static LogDelegate delegate = new DefaultLogDelegate();
	
	public static final int VERBOSE = 2;
	public static final int DEBUG = 3;
	public static final int INFO = 4;
	public static final int WARN = 5;
	public static final int ERROR = 6;
	public static final int ASSERT = 7;
	
	/**
	 * Sets the log delegate. See the top of this page for more information.
	 * 
	 * @param delegate the log delegate
	 */
	public static void setLogDelegate(LogDelegate delegate) {
		Logger.delegate = delegate;
	}
	
	/**
	 * Returns the log delegate.
	 * 
	 * @return the log delegate
	 */
	public static LogDelegate getLogDelegate() {
		return delegate;
	}
	
	/**
	 * Writes a message at level {@link #DEBUG DEBUG}.
	 * 
	 * @param tag the tag
	 * @param msg the message
	 * @param tr an exception
	 * @return 0 if no error occurred, an error code otherwise
	 */
	public static int d(String tag, String msg, Throwable tr) {
		return delegate.d(tag, msg, tr);
	}
	
	/**
	 * Writes a message at level {@link #DEBUG DEBUG}.
	 * 
	 * @param tag the tag
	 * @param msg the message
	 * @return 0 if no error occurred, an error code otherwise
	 */
	public static int d(String tag, String msg) {
		return delegate.d(tag, msg);
	}
	
	/**
	 * Writes a message at level {@link #ERROR ERROR}.
	 * 
	 * @param tag the tag
	 * @param msg the message
	 * @return 0 if no error occurred, an error code otherwise
	 */
	public static int e(String tag, String msg) {
		return delegate.e(tag, msg);
	}

	/**
	 * Writes a message at level {@link #ERROR ERROR}.
	 * 
	 * @param tag the tag
	 * @param msg the message
	 * @param tr an exception
	 * @return 0 if no error occurred, an error code otherwise
	 */
	public static int e(String tag, String msg, Throwable tr) {
		return delegate.e(tag, msg, tr);
	}

	/**
	 * Returns the stack trace string for the specified exception. The returned
	 * string probably contains new lines, but it does not have a trailing new
	 * line.
	 * 
	 * @param tr the exception
	 * @return the stack trace string
	 */
	public static String getStackTraceString(Throwable tr) {
		return delegate.getStackTraceString(tr);
	}

	/**
	 * Writes a message at level {@link #INFO INFO}.
	 * 
	 * @param tag the tag
	 * @param msg the message
	 * @param tr an exception
	 * @return 0 if no error occurred, an error code otherwise
	 */
	public static int i(String tag, String msg, Throwable tr) {
		return delegate.i(tag, msg, tr);
	}

	/**
	 * Writes a message at level {@link #INFO INFO}.
	 * 
	 * @param tag the tag
	 * @param msg the message
	 * @return 0 if no error occurred, an error code otherwise
	 */
	public static int i(String tag, String msg) {
		return delegate.i(tag, msg);
	}

	/**
	 * Determines if messages at the specified level are logged for the
	 * specified tag.
	 * 
	 * @param tag the tag
	 * @param level the level
	 * @return true if the message will be logged, false otherwise
	 */
	public static boolean isLoggable(String tag, int level) {
		return delegate.isLoggable(tag, level);
	}

	/**
	 * Writes a log message.
	 * 
	 * @param priority the log level
	 * @param tag the tag
	 * @param msg the message
	 * @return 0 if no error occurred, an error code otherwise
	 */
	public static int println(int priority, String tag, String msg) {
		return delegate.println(priority, tag, msg);
	}

	/**
	 * Writes a message at level {@link #VERBOSE VERBOSE}.
	 * 
	 * @param tag the tag
	 * @param msg the message
	 * @param tr an exception or null
	 * @return 0 if no error occurred, an error code otherwise
	 */
	public static int v(String tag, String msg, Throwable tr) {
		return delegate.v(tag, msg, tr);
	}

	/**
	 * Writes a message at level {@link #VERBOSE VERBOSE}.
	 * 
	 * @param tag the tag
	 * @param msg the message
	 * @return 0 if no error occurred, an error code otherwise
	 */
	public static int v(String tag, String msg) {
		return delegate.v(tag, msg);
	}

	/**
	 * Writes a message at level {@link #WARN WARN}.
	 * 
	 * @param tag the tag
	 * @param msg the message
	 * @return 0 if the message was written successfully, an error code
	 * otherwise
	 */
	public static int w(String tag, String msg) {
		return delegate.w(tag, msg);
	}

	/**
	 * Writes a message at level {@link #WARN WARN}.
	 * 
	 * @param tag the tag
	 * @param tr an exception or null
	 * @return 0 if no error occurred, an error code otherwise
	 */
	public static int w(String tag, Throwable tr) {
		return delegate.w(tag, tr);
	}

	/**
	 * Writes a message at level {@link #WARN WARN}.
	 * 
	 * @param tag the tag
	 * @param msg the message
	 * @param tr an exception or null
	 * @return 0 if no error occurred, an error code otherwise
	 */
	public static int w(String tag, String msg, Throwable tr) {
		return delegate.w(tag, msg, tr);
	}

	/**
	 * Writes a message at level {@link #ASSERT ASSERT}.
	 * 
	 * @param tag the tag
	 * @param msg the message
	 * @return 0 if no error occurred, an error code otherwise
	 */
	public static int wtf(String tag, String msg) {
		return delegate.wtf(tag, msg);
	}

	/**
	 * Writes a message at level {@link #ASSERT ASSERT}.
	 * 
	 * @param tag the tag
	 * @param tr an exception or null
	 * @return 0 if no error occurred, an error code otherwise
	 */
	public static int wtf(String tag, Throwable tr) {
		return delegate.wtf(tag, tr);
	}

	/**
	 * Writes a message at level {@link #ASSERT ASSERT}.
	 * 
	 * @param tag the tag
	 * @param msg the message
	 * @param tr an exception or null
	 * @return 0 if no error occurred, an error code otherwise
	 */
	public static int wtf(String tag, String msg, Throwable tr) {
		return delegate.wtf(tag, msg, tr);
	}
}
