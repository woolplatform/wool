package eu.woolplatform.utils.json;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import eu.woolplatform.utils.json.JsonAtomicToken.Type;

public class JsonObjectStreamReaderTest {
	@Test
	public void runTest() throws Exception {
		String json = "[{\"a\":1,\"b\":2},{\"c\":3},true,null,271," +
				Long.MAX_VALUE + ",\"abcd\",[\"a\",1],{}]";

		final List<Object> items = new ArrayList<Object>();
		Map<String,Object> item = new LinkedHashMap<String,Object>();
		item.put("a", 1);
		item.put("b", 2);
		items.add(item);
		item = new LinkedHashMap<String,Object>();
		item.put("c", 3);
		items.add(item);
		items.add(true);
		items.add(null);
		items.add(271);
		items.add(Long.MAX_VALUE);
		items.add("abcd");
		List<Object> sublist = new ArrayList<Object>();
		sublist.add("a");
		sublist.add(1);
		items.add(sublist);
		item = new LinkedHashMap<String,Object>();
		items.add(item);
		
		runReaderTest(json, new ReaderTest() {
			@Override
			public void runTest(JsonObjectStreamReader reader)
					throws Exception {
				Object value = reader.readValue();
				System.out.println(value);
				Assert.assertEquals(items, value);
			}
		});
		runReaderTest(json, new ReaderTest() {
			@Override
			public void runTest(JsonObjectStreamReader reader)
					throws Exception {
				reader.readToken(Type.START_LIST);
				Iterator<?> expectedIt = items.iterator();
				while (reader.getToken().getType() != Type.END_LIST) {
					Object item = reader.readValue();
					Object expected = expectedIt.next();
					Assert.assertEquals(expected, item);
					if (reader.getToken().getType() != Type.END_LIST)
						reader.readToken(Type.LIST_ITEM_SEPARATOR);
				}
			}
		});
		runReaderTest(json, new ReaderTest() {
			@Override
			public void runTest(JsonObjectStreamReader reader)
					throws Exception {
				Iterator<?> expectedIt = items.iterator();
				reader.readToken(Type.START_LIST);
				Object item = reader.readObject();
				Assert.assertEquals(expectedIt.next(), item);
				reader.readToken(Type.LIST_ITEM_SEPARATOR);
				item = reader.readObject();
				Assert.assertEquals(expectedIt.next(), item);
				reader.readToken(Type.LIST_ITEM_SEPARATOR);
				item = reader.readBoolean();
				Assert.assertEquals(expectedIt.next(), item);
				reader.readToken(Type.LIST_ITEM_SEPARATOR);
				item = reader.readValue();
				Assert.assertEquals(expectedIt.next(), item);
				reader.readToken(Type.LIST_ITEM_SEPARATOR);
				item = reader.readInt();
				Assert.assertEquals(expectedIt.next(), item);
				reader.readToken(Type.LIST_ITEM_SEPARATOR);
				item = reader.readLong();
				Assert.assertEquals(expectedIt.next(), item);
				reader.readToken(Type.LIST_ITEM_SEPARATOR);
				item = reader.readString();
				Assert.assertEquals(expectedIt.next(), item);
				reader.readToken(Type.LIST_ITEM_SEPARATOR);
				item = reader.readList();
				Assert.assertEquals(expectedIt.next(), item);
				reader.readToken(Type.LIST_ITEM_SEPARATOR);
				item = reader.readObject();
				Assert.assertEquals(expectedIt.next(), item);
				reader.readToken(Type.END_LIST);
			}
		});
		
		runFailureTest("128", new ReaderTest() {
			@Override
			public void runTest(JsonObjectStreamReader reader)
					throws Exception {
				reader.readByte();
			}
		});
		runFailureTest("1.0", new ReaderTest() {
			@Override
			public void runTest(JsonObjectStreamReader reader)
					throws Exception {
				reader.readInt();
			}
		});
		runFailureTest("null", new ReaderTest() {
			@Override
			public void runTest(JsonObjectStreamReader reader)
					throws Exception {
				reader.readString();
			}
		});
	}
	
	private void runReaderTest(String json, ReaderTest test) throws Exception {
		JsonStreamReader streamReader = new JsonStreamReader(new StringReader(
				json));
		JsonObjectStreamReader reader = new JsonObjectStreamReader(
				streamReader);
		try {
			test.runTest(reader);
		} finally {
			reader.close();
		}
	}
	
	private void runFailureTest(String json, ReaderTest test) throws Exception {
		JsonStreamReader streamReader = new JsonStreamReader(new StringReader(
				json));
		JsonObjectStreamReader reader = new JsonObjectStreamReader(
				streamReader);
		JsonParseException exception = null;
		try {
			test.runTest(reader);
		} catch (JsonParseException ex) {
			System.out.println("JSON parse error: " + ex.getMessage());
			exception = ex;
		} finally {
			reader.close();
		}
		Assert.assertTrue(exception != null);
	}
	
	private interface ReaderTest {
		void runTest(JsonObjectStreamReader reader) throws Exception;
	}
}
