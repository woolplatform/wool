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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.woolplatform.utils.exception.ParseException;
import eu.woolplatform.utils.schedule.Job;
import eu.woolplatform.utils.schedule.SerialJobRunner;
import eu.woolplatform.utils.xml.SimpleSAXHandler;
import eu.woolplatform.utils.xml.SimpleSAXParser;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;

/**
 * <p>This abstract log delegate provides the following features:</p>
 * 
 * <ul>
 * <li>Every line that is printed in a log message will get three tags: the log
 * level, the message tag (identifies the source of the log message) and the
 * current date and time.</li>
 * <li>Capture standard output and standard error. Everything that is printed
 * to standard output and standard error can get the same treatment as other
 * log messages. Lines on standard output will be logged at level {@link
 * Logger#INFO INFO} with tag "STDOUT". Lines on standard error will be logged
 * at level {@link Logger#ERROR ERROR} with tag "STDERR". This feature is by
 * default disabled, but it can be enabled with {@link
 * #setCaptureStdOut(boolean) setCaptureStdOut()} and {@link
 * #setCaptureStdErr(boolean) setCaptureStdErr()}.</li>
 * <li>Log files. All log messages can be written to log files using a {@link
 * FileLogger FileLogger}. This feature can be enabled with {@link
 * #setFileLogger(FileLogger) setFileLogger()}.</li>
 * </ul>
 * 
 * <p>The first time an instance of this class is constructed, it will store
 * the current values of {@link System#out System.out} and {@link System#err
 * System.err}. You should ensure that they have the default system values.
 * The values are changed when you call {@link #setCaptureStdOut(boolean)
 * setCaptureStdOut(true)} or {@link #setCaptureStdErr(boolean)
 * setCaptureStdErr(true)}. The default system values are restored when you
 * call {@link #setCaptureStdOut(boolean) setCaptureStdOut(false)} and {@link
 * #setCaptureStdErr(boolean) setCaptureStdErr(false)}.</p>
 * 
 * <p>Subclasses can get access to the default system values with the methods
 * {@link #getDefaultStdOut() getDefaultStdOut()} and {@link
 * #getDefaultStdErr() getDefaultStdErr()}. If a log message needs to be
 * printed to standard output or standard error, these default system values
 * MUST be used. Otherwise the log messages will be recursively captured.</p>
 * 
 * <p>A subclass need only implement {@link
 * #printTaggedMessage(int, String, String) printTaggedMessage()}.</p>
 * 
 * <p>Every log method may return an error code. This is in fact an OR of error
 * constants defined in this class or a subclass. This class defines the
 * following constants:</p>
 * 
 * <ul>
 * <li>{@link #ERROR_WRITE_FILE ERROR_WRITE_FILE}</li>
 * </ul>
 * 
 * @author Dennis Hofs
 */
public abstract class AbstractLogDelegate implements LogDelegate {
	/**
	 * This error code indicates that an error occurred while writing a log
	 * message to a log file.
	 */
	public static int ERROR_WRITE_FILE = 1;
	
	/**
	 * Subclasses can define error codes starting at this constant. An error
	 * code should have exactly one bit set, in order that an OR of error
	 * codes is possible.
	 */
	protected static int ERROR_BASE = 1 << 16;
	
	private int defaultLevel = Logger.INFO;
	private Map<String, Integer> levelMap = new HashMap<>();
	private static PrintStream oldStdOut = null;
	private static PrintStream oldStdErr = null;
	private FileLogger fileLogger = null;
	private SerialJobRunner fileLogRunner = new SerialJobRunner();
	private final Object lock = new Object();
	
	/**
	 * Constructs a new abstract log delegate. The first time an abstract log
	 * delegate is constructed, you should ensure that {@link System#out
	 * System.out} and {@link System#err System.err} have the default system
	 * values.
	 */
	public AbstractLogDelegate() {
		if (oldStdOut == null)
			oldStdOut = System.out;
		if (oldStdErr == null)
			oldStdErr = System.err;
	}
	
