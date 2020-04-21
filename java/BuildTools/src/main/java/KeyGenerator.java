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
	public static final String TYPE_BASE64 = "base64";
	
	private static final String BLOWFISH_EXLUDE_CHARS = "'\"\\";
	private static final String CONSONANTS = "bcdfghjklmnpqrstvwxz";
	private static final String VOWELS = "aeiouy";
	private static final String SPECIALS = "!@#$%&*-=+";
	
	private static SecureRandom random = new SecureRandom();
	
	private static String[] generateKeys(String type, Integer size,
			int repeat) throws KeyGeneratorException {
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
				case TYPE_BASE64:
					result[i] = generateBase64Key(size);
					break;
				default:
					throw new KeyGeneratorException(
							"Key type not implemented: " + type);
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
	
	private static String generateBase64Key(Integer bits)
			throws KeyGeneratorException {
		if (bits == null)
			bits = 256;
		if (bits % 8 != 0)
			throw new KeyGeneratorException("Size must be a multiple of 8");
		byte[] bs = new byte[bits / 8];
		random.nextBytes(bs);
		return Base64.getEncoder().encodeToString(bs);
	}
	
	private static String parseType(String type) throws KeyGeneratorException {
		String fieldName = "TYPE_" + type.toUpperCase();
		try {
			Field field = KeyGenerator.class.getField(fieldName);
			return (String) field.get(null);
		} catch (NoSuchFieldException | IllegalAccessException ex) {
			throw new KeyGeneratorException("Invalid type: " + type);
		}
	}
	
	private static Integer parseNumber(String numStr, Integer min)
			throws KeyGeneratorException {
		int result;
		try {
			result = Integer.parseInt(numStr);
		} catch (NumberFormatException ex) {
			throw new KeyGeneratorException("Not a number: " + numStr);
		}
		if (min != null && result < min)
			throw new KeyGeneratorException("Number must be at least " + min);
		return result;
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
			out.println("    - base64: Base64 key with 256 bits. You may change the size with");
			out.println("          argument --size");
			out.println("(-size | --size | -s | --s) SIZE");
			out.println("    Optional: Size of the key to generate. This depends on the type:");
			out.println("    - base64: size in bits (default 256)");
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
			} else if (key.equals("size") || key.equals("s")) {
				if (i >= args.length) {
					exitUsage(1);
					return;
				}
				params.put("size", args[i++]);
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
		try {
			if (params.containsKey("type"))
				type = parseType(params.get("type"));
			Integer size = null;
			if (params.containsKey("size"))
				size = parseNumber(params.get("size"), 1);
			Integer n = 1;
			if (params.containsKey("n"))
				n = parseNumber(params.get("n"), 1);
			String[] keys = generateKeys(type, size, n);
			for (String key : keys) {
				System.out.println(key);
			}
		} catch (KeyGeneratorException ex) {
			System.err.println(ex.getMessage());
			System.err.println();
			exitUsage(1);
		}
	}
}
