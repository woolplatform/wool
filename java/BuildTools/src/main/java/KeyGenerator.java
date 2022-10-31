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
					result[i] = generatePassword(size);
					break;
				case TYPE_ALPHANUM_PASSWORD:
					result[i] = generateAlphanumPassword(size);
					break;
				case TYPE_SECURE_PASSWORD:
					result[i] = generateSecurePassword(size);
					break;
				case TYPE_PHPMYADMIN_BLOWFISH:
					result[i] = generatePhpMyAdminBlowfish(size);
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
	
	private static String generatePassword(Integer size) {
		if (size == null)
			size = 9;
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < size; i++) {
			String chars = i % 2 == 0 ? CONSONANTS : VOWELS;
			builder.append(chars.charAt(random.nextInt(chars.length())));
		}
		return builder.toString();
	}
	
	private static String generateAlphanumPassword(Integer size) {
		if (size == null)
			size = 12;
		float lowerBoundF = size / 2.0f;
		int lowerBound = (int)Math.ceil(lowerBoundF);
		float upperBoundF = lowerBoundF + size / 4.0f;
		int upperBound = (int)Math.ceil(upperBoundF);
		StringBuilder chars = new StringBuilder();
		for (int i = 0; i < size; i++) {
			char c;
			if (i < lowerBound)
				c = (char)('a' + random.nextInt(26));
			else if (i < upperBound)
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
	
	private static String generateSecurePassword(Integer size) {
		if (size == null)
			size = 12;
		float lowerBoundF = size / 3.0f;
		int lowerBound = (int)Math.ceil(lowerBoundF);
		float upperBoundF = lowerBoundF + size / 4.0f;
		int upperBound = (int)Math.ceil(upperBoundF);
		float digitBoundF = upperBoundF + size / 4.0f;
		int digitBound = (int)Math.ceil(digitBoundF);
		StringBuilder chars = new StringBuilder();
		for (int i = 0; i < size; i++) {
			char c;
			if (i < lowerBound)
				c = (char)('a' + random.nextInt(26));
			else if (i < upperBound)
				c = (char)('A' + random.nextInt(26));
			else if (i < digitBound)
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
	
	private static String generatePhpMyAdminBlowfish(Integer size) {
		StringBuilder chars = new StringBuilder();
		for (int i = 33; i < 127; i++) {
			char c = (char)i;
			if (BLOWFISH_EXLUDE_CHARS.indexOf(c) == -1)
				chars.append(c);
		}
		StringBuilder builder = new StringBuilder();
		if (size == null)
			size = 32;
		for (int i = 0; i < size; i++) {
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
			out.println();
			out.println("(-type | --type | -t | --t) TYPE");
			out.println("    Optional: Type of key to generate.");
			out.println("    - password (default): friendly password with lower-case vowels and");
			out.println("        consonants");
			out.println("        --size: number of characters, default 9");
			out.println();
			out.println("    - alphanum_password: password with alphanumeric characters");
			out.println("        --size: number of characters, default 12");
			out.println();
			out.println("    - secure_password: password with alphanumeric and special characters");
			out.println("        --size: number of characters, default 12");
			out.println();
			out.println("    - phpmyadmin_blowfish:");
			out.println("        --size: number of characters, default 32");
			out.println();
			out.println("    - base64: Base64 key");
			out.println("        --size: number of bits, default 256");
			out.println();
			out.println("(-size | --size | -s | --s) SIZE");
			out.println("    Optional: Size of the key to generate. This depends on the type.");
			out.println();
			out.println("(-n | --n) NUMBER");
			out.println("    Optional: Number of keys to generate. Default: 1.");
			out.println();
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
			if (!arg.matches("--?[a-zA-Z]+")) {
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
