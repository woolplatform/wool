package eu.woolplatform.utils.json;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import eu.woolplatform.utils.json.JsonAtomicToken.Type;

public class JsonStreamReaderFixture {
	private String newline;
	private List<TestCase> testCases = new ArrayList<TestCase>();
	
	public JsonStreamReaderFixture() {
		newline = System.getProperty("line.separator");
		testCases.addAll(getStringSuccess());
		testCases.addAll(getStringFailure());
		testCases.addAll(getNumberSuccess());
		testCases.addAll(getNumberFailure());
		testCases.addAll(getBooleanSuccess());
		testCases.addAll(getBooleanFailure());
		testCases.addAll(getNullSuccess());
		testCases.addAll(getNullFailure());
		testCases.addAll(getListSuccess());
		testCases.addAll(getListFailure());
		testCases.addAll(getObjectSuccess());
		testCases.addAll(getObjectFailure());
	}
	
	public List<TestCase> getTestCases() {
		return testCases;
	}
	
	public TestCase getLongStringList() {
		StringBuilder json = new StringBuilder("[" + newline);
		List<JsonAtomicToken> tokens = new ArrayList<JsonAtomicToken>();
		tokens.add(new JsonAtomicToken(Type.START_LIST));
		Random random = new Random();
		int listSize = 1000;
		for (int i = 0; i < listSize; i++) {
			StringBuilder builder = new StringBuilder();
			for (int c = 0; c < 1000; c++) {
				builder.append((char)('a' + random.nextInt(26)));
			}
			json.append(indent(1) + "\"" + builder + "\"");
			tokens.add(new JsonAtomicToken(Type.STRING, builder.toString()));
			if (i < listSize - 1) {
				json.append(",");
				builder.append(new JsonAtomicToken(Type.LIST_ITEM_SEPARATOR));
			}
			json.append(newline);
		}
		json.append("]" + newline);
		tokens.add(new JsonAtomicToken(Type.END_LIST));
		return new TestCase(json.toString(), tokens);
	}
	
	private List<TestCase> getStringSuccess() {
		List<TestCase> testCases = new ArrayList<TestCase>();
		TestCase testCase = new TestCase("\"abcd\"", Arrays.asList(
				new JsonAtomicToken(Type.STRING, "abcd")));
		testCases.add(testCase);
		testCase = new TestCase("\"\"", Arrays.asList(
				new JsonAtomicToken(Type.STRING, "")));
		testCases.add(testCase);
		testCase = new TestCase("\"a\\tb c\\nd\\\"e\\\\f\"", Arrays.asList(
				new JsonAtomicToken(Type.STRING, "a\tb c\nd\"e\\f")));
		testCases.add(testCase);
		testCase = new TestCase("\"\\u03b1\\u03B2\\u03b3\"", Arrays.asList(
				new JsonAtomicToken(Type.STRING, "\u03b1\u03b2\u03b3")));
		testCases.add(testCase);
		testCase = new TestCase(indent(1) + "\"abcd\"" + newline, Arrays.asList(
				new JsonAtomicToken(Type.STRING, "abcd")));
		testCases.add(testCase);
		return testCases;
	}
	
	private List<TestCase> getStringFailure() {
		List<TestCase> testCases = new ArrayList<TestCase>();
		testCases.add(new TestCase("", true));
		testCases.add(new TestCase("\"\t\"", true));
		testCases.add(new TestCase("abcd", true));
		testCases.add(new TestCase("\"", true));
		testCases.add(new TestCase("\"abcd", true));
		testCases.add(new TestCase("\"\\a\"", true));
		testCases.add(new TestCase("\"\\\"", true));
		testCases.add(new TestCase("\"\\u03b\"", true));
		testCases.add(new TestCase("\"\\u03bz\"", true));
		testCases.add(new TestCase("\"\\u03", true));
		testCases.add(new TestCase("\"abcd\"{", true));
		return testCases;
	}
	