	/**
	 * Sets the log levels as defined in an XML file.
	 * 
	 * @param xmlFile the XML file
	 * @throws ParseException if the content of the XML file is invalid
	 * @throws IOException if a reading error occurs
	 */
	public void setLogLevels(File xmlFile) throws ParseException, IOException {
		try (FileInputStream in = new FileInputStream(xmlFile)) {
			setLogLevels(in);
		}
	}
	
	/**
	 * Sets the log levels as defined in an XML file.
	 * 
	 * @param in the input stream for the XML file
	 * @throws ParseException if the content of the XML file is invalid
	 * @throws IOException if a reading error occurs
	 */
	public void setLogLevels(InputStream in) throws ParseException,
	IOException {
		SimpleSAXParser<LogLevelMap> parser = new SimpleSAXParser<>(
				new XMLHandler());
		LogLevelMap map = parser.parse(new InputSource(in));
		defaultLevel = map.defaultLevel;
		levelMap = map.levelMap;
	}
	
	/**
	 * Sets the default log level. This level is used for message tags for
	 * which no specific log level was set (e.g. with {@link
	 * #setLogLevel(String, int) setLogLevel(tag, level)}). The logger will
	 * not write log messages at a lower level than the specified level.
	 * 
	 * @param level the log level
	 */
	public void setLogLevel(int level) {
		defaultLevel = level;
	}
	
	/**
	 * Sets the log level for messages with the specified tag. The logger will
	 * not write log messages at a lower level than the specified level.
	 * 
	 * @param tag the message tag
	 * @param level the log level
	 */
	public void setLogLevel(String tag, int level) {
		levelMap.put(tag, level);
	}

	/**
	 * Sets whether standard output should be captured.
	 * 
	 * @param capture true if standard output should be captured, false
	 * otherwise
	 */
	public void setCaptureStdOut(boolean capture) {
		if (capture)
			System.setOut(new PrintStream(new LogStream(true)));
		else
			System.setOut(oldStdOut);
	}
	
	/**
	 * Sets whether standard error should be captured.
	 * 
	 * @param capture true if standard error should be captured, false
	 * otherwise
	 */
	public void setCaptureStdErr(boolean capture) {
		if (capture)
			System.setErr(new PrintStream(new LogStream(false)));
		else
			System.setErr(oldStdErr);
	}
	
	/**
	 * Sets a file logger to which log messages should be written. You may set
	 * this to null to disable logging to files (that is the default).
	 * 
	 * @param fileLogger the file logger or null
	 */
	public void setFileLogger(FileLogger fileLogger) {
		this.fileLogger = fileLogger;
	}
	
	/**
	 * Returns the file logger. If not file logger was set, this method returns
	 * null.
	 * 
	 * @return the file logger or null
	 */
	public FileLogger getFileLogger() {
		return fileLogger;
	}
	
	/**
	 * Returns the default system value of {@link System#out System.out}.
	 * Subclasses must use this value if they want to print to standard output,
	 * because the current value of {@link System#out System.out} may be a
	 * stream that is captured by the logger.
	 * 
	 * @return the default standard output
	 */
	protected PrintStream getDefaultStdOut() {
		return oldStdOut;
	}
	
	/**
	 * Returns the default system value of {@link System#err System.err}.
	 * Subclasses must use this value if they want to print to standard error,
	 * because the current value of {@link System#err System.err} may be a
	 * stream that is captured by the logger.
	 * 
	 * @return the default standard error
	 */
	protected PrintStream getDefaultStdErr() {
		return oldStdErr;
	}
	
	/**
	 * Writes a log message.
	 * 
	 * @param priority the log level
	 * @param tag the tag
	 * @param msg the message
	 * @param tr an exception
	 * @return 0 if no error occurred, an error code otherwise
	 */
	private int println(int priority, String tag, String msg, Throwable tr) {
		if (!isLoggable(tag, priority))
			return 0;
		String newline = System.getProperty("line.separator");
		String errMsg = msg;
		if (tr != null)
			errMsg += newline + getStackTraceString(tr);
		return println(priority, tag, errMsg);
	}
	
