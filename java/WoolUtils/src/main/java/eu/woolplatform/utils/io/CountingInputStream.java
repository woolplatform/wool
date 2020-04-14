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
import java.io.InputStream;

/**
 * This input stream keeps track of the current position in an underlying input
 * stream. It assumes that the current position in the underlying stream at
 * construction is 0. The counting input stream does not support mark and
 * reset.
 * 
 * @author Dennis Hofs
 */
public class CountingInputStream extends InputStream {
	private InputStream input;
	private long position = 0;
	
	/**
	 * Constructs a new counting input stream.
	 * 
	 * @param input the underlying stream at position 0
	 */
	public CountingInputStream(InputStream input) {
		this.input = input;
	}
	
	/**
	 * Returns the current position.
	 * 
	 * @return the current position
	 */
	public long getPosition() {
		return position;
	}

	@Override
	public int read() throws IOException {
		int result = input.read();
		if (result != -1)
			position++;
		return result;
	}

	@Override
	public int available() throws IOException {
		return input.available();
	}

	@Override
	public void close() throws IOException {
		input.close();
	}

	@Override
	public synchronized void mark(int readlimit) {
	}

	@Override
	public boolean markSupported() {
		return false;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		int result = input.read(b, off, len);
		if (result > 0)
			position += result;
		return result;
	}

	@Override
	public int read(byte[] b) throws IOException {
		int result = input.read(b);
		if (result > 0)
			position += result;
		return result;
	}

	@Override
	public synchronized void reset() throws IOException {
	}

	@Override
	public long skip(long n) throws IOException {
		long result = input.skip(n);
		position += result;
		return result;
	}
}
