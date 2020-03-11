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

package nl.rrd.wool.utils;

import java.io.*;
import java.net.URL;
import java.util.Random;

/**
 * This class contains utility methods related to the file system.
 * 
 * @author Dennis Hofs
 */
public class FileUtils {
	
	/**
	 * Creates the specified directory and all non-existing parent directories.
	 * 
	 * @param dir the directory
	 * @throws IOException if the directory can't be created
	 */
	public static void mkdir(File dir) throws IOException {
		File parent = dir.getParentFile();
		if (parent != null)
			mkdir(parent);
		if (dir.exists()) {
			if (!dir.isDirectory()) {
				throw new IOException("Path is not a directory: " +
						dir.getAbsolutePath());
			}
			return;
		}
		String path = dir.getAbsolutePath();
		String pattern = escapePattern(File.separator + "*");
		if (path.matches(pattern))
			return;
		if (path.matches("[A-Z]:(\\\\)*"))
			return;
		pattern = escapePattern(File.separator + File.separator +
				"[^" + File.separator + "]+(" + File.separator + "[^" +
				File.separator + "]+)?");
		if (path.matches(pattern))
			return;
		if (!dir.mkdir() && (!dir.exists() || !dir.isDirectory())) {
			throw new IOException("Can't create directory: " +
					dir.getAbsolutePath());
		}
	}
	
	/**
	 * Escapes backslashes in the specified regular expression.
	 * 
	 * @param pattern a regular expression
	 * @return the regular expression with escaped backslashes
	 */
	private static String escapePattern(String pattern) {
		return pattern.replaceAll("\\\\", "\\\\\\\\");
	}
	
	/**
	 * Truncates the specified file so it won't be longer than the specified
	 * length. This method will copy the file to a temporary file and then
	 * copy back at most "len" bytes.
	 * 
	 * @param file the file
	 * @param len the length
	 * @throws IOException if a reading/writing error occurs
	 */
	public static void truncate(File file, long len) throws IOException {
		try {
			file = file.getCanonicalFile();
		} catch (IOException ex) {
			file = file.getAbsoluteFile();
		}
		File parent = file.getParentFile();
		if (parent == null) {
			throw new IOException("Can't get parent of file: " +
					file.getAbsolutePath());
		}
		String filename = file.getName();
		File tempFile = createTempFile(parent, filename, "temp");
		copyFile(file, tempFile);
		long toCopy = tempFile.length();
		if (toCopy > len)
			toCopy = len;
		copyFile(tempFile, file, 0, toCopy);
		tempFile.delete();
	}
	
	/**
	 * Creates a temporary file in the specified directory. The file name will
	 * be formatted as "prefix.number.ext", where number is a sequence of eight
	 * hexadecimal digits. This method will use a file name that doesn't
	 * already exist and it atomically creates an empty file with that name.
	 * 
	 * @param dir the directory 
	 * @param prefix the prefix
	 * @param ext the extension
	 * @return the file
	 * @throws IOException if a writing error occurs
	 */
	public static File createTempFile(File dir, String prefix, String ext)
	throws IOException {
		try {
			dir = dir.getCanonicalFile();
		} catch (IOException ex) {
			dir = dir.getAbsoluteFile();
		}
		Random random = new Random();
		int start = random.nextInt();
		int n = start;
		while (true) {
			String filename = prefix + "." +
					String.format("%08X", n) + "." + ext;
			File f = new File(dir, filename);
			if (!f.exists()) {
				if (f.createNewFile())
					return f;
			}
			n++;
			if (n == start) {
				throw new IOException(
						"Can't find free file name for temporary file in " +
						"directory: " + dir.getAbsolutePath());
			}
		}
	}
	
	/**
	 * Copies the specified source file to the specified destination file.
	 * 
	 * @param source the source file
	 * @param dest the destination file
	 * @throws IOException if a read/write error occurs
	 */
	public static void copyFile(File source, File dest) throws IOException {
		copyFile(source, dest, 0, source.length());
	}

	/**
	 * Copies bytes from the specified source file to the specified destination
	 * file.
	 * 
	 * @param source the source file
	 * @param dest the destination file
	 * @param start the index of the first byte to copy
	 * @param len the number of bytes to copy
	 * @throws IOException if a read/write error occurs
	 */
	public static void copyFile(File source, File dest, long start, long len)
	throws IOException {
		FileInputStream in = new FileInputStream(source);
		try {
			FileOutputStream out = new FileOutputStream(dest);
			try {
				copyStream(in, out, start, len);
			} finally {
				out.close();
			}
		} finally {
			in.close();
		}
	}

	public static void downloadFile(URL source, File dest) throws IOException {
		InputStream in = source.openStream();
		try {
			FileOutputStream out = new FileOutputStream(dest);
			try {
				copyStream(in, out, 0, null);
			} finally {
				out.close();
			}
		} finally {
			in.close();
		}
	}

	/**
	 * Copies bytes from the specified source file to the specified destination
	 * file.
	 *
	 * @param in the input stream, at position 0
	 * @param out the output stream
	 * @param start the index of the first byte to copy
	 * @param len the number of bytes to copy, or null to copy until the end of
	 * stream
	 * @throws IOException if a read/write error occurs
	 */
	 private static void copyStream(InputStream in, OutputStream out,
	 		long start, Long len) throws IOException {
		long skipped = 0;
		while (skipped < start) {
			long currSkipped = in.skip(start - skipped);
			if (currSkipped <= 0)
				throw new EOFException("End of file");
			skipped += currSkipped;
		}
		int nread = 0;
		byte[] bs = new byte[1024];
		while (len == null || nread < len) {
			Long toRead = null;
			if (len != null) {
				toRead = len - nread;
				if (toRead > bs.length)
					toRead = (long)bs.length;
			}
			int n;
			if (len == null)
				n = in.read(bs);
			else
				n = in.read(bs, 0, toRead.intValue());
			if (len == null && n <= 0)
				return;
			else if (n <= 0)
				throw new EOFException("End of file");
			out.write(bs, 0, n);
			nread += n;
		}
	 }

