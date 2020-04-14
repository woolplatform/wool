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

package eu.woolplatform.utils.io;

import java.io.IOException;
import java.io.Writer;
import java.net.URLEncoder;

/**
 * This class encodes strings using the {@link URLEncoder URLEncoder} and
 * character set UTF-8.
 * 
 * @author Dennis Hofs
 */
public class URLEncodedWriter extends Writer {
	private Writer writer;
	
	/**
	 * Constructs a new URL-encoded writer.
	 * 
	 * @param writer the writer to which the URL-encoded strings should be
	 * written
	 */
	public URLEncodedWriter(Writer writer) {
		this.writer = writer;
	}
	
	@Override
	public void close() throws IOException {
		writer.close();
	}

	@Override
	public void flush() throws IOException {
		writer.flush();
	}
	
	@Override
	public void write(char[] cbuf) throws IOException {
		write(new String(cbuf));
	}

	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		write(new String(cbuf, off, len));
	}

	@Override
	public void write(int c) throws IOException {
		write(Character.toString((char)c));
	}
	
	@Override
	public void write(String str) throws IOException {
		writer.write(URLEncoder.encode(str, "UTF-8"));
	}
	
	@Override
	public void write(String str, int off, int len) throws IOException {
		write(str.substring(off, off + len));
	}
}
