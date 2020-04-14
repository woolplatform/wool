package eu.woolplatform.utils.json;

import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import eu.woolplatform.utils.json.JsonAtomicToken.Type;
import eu.woolplatform.utils.json.JsonStreamReaderFixture.TestCase;

public class JsonStreamReaderTest {
	private String newline;
	private JsonStreamReaderFixture fixture;
	
	public JsonStreamReaderTest() {
		newline = System.getProperty("line.separator");
		fixture = new JsonStreamReaderFixture();
	}
	
	@Test
	public void runTest() throws Exception {
		Writer out = new OutputStreamWriter(System.out);
		try {
			runTest(out, true);
			runTest(out, false);
			TestCase longTestCase = fixture.getLongStringList();
			readTokens(out, true, longTestCase);
		} finally {
			out.close();
		}
	}
	
	private void runTest(Writer out, boolean isStringAtomic) throws Exception {
		for (TestCase testCase : fixture.getTestCases()) {
			if (out != null)
				out.write(testCase + newline);
			JsonParseException exception = null;
			List<JsonAtomicToken> tokens = null;
			try {
				tokens = readTokens(out, isStringAtomic, testCase);
			} catch (JsonParseException ex) {
				exception = ex;
			}
			if (testCase.isException()) {
				if (exception != null && out != null) {
					out.write("Expected JSON parse error: " +
				exception.getMessage() + newline);
				}
				Assert.assertTrue(exception != null);
			} else {
				if (exception != null)
					throw exception;
				if (out != null)
					out.write("Received tokens: " + tokens + newline);
				List<JsonAtomicToken> expectedTokens;
				if (isStringAtomic)
					expectedTokens = testCase.getTokens();
				else
					expectedTokens = splitStringTokens(testCase.getTokens());
				Assert.assertArrayEquals(expectedTokens.toArray(),
						tokens.toArray());
			}
			if (out != null)
				out.write(newline);
		}
	}
	
	private List<JsonAtomicToken> readTokens(Writer out,
			boolean isStringAtomic, TestCase testCase) throws Exception {
		JsonStreamReader reader = new JsonStreamReader(
				new StringReader(testCase.getJson()));
		reader.setIsStringAtomic(isStringAtomic);
		List<JsonAtomicToken> tokens = new ArrayList<JsonAtomicToken>();
		try {
			long start = System.currentTimeMillis();
			boolean hasMore = reader.moveNext();
			while (hasMore) {
				tokens.add(reader.getToken());
				hasMore = reader.moveNext();
			}
			long end = System.currentTimeMillis();
			out.write("Duration: " + (end - start) + " ms" + newline);
			return tokens;
		} finally {
			reader.close();
		}
	}
	
	private List<JsonAtomicToken> splitStringTokens(
			List<JsonAtomicToken> tokens) {
		List<JsonAtomicToken> result = new ArrayList<JsonAtomicToken>();
		for (JsonAtomicToken token : tokens) {
			if (token.getType() == Type.STRING) {
				String s = (String)token.getValue();
				result.add(new JsonAtomicToken(Type.START_STRING));
				for (int i = 0; i < s.length(); i++) {
					result.add(new JsonAtomicToken(Type.STRING_CHARACTER,
							Character.toString(s.charAt(i))));
				}
				result.add(new JsonAtomicToken(Type.END_STRING));
			} else {
				result.add(token);
			}
		}
		return result;
	}
}
