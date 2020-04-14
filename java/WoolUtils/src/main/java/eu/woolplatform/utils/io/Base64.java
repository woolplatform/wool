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

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class Base64 {
	public static String encode(byte[] bs) {
		byte[] base64Bytes = org.apache.commons.codec.binary.Base64
				.encodeBase64(bs);
		Charset utf8 = Charset.forName("UTF-8");
		ByteBuffer bb = ByteBuffer.wrap(base64Bytes);
		return utf8.decode(bb).toString();
	}

	public static byte[] decode(String base64) {
		Charset utf8 = Charset.forName("UTF-8");
		ByteBuffer bb = utf8.encode(base64);
		byte[] base64Bytes = new byte[bb.remaining()];
		bb.get(base64Bytes);
		return org.apache.commons.codec.binary.Base64.decodeBase64(base64Bytes);
	}
}