	private List<TestCase> getNumberSuccess() {
		List<TestCase> testCases = new ArrayList<TestCase>();
		TestCase testCase = new TestCase("271", Arrays.asList(
				new JsonAtomicToken(Type.NUMBER, 271)));
		testCases.add(testCase);
		testCase = new TestCase("-271", Arrays.asList(
				new JsonAtomicToken(Type.NUMBER, -271)));
		testCases.add(testCase);
		testCase = new TestCase("0", Arrays.asList(
				new JsonAtomicToken(Type.NUMBER, 0)));
		testCases.add(testCase);
		testCase = new TestCase(Integer.toString(0x80000000), Arrays.asList(
				new JsonAtomicToken(Type.NUMBER, Integer.MIN_VALUE)));
		testCases.add(testCase);
		testCase = new TestCase(Integer.toString(0x7fffffff), Arrays.asList(
				new JsonAtomicToken(Type.NUMBER, Integer.MAX_VALUE)));
		testCases.add(testCase);
		testCase = new TestCase(Long.toString(0x80000000 - 1L), Arrays.asList(
				new JsonAtomicToken(Type.NUMBER, Integer.MIN_VALUE - 1L)));
		testCases.add(testCase);
		testCase = new TestCase(Long.toString(0x80000000L), Arrays.asList(
				new JsonAtomicToken(Type.NUMBER, Integer.MAX_VALUE + 1L)));
		testCases.add(testCase);
		testCase = new TestCase(Long.toString(Long.MIN_VALUE), Arrays.asList(
				new JsonAtomicToken(Type.NUMBER, Long.MIN_VALUE)));
		testCases.add(testCase);
		testCase = new TestCase(Long.toString(Long.MAX_VALUE), Arrays.asList(
				new JsonAtomicToken(Type.NUMBER, Long.MAX_VALUE)));
		testCases.add(testCase);
		testCase = new TestCase("32.075", Arrays.asList(
				new JsonAtomicToken(Type.NUMBER, 32.075)));
		testCases.add(testCase);
		testCase = new TestCase("32.075E-02", Arrays.asList(
				new JsonAtomicToken(Type.NUMBER, 32.075E-02)));
		testCases.add(testCase);
		testCase = new TestCase("32.075E+02", Arrays.asList(
				new JsonAtomicToken(Type.NUMBER, 32.075E+02)));
		testCases.add(testCase);
		testCase = new TestCase("32e-3", Arrays.asList(
				new JsonAtomicToken(Type.NUMBER, 32e-3)));
		testCases.add(testCase);
		testCase = new TestCase("32e3", Arrays.asList(
				new JsonAtomicToken(Type.NUMBER, 32e3)));
		testCases.add(testCase);
		testCase = new TestCase(indent(1) + "271" + newline, Arrays.asList(
				new JsonAtomicToken(Type.NUMBER, 271)));
		testCases.add(testCase);
		return testCases;
	}
	
	private List<TestCase> getNumberFailure() {
		List<TestCase> testCases = new ArrayList<TestCase>();
		testCases.add(new TestCase("01", true));
		testCases.add(new TestCase("--1", true));
		testCases.add(new TestCase("271.", true));
		testCases.add(new TestCase("32.e3", true));
		testCases.add(new TestCase("32e", true));
		testCases.add(new TestCase("32e+", true));
		testCases.add(new TestCase("271a", true));
		testCases.add(new TestCase("32.34.36", true));
		return testCases;
	}
	
	private List<TestCase> getBooleanSuccess() {
		List<TestCase> testCases = new ArrayList<TestCase>();
		TestCase testCase = new TestCase("true", Arrays.asList(
				new JsonAtomicToken(Type.BOOLEAN, true)));
		testCases.add(testCase);
		testCase = new TestCase("false", Arrays.asList(
				new JsonAtomicToken(Type.BOOLEAN, false)));
		testCases.add(testCase);
		testCase = new TestCase(indent(1) + "true" + newline, Arrays.asList(
				new JsonAtomicToken(Type.BOOLEAN, true)));
		testCases.add(testCase);
		return testCases;
	}
	
