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

public class LogUtils {
	public static String asciiBytesToString(byte[] bs) {
		return asciiBytesToString(bs, 0, bs.length);
	}

	public static String asciiBytesToString(byte[] bs, int off, int len) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < len; i++) {
			int n = bs[i + off] & 0xff;
			if (n >= 32 && n <= 126) {
				builder.append((char)n);
			} else {
				builder.append(String.format("\\%02x", n));
			}
		}
		return builder.toString();
	}

	public static String bytesToString(byte[] bs) {
		return bytesToString(bs, 0, bs.length);
	}

	public static String bytesToString(byte[] bs, int off, int len) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < len; i++) {
			if (builder.length() > 0)
				builder.append(" ");
			builder.append(String.format("%02x", bs[i + off] & 0xff));
		}
		return builder.toString();
	}
}
