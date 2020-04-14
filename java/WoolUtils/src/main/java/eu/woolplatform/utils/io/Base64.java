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