	private List<TestCase> getBooleanFailure() {
		List<TestCase> testCases = new ArrayList<TestCase>();
		testCases.add(new TestCase("t", true));
		testCases.add(new TestCase("tRUE", true));
		testCases.add(new TestCase("TRUE", true));
		testCases.add(new TestCase("True", true));
		testCases.add(new TestCase("trues", true));
		return testCases;
	}
	
	private List<TestCase> getNullSuccess() {
		List<TestCase> testCases = new ArrayList<TestCase>();
		TestCase testCase = new TestCase("null", Arrays.asList(
				new JsonAtomicToken(Type.NULL)));
		testCases.add(testCase);
		testCase = new TestCase(indent(1) + "null" + newline, Arrays.asList(
				new JsonAtomicToken(Type.NULL)));
		testCases.add(testCase);
		return testCases;
	}
	
	private List<TestCase> getNullFailure() {
		List<TestCase> testCases = new ArrayList<TestCase>();
		testCases.add(new TestCase("n", true));
		testCases.add(new TestCase("nULL", true));
		testCases.add(new TestCase("NULL", true));
		testCases.add(new TestCase("Null", true));
		testCases.add(new TestCase("null1", true));
		return testCases;
	}
	
	private List<TestCase> getListSuccess() {
		List<TestCase> testCases = new ArrayList<TestCase>();
		testCases.add(getSimpleList(0));
		testCases.add(getComplexList());
		TestCase testCase = new TestCase("[]", Arrays.asList(
				new JsonAtomicToken(Type.START_LIST),
				new JsonAtomicToken(Type.END_LIST)));
		testCases.add(testCase);
		testCase = new TestCase(indent(1) + "[" + newline +
				indent(1) + "]" + newline, Arrays.asList(
				new JsonAtomicToken(Type.START_LIST),
				new JsonAtomicToken(Type.END_LIST)));
		testCases.add(testCase);
		List<JsonAtomicToken> tokens = Arrays.asList(
				new JsonAtomicToken(Type.START_LIST),
				new JsonAtomicToken(Type.NUMBER, 1),
				new JsonAtomicToken(Type.LIST_ITEM_SEPARATOR),
				new JsonAtomicToken(Type.NUMBER, 2),
				new JsonAtomicToken(Type.LIST_ITEM_SEPARATOR),
				new JsonAtomicToken(Type.NUMBER, 3),
				new JsonAtomicToken(Type.END_LIST));
		testCase = new TestCase("[1,2,3]", tokens);
		testCases.add(testCase);
		testCase = new TestCase(indent(1) + "[" + newline +
				indent(2) + "1 ," + newline +
				indent(2) + "2 ," + newline +
				indent(2) + "3" + newline +
				indent(1) + "]" + newline, tokens);
		testCases.add(testCase);
		return testCases;
	}
	
	private List<TestCase> getListFailure() {
		List<TestCase> testCases = new ArrayList<TestCase>();
		testCases.add(new TestCase("[", true));
		testCases.add(new TestCase("[1", true));
		testCases.add(new TestCase("[true", true));
		testCases.add(new TestCase("[\"a\"", true));
		testCases.add(new TestCase("[[1]", true));
		testCases.add(new TestCase("[1,", true));
		testCases.add(new TestCase("[1,]", true));
		testCases.add(new TestCase("[,1]", true));
		testCases.add(new TestCase("[\"a\" \"b\"]", true));
		testCases.add(new TestCase("[\"a\",\"b\"}", true));
		return testCases;
	}
	
	private TestCase getSimpleList(int indent) {
		return getListTestCaseForItems(indent, getSimpleListItems());
	}
	
	private TestCase getComplexList() {
		return getListTestCaseForItems(0, getComplexListItems());
	}
	
