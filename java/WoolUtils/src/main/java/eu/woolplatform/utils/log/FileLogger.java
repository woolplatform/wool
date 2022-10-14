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

import eu.woolplatform.utils.io.FileUtils;
import eu.woolplatform.utils.io.ZipUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * This class can write log messages to log files. It is constructed with a log
 * directory. In this directory, the file logger will automatically create log
 * files. Each log file will get a name with the current date.
 * 
 * @author Dennis Hofs
 */
public class FileLogger {
	public static final Object ARCHIVE_LOCK = new Object();

	private File logDir;
	private final Object lock = new Object();
	private int archiveDelay = 0;
	private int purgeDelay = 0;
	private LocalDate archivedUntil = null;
	private LocalDate purgedUntil = null;
	
	/**
	 * Constructs a new file logger. The specified log directory will be
	 * created if it doesn't already exist.
	 * 
	 * @param logDir the log directory
	 * @throws IOException if the log directory can't be created
	 */
	public FileLogger(File logDir) throws IOException {
		this.logDir = logDir;
		FileUtils.mkdir(logDir);
		String[] zipFiles = logDir.list((dir, filename) ->
				filename.matches("[0-9]{8}\\.zip"));
		if (zipFiles.length > 0) {
			List<String> zipFileList = new ArrayList<>(Arrays.asList(
					zipFiles));
			Collections.sort(zipFileList);
			String lastFile = zipFileList.get(zipFileList.size() - 1);
			DateTimeFormatter parser = DateTimeFormatter.ofPattern("yyyyMMdd");
			archivedUntil = parser.parse(lastFile.substring(0, 8),
					LocalDate::from);
		}
	}
	
	/**
	 * Sets the number of days after which log files should be archived to a
	 * zip file. If you set "days" to 1, then it will archive yesterday's log
	 * file and older log files. If you set "days" to 0, then it won't archive
	 * any log files. The default is 0.
	 *
	 * @param days the number of days after which log files should be archived
	 */
	public void setArchiveDelay(int days) {
		this.archiveDelay = days;
	}

	/**
	 * Sets the number of days after which log files should be purged. If you
	 * set "days" to 1, then it will purge yesterday's log file and older log
	 * files. If you set "days" to 0, then it won't purge any log files. The
	 * default is 0.
	 *
	 * @param days the number of days after which log files should be purged
	 */
	public void setPurgeDelay(int days) {
		this.purgeDelay = days;
	}
	
	/**
	 * Writes a log message. The lines in the message should already be tagged
	 * with the log level, message tag (identifies the source of the log
	 * message) and the current date and time. Every line should end with a new
	 * line character, including the last line. This method is thread safe.
	 *
	 * <p>If the specified date has already been archived or purged, then this
	 * method has no effect.</p>
	 * 
	 * @param priority the log level
	 * @param tag the message tag
	 * @param date the date of the log message
	 * @param msg the tagged message
	 * @return 0 if no error occurred, an error code otherwise
	 */
	public int printTaggedMessage(int priority, String tag, LocalDate date,
			String msg) {
		synchronized (lock) {
			try {
				Writer out = openLogFile(date);
				if (out == null)
					return 0;
				try (out) {
					out.write(msg);
				}
				return 0;
			} catch (IOException ex) {
				return AbstractLogDelegate.ERROR_WRITE_FILE;
			}
		}
	}
	
	/**
	 * Opens a log file for the specified date. If the date has already been
	 * archived or purged, this method returns null.
	 * 
	 * @param date the date
	 * @return the writer or null
	 * @throws IOException if the file can't be opened
	 */
	private Writer openLogFile(LocalDate date) throws IOException {
		purgeFiles(date);
		archiveFiles(date);
		if ((archivedUntil != null && !archivedUntil.isBefore(date)) ||
				(purgedUntil != null && !purgedUntil.isBefore(date))) {
			return null;
		}
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
		String filename = formatter.format(date) + ".log";
		File file = new File(logDir, filename);
		return new OutputStreamWriter(new FileOutputStream(file, true),
				StandardCharsets.UTF_8);
	}

	/**
	 * Purges all log files until and including the specified date.
	 *
	 * @param date the date
	 */
	private void purgeFiles(LocalDate date) {
		if (purgeDelay <= 0)
			return;
		LocalDate minDate = null;
		if (purgedUntil != null)
			minDate = purgedUntil.plusDays(1);
		final LocalDate maxDate = date.minusDays(purgeDelay);
		if (minDate != null && minDate.isAfter(maxDate))
			return;
		purgedUntil = maxDate;
		synchronized (ARCHIVE_LOCK) {
			File[] files = logDir.listFiles(new PurgeFilter(maxDate));
			for (File file : files) {
				if (!file.delete()) {
					System.err.println("Can't purge log file: " +
							file.getAbsolutePath());
				}
			}
		}
	}