	/**
	 * Writes a log message.
	 * 
	 * @param priority the log level
	 * @param tag the tag
	 * @param tr an exception
	 * @return 0 if no error occurred, an error code otherwise
	 */
	private int println(int priority, String tag, Throwable tr) {
		if (!isLoggable(tag, priority))
			return 0;
		String errMsg = tr != null ? getStackTraceString(tr) : "null";
		return println(priority, tag, errMsg);
	}

	@Override
	public int println(int priority, String tag, String msg) {
		if (!isLoggable(tag, priority))
			return 0;
		ZonedDateTime time = DateTimeUtils.nowMs();
		String taggedMsg = LogLineTagger.tagLines(priority, tag, time, msg);
		synchronized (lock) {
			int result = printTaggedMessage(priority, tag, taggedMsg);
			if (fileLogger != null) {
				fileLogRunner.postJob(new FileLogJob(priority, tag,
						time.toLocalDate(), taggedMsg), null);
			}
			return result;
		}
	}

	private class FileLogJob implements Job {
		private int priority;
		private String tag;
		private LocalDate date;
		private String taggedMsg;

		public FileLogJob(int priority, String tag, LocalDate date,
				String taggedMsg) {
			this.priority = priority;
			this.tag = tag;
			this.date = date;
			this.taggedMsg = taggedMsg;
		}

		@Override
		public void run() {
			fileLogger.printTaggedMessage(priority, tag, date, taggedMsg);
		}

		@Override
		public void cancel() {
		}
	}

	/**
	 * Writes a log message. Every line in the message text has already been
	 * tagged with the log level, message tag (identifier of the source of the
	 * log message) and the current date and time. Every line ends with a new
	 * line character, including the last line.
	 * 
	 * <p>This method is only called if {@link #isLoggable(String, int)
	 * isLoggable()} returns true. It's called inside a lock, so it's thread
	 * safe.</p>
	 * 
	 * <p>For returned error codes, a subclass may define its own error codes
	 * based on {@link #ERROR_BASE ERROR_BASE}.</p>
	 * 
	 * @param priority the log level
	 * @param tag the message tag
	 * @param msg the tagged message
	 * @return 0 if no error occurred, an error code otherwise.
	 */
	public abstract int printTaggedMessage(int priority, String tag,
			String msg);