	private TestCase getListTestCaseForItems(int indent,
			List<TestCase> items) {
		StringBuilder json = new StringBuilder("[" + newline);
		List<JsonAtomicToken> tokens = new ArrayList<JsonAtomicToken>();
		tokens.add(new JsonAtomicToken(Type.START_LIST));
		for (int i = 0; i < items.size(); i++) {
			TestCase item = items.get(i);
			json.append(indent(indent + 1) + item.json);
			tokens.addAll(item.tokens);
			if (i < items.size() - 1) {
				json.append(",");
				tokens.add(new JsonAtomicToken(Type.LIST_ITEM_SEPARATOR));
			}
			json.append(newline);
		}
		json.append(indent(indent) + "]");
		tokens.add(new JsonAtomicToken(Type.END_LIST));
		return new TestCase(json.toString(), tokens);
	}
	
	private List<TestCase> getObjectSuccess() {
		List<TestCase> testCases = new ArrayList<TestCase>();
		testCases.add(getSimpleObject(0));
		testCases.add(getComplexObject());
		TestCase testCase = new TestCase("{}", Arrays.asList(
				new JsonAtomicToken(Type.START_OBJECT),
				new JsonAtomicToken(Type.END_OBJECT)));
		testCases.add(testCase);
		testCase = new TestCase(indent(1) + "{" + newline +
				indent(1) + "}" + newline, Arrays.asList(
				new JsonAtomicToken(Type.START_OBJECT),
				new JsonAtomicToken(Type.END_OBJECT)));
		testCases.add(testCase);
		List<JsonAtomicToken> tokens = Arrays.asList(
				new JsonAtomicToken(Type.START_OBJECT),
				new JsonAtomicToken(Type.STRING, "a"),
				new JsonAtomicToken(Type.OBJECT_KEY_VALUE_SEPARATOR),
				new JsonAtomicToken(Type.NUMBER, 1),
				new JsonAtomicToken(Type.OBJECT_PAIR_SEPARATOR),
				new JsonAtomicToken(Type.STRING, "b"),
				new JsonAtomicToken(Type.OBJECT_KEY_VALUE_SEPARATOR),
				new JsonAtomicToken(Type.NUMBER, 2),
				new JsonAtomicToken(Type.OBJECT_PAIR_SEPARATOR),
				new JsonAtomicToken(Type.STRING, "c"),
				new JsonAtomicToken(Type.OBJECT_KEY_VALUE_SEPARATOR),
				new JsonAtomicToken(Type.NUMBER, 3),
				new JsonAtomicToken(Type.END_OBJECT));
		testCase = new TestCase("{\"a\":1,\"b\":2,\"c\":3}", tokens);
		testCases.add(testCase);
		testCase = new TestCase(indent(1) + "{" + newline +
				indent(2) + "\"a\" :  1 ," + newline +
				indent(2) + "\"b\" :  2 ," + newline +
				indent(2) + "\"c\" :  3" + newline +
				indent(1) + "}" + newline, tokens);
		testCases.add(testCase);
		return testCases;
	}
	
	private List<TestCase> getObjectFailure() {
		List<TestCase> testCases = new ArrayList<TestCase>();
		testCases.add(new TestCase("{", true));
		testCases.add(new TestCase("{\"", true));
		testCases.add(new TestCase("{\"a", true));
		testCases.add(new TestCase("{\"a\"", true));
		testCases.add(new TestCase("{\"a\":", true));
		testCases.add(new TestCase("{\"a\":1", true));
		testCases.add(new TestCase("{\"a\":1,", true));
		testCases.add(new TestCase("{\"a\":true", true));
		testCases.add(new TestCase("{\"a\":\"b\"", true));
		testCases.add(new TestCase("{\"a\":[1]", true));
		testCases.add(new TestCase("{\"}", true));
		testCases.add(new TestCase("{\"a}", true));
		testCases.add(new TestCase("{\"a\"}", true));
		testCases.add(new TestCase("{\"a\":}", true));
		testCases.add(new TestCase("{\"a\":1,}", true));
		testCases.add(new TestCase("{,\"a\":1}", true));
		testCases.add(new TestCase("{\"a\":1 \"b\":2}", true));
		testCases.add(new TestCase("{\"a\":1,\"b\":2]", true));
		testCases.add(new TestCase("{a:1}", true));
		testCases.add(new TestCase("{\"a\":1,b:2}", true));
		testCases.add(new TestCase("{\"a\",\"b\"}", true));
		return testCases;
	}
	
