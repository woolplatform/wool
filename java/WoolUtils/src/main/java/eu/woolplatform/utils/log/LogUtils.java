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