	/**
	 * Archives log files that should be archived at the specified date. It
	 * will determine the dates that should be archived and then create a ZIP
	 * file for each of those dates. This method uses a minimum and maximum
	 * date. The minimum date is the last date that was archived plus one day,
	 * so it won't archive any dates that have already been archived or
	 * attempted to archive. The maximum date is the current date minus
	 * "archiveDelay" days.
	 * 
	 * @param date the current date
	 */
	private void archiveFiles(LocalDate date) {
		if (archiveDelay <= 0)
			return;
		LocalDate minDate = null;
		if (archivedUntil != null)
			minDate = archivedUntil.plusDays(1);
		LocalDate maxDate = date.minusDays(archiveDelay);
		if (minDate != null && minDate.isAfter(maxDate))
			return;
		archivedUntil = maxDate;
		new ArchiveFilesThread(minDate, maxDate).start();
	}
	
	/**
	 * This thread will archive files between the two specified dates.
	 */
	private class ArchiveFilesThread extends Thread {
		private LocalDate minDate;
		private LocalDate maxDate;
		
		/**
		 * Constructs a new archive files thread.
		 * 
		 * @param minDate the minimum date (inclusive) or null
		 * @param maxDate the maximum date (inclusive)
		 */
		public ArchiveFilesThread(LocalDate minDate, LocalDate maxDate) {
			this.minDate = minDate;
			this.maxDate = maxDate;
		}
		
		@Override
		public void run() {
			File[] files = logDir.listFiles(new ArchiveFilter(minDate,
					maxDate));
			if (files.length == 0)
				return;
			List<File> fileList = new ArrayList<>(Arrays.asList(files));
			Collections.sort(fileList);
			for (File file : fileList) {
				String dateStr = file.getName().substring(0, 8);
				synchronized (ARCHIVE_LOCK) {
					File zipFile = new File(logDir, dateStr + ".zip");
					try {
						ZipUtils.zipFile(file, zipFile);
					} catch (IOException ex) {
						System.err.println("Can't archive log file \"" +
								file.getAbsolutePath() + "\": " +
								ex.getMessage());
						ex.printStackTrace();
						return;
					}
					if (!file.delete()) {
						System.err.println("Can't delete archived log file: " +
								file.getAbsolutePath());
						return;
					}
				}
			}
		}
	}

	/**
	 * This filter matches files that should be archived between two specified
	 * dates.
	 */
	private class ArchiveFilter implements FileFilter {
		private LocalDate minDate;
		private LocalDate maxDate;
		
		/**
		 * Constructs a new archive filter.
		 * 
		 * @param minDate the minimum date (inclusive) or null
		 * @param maxDate the maximum date (inclusive)
		 */
		public ArchiveFilter(LocalDate minDate, LocalDate maxDate) {
			this.minDate = minDate;
			this.maxDate = maxDate;
		}
		
		@Override
		public boolean accept(File pathname) {
			if (archiveDelay <= 0)
				return false;
			if (!pathname.isFile())
				return false;
			String filename = pathname.getName();
			if (!filename.matches("[0-9]{8}\\.log"))
				return false;
			String dateStr = filename.substring(0, 8);
			DateTimeFormatter parser = DateTimeFormatter.ofPattern("yyyyMMdd");
			LocalDate fileDate;
			try {
				fileDate = parser.parse(dateStr, LocalDate::from);
			} catch (IllegalArgumentException ex) {
				return false;
			}
			if (minDate != null && fileDate.isBefore(minDate))
				return false;
			if (fileDate.isAfter(maxDate))
				return false;
			return true;
		}
	}

	/**
	 * This filter matches files that should be purged.
	 */
	private class PurgeFilter implements FileFilter {
		private LocalDate maxDate;

		/**
		 * Constructs a new purge filter.
		 *
		 * @param maxDate the maximum date (inclusive)
		 */
		public PurgeFilter(LocalDate maxDate) {
			this.maxDate = maxDate;
		}

		@Override
		public boolean accept(File pathname) {
			if (purgeDelay <= 0)
				return false;
			if (!pathname.isFile())
				return false;
			String filename = pathname.getName();
			if (!filename.matches("[0-9]{8}\\..*"))
				return false;
			String dateStr = filename.substring(0, 8);
			DateTimeFormatter parser = DateTimeFormatter.ofPattern("yyyyMMdd");
			LocalDate fileDate;
			try {
				fileDate = parser.parse(dateStr, LocalDate::from);
			} catch (IllegalArgumentException ex) {
				return false;
			}
			if (fileDate.isAfter(maxDate))
				return false;
			return true;
		}
	}
}