	@Override
	public String getStackTraceString(Throwable tr) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		tr.printStackTrace(pw);
		return sw.toString();
	}

	@Override
	public boolean isLoggable(String tag, int level) {
		if (level < Logger.VERBOSE || level > Logger.ASSERT)
			return false;
		Integer tagLevel = levelMap.get(tag);
		if (tagLevel == null)
			return level >= defaultLevel;
		else
			return level >= tagLevel;
	}

	@Override
	public int d(String tag, String msg, Throwable tr) {
		return println(Logger.DEBUG, tag, msg, tr);
	}

	@Override
	public int d(String tag, String msg) {
		return println(Logger.DEBUG, tag, msg);
	}

	@Override
	public int e(String tag, String msg) {
		return println(Logger.ERROR, tag, msg);
	}

	@Override
	public int e(String tag, String msg, Throwable tr) {
		return println(Logger.ERROR, tag, msg, tr);
	}

	@Override
	public int i(String tag, String msg, Throwable tr) {
		return println(Logger.INFO, tag, msg, tr);
	}

	@Override
	public int i(String tag, String msg) {
		return println(Logger.INFO, tag, msg);
	}

	@Override
	public int v(String tag, String msg, Throwable tr) {
		return println(Logger.VERBOSE, tag, msg, tr);
	}

	@Override
	public int v(String tag, String msg) {
		return println(Logger.VERBOSE, tag, msg);
	}

	@Override
	public int w(String tag, String msg) {
		return println(Logger.WARN, tag, msg);
	}

	@Override
	public int w(String tag, Throwable tr) {
		return println(Logger.WARN, tag, tr);
	}

	@Override
	public int w(String tag, String msg, Throwable tr) {
		return println(Logger.WARN, tag, msg, tr);
	}

	@Override
	public int wtf(String tag, String msg) {
		return println(Logger.ASSERT, tag, msg);
	}

	@Override
	public int wtf(String tag, Throwable tr) {
		return println(Logger.ASSERT, tag, tr);
	}

	@Override
	public int wtf(String tag, String msg, Throwable tr) {
		return println(Logger.ASSERT, tag, msg, tr);
	}

	/**
	 * This XML handler can parse an XML file log levels.
	 */
	private static class XMLHandler implements SimpleSAXHandler<LogLevelMap> {
		private LogLevelMap map = new LogLevelMap();
		
		@Override
		public void startElement(String name, Attributes atts,
				List<String> parents) throws ParseException {
			if (name.equals("loglevels")) {
				startLoglevels(atts);
			} else if (name.equals("tag")) {
				startTag(atts);
			}
		}
		
		private void startLoglevels(Attributes atts) throws ParseException {
			String s = atts.getValue("level");
			if (s != null)
				map.defaultLevel = parseLevel(s);
		}
		
		private void startTag(Attributes atts) throws ParseException {
			String name = atts.getValue("name");
			if (name == null)
				throw new ParseException("Attribute \"name\" not found");
			if (map.levelMap.containsKey(name))
				throw new ParseException("Duplicate tag name: " + name);
			String s = atts.getValue("level");
			if (s == null)
				throw new ParseException("Attribute \"level\" not found");
			int level = parseLevel(s);
			map.levelMap.put(name, level);
		}
		
		private int parseLevel(String s) throws ParseException {
			switch (s) {
				case "ASSERT":
					return Logger.ASSERT;
				case "ERROR":
					return Logger.ERROR;
				case "WARN":
					return Logger.WARN;
				case "INFO":
					return Logger.INFO;
				case "DEBUG":
					return Logger.DEBUG;
				case "VERBOSE":
					return Logger.VERBOSE;
				default:
					throw new ParseException("Invalid log level: " + s);
			}
		}

		@Override
		public void endElement(String name, List<String> parents)
				throws ParseException {
		}

		@Override
		public void characters(String ch, List<String> parents)
				throws ParseException {
		}

		@Override
		public LogLevelMap getObject() {
			return map;
		}
	}
	
	/**
	 * This class is returned by the XML handler that parses an XML file with
	 * log levels.
	 */
	private static class LogLevelMap {
		/**
		 * The log level for messages with a tag for which no specific log
		 * level has been defined.
		 */
		public int defaultLevel = Logger.INFO;
		
		/**
		 * A map from message tags to log levels.
		 */
		public Map<String, Integer> levelMap = new HashMap<>();
	}

	/**
	 * A log stream is used to capture standard output or standard error.
	 */
	private class LogStream extends OutputStream {
		private Charset charset;
		private boolean isStdOut;
		private ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		private boolean lastIsCR = false;
		
		/**
		 * Constructs a new log stream.
		 * 
		 * @param isStdOut true if the source is standard output, false if the
		 * source is standard error
		 */
		public LogStream(boolean isStdOut) {
			charset = Charset.defaultCharset();
			this.isStdOut = isStdOut;
		}

		@Override
		public void write(int b) throws IOException {
			write(new byte[] { (byte)(b & 0xFF) });
		}
		
		@Override
		public void write(byte[] bs) throws IOException {
			write(bs, 0, bs.length);
		}

		@Override
		public void write(byte[] bs, int off, int len) throws IOException {
			int start = off;
			for (int i = off; i < off + len; i++) {
				byte b = bs[i];
				if (b == '\r') {
					byteOut.write(bs, start, i - start);
					writeBufferedLine();
					start = i + 1;
					lastIsCR = true;
				} else if (b == '\n') {
					if (!lastIsCR) {
						byteOut.write(bs, start, i - start);
						writeBufferedLine();
					}
					start = i + 1;
					lastIsCR = false;
				} else {
					lastIsCR = false;
				}
			}
			if (start < off + len) {
				byteOut.write(bs, start, off + len - start);
			}
		}
		
		/**
		 * Takes the content of "byteOut" and writes it as a line to the log
		 * output.
		 */
		private void writeBufferedLine() {
			ByteBuffer bb = ByteBuffer.wrap(byteOut.toByteArray());
			byteOut.reset();
			CharBuffer cs = charset.decode(bb);
			if (isStdOut)
				i("STDOUT", cs.toString());
			else
				e("STDERR", cs.toString());
		}
	}
}
