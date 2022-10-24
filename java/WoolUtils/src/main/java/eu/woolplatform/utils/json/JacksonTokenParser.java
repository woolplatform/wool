package eu.woolplatform.utils.json;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class JacksonTokenParser {
	private JsonParser parser;

	public JacksonTokenParser(JsonParser parser) {
		this.parser = parser;
	}

	public Object readValue() throws IOException, JacksonException {
		validateAtToken();
		switch (parser.getCurrentToken()) {
			case VALUE_STRING:
				return readString();
			case VALUE_NUMBER_INT:
				return readLong();
			case VALUE_NUMBER_FLOAT:
				return readDouble();
			case VALUE_TRUE:
				parser.nextToken();
				return true;
			case VALUE_FALSE:
				parser.nextToken();
				return false;
			case VALUE_NULL:
				parser.nextToken();
				return null;
			case START_ARRAY:
				return readArray();
			case START_OBJECT:
				return readObject();
			case END_ARRAY:
			case END_OBJECT:
			case FIELD_NAME:
				throw new JsonParseException(parser,
						"Unexpected token at start of value: " +
						parser.getCurrentToken());
			case VALUE_EMBEDDED_OBJECT:
			case NOT_AVAILABLE:
			default:
				throw new JsonParseException(parser,
						"Unsupported token: " + parser.getCurrentToken());
		}
	}

	public <T> T readValueOrNull(JsonValueReader<T> reader) throws IOException,
			JacksonException {
		validateAtToken();
		if (parser.getCurrentToken() == JsonToken.VALUE_NULL) {
			parser.nextToken();
			return null;
		}
		return reader.readValue(parser);
	}

	public String readString() throws IOException, JacksonException {
		validateAtToken();
		if (parser.getCurrentToken() != JsonToken.VALUE_STRING) {
			throw new JsonParseException(parser, "Expected token " +
					JsonToken.VALUE_STRING + ", found: " +
					parser.getCurrentToken());
		}
		String result = parser.getValueAsString();
		parser.nextToken();
		return result;
	}

	public boolean readBoolean() throws IOException, JacksonException {
		validateAtToken();
		if (parser.getCurrentToken() == JsonToken.VALUE_TRUE) {
			parser.nextToken();
			return true;
		}
		if (parser.getCurrentToken() == JsonToken.VALUE_FALSE) {
			parser.nextToken();
			return false;
		}
		throw new JsonParseException(parser, "Expected boolean token, found: " +
				parser.getCurrentToken());
	}

	public byte readByte() throws IOException, JacksonException {
		long value = readLong();
		if (value < Byte.MIN_VALUE || value > Byte.MAX_VALUE) {
			throw new JsonParseException(parser, String.format(
					"Value out of range %s - %s: %s",
					Byte.MIN_VALUE, Byte.MAX_VALUE, value));
		}
		return (byte)value;
	}

	public short readShort() throws IOException, JacksonException {
		long value = readLong();
		if (value < Short.MIN_VALUE || value > Short.MAX_VALUE) {
			throw new JsonParseException(parser, String.format(
					"Value out of range %s - %s: %s",
					Short.MIN_VALUE, Short.MAX_VALUE, value));
		}
		return (short)value;
	}

	public int readInt() throws IOException, JacksonException {
		long value = readLong();
		if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
			throw new JsonParseException(parser, String.format(
					"Value out of range %s - %s: %s",
					Integer.MIN_VALUE, Integer.MAX_VALUE, value));
		}
		return (int)value;
	}

	public long readLong() throws IOException, JacksonException {
		validateAtToken();
		if (parser.getCurrentToken() != JsonToken.VALUE_NUMBER_INT) {
			throw new JsonParseException(parser, "Expected token " +
					JsonToken.VALUE_NUMBER_INT + ", found: " +
					parser.getCurrentToken());
		}
		long result = parser.getLongValue();
		parser.nextToken();
		return result;
	}

	public float readFloat() throws IOException, JacksonException {
		return (float)readDouble();
	}

	public double readDouble() throws IOException, JacksonException {
		validateAtToken();
		if (parser.getCurrentToken() == JsonToken.VALUE_NUMBER_FLOAT) {
			double result = parser.getDoubleValue();
			parser.nextToken();
			return result;
		}
		if (parser.getCurrentToken() == JsonToken.VALUE_NUMBER_INT) {
			return (float)readLong();
		}
		throw new JsonParseException(parser, "Expected number token, found: " +
				parser.getCurrentToken());
	}

	public <T> List<T> readArray(JsonValueReader<T> itemReader)
			throws IOException, JacksonException {
		List<T> result = new ArrayList<>();
		validateAtToken();
		if (parser.getCurrentToken() != JsonToken.START_ARRAY) {
			throw new JsonParseException(parser,
					"Expected token " + JsonToken.START_ARRAY + ", found: " +
					parser.getCurrentToken());
		}
		parser.nextToken();
		while (parser.getCurrentToken() != null &&
				parser.getCurrentToken() != JsonToken.END_ARRAY) {
			result.add(itemReader.readValue(parser));
		}
		validateAtToken();
		// at end array
		parser.nextToken();
		return result;
	}

	public List<Object> readArray() throws IOException, JacksonException {
		return readArray(p -> readValue());
	}

	public <T> Map<String,T> readObject(JsonObjectItemReader<T> itemReader)
			throws IOException, JacksonException {
		Map<String,T> result = new LinkedHashMap<>();
		validateAtToken();
		if (parser.getCurrentToken() != JsonToken.START_OBJECT) {
			throw new JsonParseException(parser,
					"Expected token " + JsonToken.START_OBJECT + ", found: " +
					parser.getCurrentToken());
		}
		parser.nextToken();
		while (parser.getCurrentToken() != null &&
				parser.getCurrentToken() != JsonToken.END_OBJECT) {
			if (parser.getCurrentToken() != JsonToken.FIELD_NAME) {
				throw new JsonParseException(parser,
						"Expected token " + JsonToken.FIELD_NAME + ", found: " +
						parser.getCurrentToken());
			}
			String field = parser.getValueAsString();
			parser.nextToken();
			JacksonObjectItem<T> item = itemReader.readValue(parser, field);
			if (!item.isIgnore())
				result.put(field, item.getValue());
		}
		validateAtToken();
		// at end object
		parser.nextToken();
		return result;
	}

	public <T> Map<String,T> readObject(JsonValueReader<T> itemReader)
			throws IOException, JacksonException {
		return readObject((p, field) -> JacksonObjectItem.createValue(
				itemReader.readValue(p)));
	}

	public Map<String,Object> readObject() throws IOException,
			JacksonException {
		return readObject((p, field) -> JacksonObjectItem.createValue(
				readValue()));
	}

	private void validateAtToken() throws IOException, JacksonException {
		if (parser.getCurrentToken() == null) {
			throw new JsonParseException(parser,
					"Unexpected end of JSON content");
		}
	}

	public interface JsonValueReader<T> {
		T readValue(JsonParser p) throws IOException, JacksonException;
	}

	public interface JsonObjectItemReader<T> {
		JacksonObjectItem<T> readValue(JsonParser p, String field)
				throws IOException, JacksonException;
	}
}