	/**
	 * Deletes the specified directory including all its contents. If
	 * <code>dir</code> is a file, it will just delete that file.
	 * 
	 * @param dir the directory or file
	 * @throws IOException if a file or directory can't be deleted
	 */
	public static void deleteTree(File dir) throws IOException {
		if (!dir.exists())
			return;
		if (dir.isDirectory()) {
			File[] children = dir.listFiles();
			for (File f : children) {
				deleteTree(f);
			}
			if (!dir.delete()) {
				throw new IOException("Can't delete directory: " +
						dir.getAbsolutePath());
			}
		} else {
			if (!dir.delete()) {
				throw new IOException("Can't delete file: " +
						dir.getAbsolutePath());
			}
		}
	}

	/**
	 * Deletes the content of the specified directory. If the directory doesn't
	 * exist, this method has no effect.
	 *
	 * @param dir the directory
	 * @throws IOException if the specified path is not a directory or if a
	 * file or directory can't be deleted
	 */
	public static void clearDirectory(File dir) throws IOException {
		if (!dir.exists())
			return;
		if (!dir.isDirectory()) {
			throw new IOException("Path is not a directory: " +
					dir.getAbsolutePath());
		}
		File[] children = dir.listFiles();
		for (File child : children) {
			deleteTree(child);
		}
	}
	
	/**
	 * Reads the content of the specified file as a byte array.
	 * 
	 * @param file the file
	 * @return the content
	 * @throws IOException if a reading error occurs
	 */
	public static byte[] readFileBytes(File file) throws IOException {
		InputStream input = new FileInputStream(file);
		try {
			return readFileBytes(input);
		} finally {
			input.close();
		}
	}
	
	/**
	 * Reads the content of the specified file as a byte array.
	 * 
	 * @param url the URL for the file
	 * @return the content
	 * @throws IOException if a reading error occurs
	 */
	public static byte[] readFileBytes(URL url) throws IOException {
		InputStream input = url.openStream();
		try {
			return readFileBytes(input);
		} finally {
			input.close();
		}
	}
	
	/**
	 * Reads the content of the specified file as a byte array.
	 * 
	 * @param input the input stream from the file
	 * @return the content
	 * @throws IOException if a reading error occurs
	 */
	public static byte[] readFileBytes(InputStream input) throws IOException {
		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		try {
			byte[] bs = new byte[2048];
			int len;
			while ((len = input.read(bs)) > 0) {
				byteOut.write(bs, 0, len);
			}
			return byteOut.toByteArray();
		} finally {
			byteOut.close();
		}
	}
	
	/**
	 * Reads the content of the specified file as a string. It assumes
	 * character encoding UTF-8.
	 * 
	 * @param file the file
	 * @return the content
	 * @throws IOException if a reading error occurs
	 */
	public static String readFileString(File file) throws IOException {
		InputStream input = new FileInputStream(file);
		try {
			return readFileString(input);
		} finally {
			input.close();
		}
	}
	
	/**
	 * Reads the content of the specified file as a string. It assumes
	 * character encoding UTF-8.
	 * 
	 * @param url the URL for the file
	 * @return the content
	 * @throws IOException if a reading error occurs
	 */
	public static String readFileString(URL url) throws IOException {
		InputStream input = url.openStream();
		try {
			return readFileString(input);
		} finally {
			input.close();
		}
	}
	
	/**
	 * Reads the content of the specified file as a string. It assumes
	 * character encoding UTF-8.
	 * 
	 * @param input the input stream from the file
	 * @return the content
	 * @throws IOException if a reading error occurs
	 */
	public static String readFileString(InputStream input) throws IOException {
		return readFileString(new InputStreamReader(input, "UTF-8"));
	}
	
	/**
	 * Reads the content of the specified file as a string.
	 * 
	 * @param reader the reader from the file
	 * @return the content
	 * @throws IOException if a reading error occurs
	 */
	public static String readFileString(Reader reader) throws IOException {
		StringBuilder builder = new StringBuilder();
		char[] cs = new char[2048];
		int len;
		while ((len = reader.read(cs)) > 0) {
			builder.append(cs, 0, len);
		}
		return builder.toString();
	}

	/**
	 * Writes a string to a file with encoding UTF-8.
	 *
	 * @param file the file
	 * @param content the content string to write
	 * @throws IOException if a writing error occurs
	 */
	public static void writeFileString(File file, String content)
	throws IOException {
		FileOutputStream output = new FileOutputStream(file);
		try {
			writeFileString(output, content);
		} finally {
			output.close();
		}
	}

	/**
	 * Writes a string to an output stream with encoding UTF-8.
	 *
	 * @param output the output stream
	 * @param content the content string to write
	 * @throws IOException if a writing error occurs
	 */
	public static void writeFileString(OutputStream output, String content)
	throws IOException {
		Writer writer = new OutputStreamWriter(output, "UTF-8");
		try {
			writer.write(content);
		} finally {
			writer.close();
		}
	}
}
