import java.io.PrintStream;
import java.lang.reflect.Field;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

public class KeyGenerator {
	public static final String TYPE_PASSWORD = "password";
	public static final String TYPE_ALPHANUM_PASSWORD = "alphanum_password";
	public static final String TYPE_SECURE_PASSWORD = "secure_password";
	public static final String TYPE_PHPMYADMIN_BLOWFISH =
			"phpmyadmin_blowfish";
	public static final String TYPE_BASE64_256BITS = "base64_256bits";
	
	private static final String BLOWFISH_EXLUDE_CHARS = "'\"\\";
	private static final String CONSONANTS = "bcdfghjklmnpqrstvwxz";
	private static final String VOWELS = "aeiouy";
	private static final String SPECIALS = "!@#$%&*-=+";
	
	private static SecureRandom random = new SecureRandom();
	
	private static String[] generateKeys(String type, int repeat) {
		String[] result = new String[repeat];
		for (int i = 0; i < repeat; i++) {
			switch (type) {
				case TYPE_PASSWORD:
					result[i] = generatePassword();
					break;
				case TYPE_ALPHANUM_PASSWORD:
					result[i] = generateAlphanumPassword();
					break;
				case TYPE_SECURE_PASSWORD:
					result[i] = generateSecurePassword();
					break;
				case TYPE_PHPMYADMIN_BLOWFISH:
					result[i] = generatePhpMyAdminBlowfish();
					break;
				case TYPE_BASE64_256BITS:
					result[i] = generateBase64Key(256);
					break;
				default:
					System.err.println("Key type not implemented: " + type);
					System.exit(1);
					return null;
			}
		}
		return result;
	}
	
	private static String generatePassword() {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < 9; i++) {
			String chars = i % 2 == 0 ? CONSONANTS : VOWELS;
			builder.append(chars.charAt(random.nextInt(chars.length())));
		}
		return builder.toString();
	}
	
	private static String generateAlphanumPassword() {
		StringBuilder chars = new StringBuilder();
		for (int i = 0; i < 12; i++) {
			char c;
			if (i < 6)
				c = (char)('a' + random.nextInt(26));
			else if (i < 9)
				c = (char)('A' + random.nextInt(26));
			else
				c = (char)('0' + random.nextInt(10));
			chars.append(c);
		}
		StringBuilder result = new StringBuilder();
		while (chars.length() > 0) {
			int index = random.nextInt(chars.length());
			result.append(chars.charAt(index));
			chars.deleteCharAt(index);
		}
		return result.toString();
	}
	
	private static String generateSecurePassword() {
		StringBuilder chars = new StringBuilder();
		for (int i = 0; i < 12; i++) {
			char c;
			if (i < 4)
				c = (char)('a' + random.nextInt(26));
			else if (i < 7)
				c = (char)('A' + random.nextInt(26));
			else if (i < 10)
				c = (char)('0' + random.nextInt(10));
			else
				c = SPECIALS.charAt(random.nextInt(SPECIALS.length()));
			chars.append(c);
		}
		StringBuilder result = new StringBuilder();
		while (chars.length() > 0) {
			int index = random.nextInt(chars.length());
			result.append(chars.charAt(index));
			chars.deleteCharAt(index);
		}
		return result.toString();
	}
	
	private static String generatePhpMyAdminBlowfish() {
		StringBuilder chars = new StringBuilder();
		for (int i = 33; i < 127; i++) {
			char c = (char)i;
			if (BLOWFISH_EXLUDE_CHARS.indexOf(c) == -1)
				chars.append(c);
		}
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < 40; i++) {
			char c = chars.charAt(random.nextInt(chars.length()));
			builder.append(c);
		}
		return builder.toString();
	}
	
	private static String generateBase64Key(int bits) {
		byte[] bs = new byte[bits / 8];
		random.nextBytes(bs);
		return Base64.getEncoder().encodeToString(bs);
	}
	
	private static String parseType(String type) {
		String fieldName = "TYPE_" + type.toUpperCase();
		try {
			Field field = KeyGenerator.class.getField(fieldName);
			return (String)field.get(null);
		} catch (Exception ex) {
			System.err.println("Invalid type: " + type);
			System.err.println();
			exitUsage(1);
			return null;
		}
	}
	
	private static Integer parseNumber(String numStr) {
		try {
			return Integer.parseInt(numStr);
		} catch (Exception ex) {
			System.err.println("Not a number: " + numStr);
			System.err.println();
			exitUsage(1);
			return null;
		}
	}
	
	private static void exitUsage(int exitCode) {
		try (PrintStream out = exitCode == 0 ? System.out : System.err) {
			out.println("Usage: java KeyGenerator ARGS");
			out.println();
			out.println("Arguments:");
			out.println("(-type | --type | -t | --t) TYPE");
			out.println("    Optional: Type of key to generate.");
			out.println("    - password (default): friendly password with lower-case vowels and");
			out.println("          consonants");
			out.println("    - alphanum_password: password with alphanumeric characters");
			out.println("    - secure_password: password with alphanumeric and special characters");
			out.println("    - phpmyadmin_blowfish");
			out.println("    - base64_256bits");
			out.println("(-n | --n) NUMBER");
			out.println("    Optional: Number of keys to generate. Default: 1.");
			out.println("(-help | --help | -h | --h)");
			out.println("    Print this usage.");
		}
		System.exit(exitCode);
	}
	
	public static void main(String[] args) {
		Map<String,String> params = new LinkedHashMap<>();
		int i = 0;
		while (i < args.length) {
			String arg = args[i++];
			if (!arg.matches("--?[a-zA-Z]")) {
				exitUsage(1);
				return;
			}
			String key = arg.replaceAll("^-+", "").toLowerCase();
			if (key.equals("type") || key.equals("t")) {
				if (i >= args.length) {
					exitUsage(1);
					return;
				}
				params.put("type", args[i++]);
			} else if (key.equals("n")) {
				if (i >= args.length) {
					exitUsage(1);
					return;
				}
				params.put("n", args[i++]);
			} else if (key.equals("help") || key.equals("h")) {
				exitUsage(0);
				return;
			} else {
				exitUsage(1);
				return;
			}
		}
		String type = TYPE_PASSWORD;
		if (params.containsKey("type")) {
			type = parseType(params.get("type"));
			if (type == null)
				return;
		}
		Integer n = 1;
		if (params.containsKey("n")) {
			n = parseNumber(params.get("n"));
			if (n == null)
				return;
			if (n <= 0) {
				System.err.println("Number not greater than 0: " + n);
				System.err.println();
				exitUsage(1);
				return;
			}
		}
		String[] keys = generateKeys(type, n);
		if (keys == null)
			return;
		for (String key : keys) {
			System.out.println(key);
		}
	}
}