	private TestCase getSimpleObject(int indent) {
		return getObjectTestCaseForItems(indent, getSimpleListItems());
	}
	
	private TestCase getComplexObject() {
		return getObjectTestCaseForItems(0, getComplexListItems());
	}

	private TestCase getObjectTestCaseForItems(int indent,
			List<TestCase> items) {
		StringBuilder json = new StringBuilder("{" + newline);
		List<JsonAtomicToken> tokens = new ArrayList<JsonAtomicToken>();
		tokens.add(new JsonAtomicToken(Type.START_OBJECT));
		for (int i = 0; i < items.size(); i++) {
			String key = "key" + (i + 1);
			TestCase item = items.get(i);
			json.append(indent(indent + 1) + "\"" + key + "\": " + item.json);
			tokens.addAll(Arrays.asList(
					new JsonAtomicToken(Type.STRING, key),
					new JsonAtomicToken(Type.OBJECT_KEY_VALUE_SEPARATOR)));
			tokens.addAll(item.tokens);
			if (i < items.size() - 1) {
				json.append(",");
				tokens.add(new JsonAtomicToken(Type.OBJECT_PAIR_SEPARATOR));
			}
			json.append(newline);
		}
		json.append(indent(indent) + "}");
		tokens.add(new JsonAtomicToken(Type.END_OBJECT));
		return new TestCase(json.toString(), tokens);
	}
	
	private List<TestCase> getSimpleListItems() {
		return Arrays.asList(
			getStringSuccess().get(0),
			getNumberSuccess().get(0),
			new TestCase("[1,\"a\"]", Arrays.asList(
					new JsonAtomicToken(Type.START_LIST),
					new JsonAtomicToken(Type.NUMBER, 1),
					new JsonAtomicToken(Type.LIST_ITEM_SEPARATOR),
					new JsonAtomicToken(Type.STRING, "a"),
					new JsonAtomicToken(Type.END_LIST))),
			new TestCase("{\"a\":1,\"b\":2}", Arrays.asList(
					new JsonAtomicToken(Type.START_OBJECT),
					new JsonAtomicToken(Type.STRING, "a"),
					new JsonAtomicToken(Type.OBJECT_KEY_VALUE_SEPARATOR),
					new JsonAtomicToken(Type.NUMBER, 1),
					new JsonAtomicToken(Type.OBJECT_PAIR_SEPARATOR),
					new JsonAtomicToken(Type.STRING, "b"),
					new JsonAtomicToken(Type.OBJECT_KEY_VALUE_SEPARATOR),
					new JsonAtomicToken(Type.NUMBER, 2),
					new JsonAtomicToken(Type.END_OBJECT))),
			getNullSuccess().get(0),
			getBooleanSuccess().get(0)
		);
	}
	
	private List<TestCase> getComplexListItems() {
		List<TestCase> items = new ArrayList<TestCase>(getSimpleListItems());
		items.add(getSimpleList(1));
		items.add(getSimpleObject(1));
		return items;
	}
	
	private String indent(int n) {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < n; i++) {
			result.append("    ");
		}
		return result.toString();
	}

	public class TestCase extends JsonObject {
		private String json;
		private List<JsonAtomicToken> tokens = null;
		private boolean exception = false;
		
		private TestCase(String json, List<JsonAtomicToken> tokens) {
			this.json = json;
			this.tokens = tokens;
		}

		private TestCase(String json, boolean exception) {
			this.json = json;
			this.exception = exception;
		}

		public String getJson() {
			return json;
		}

		public List<JsonAtomicToken> getTokens() {
			return tokens;
		}

		public boolean isException() {
			return exception;
		}
	}
}
