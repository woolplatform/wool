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

package eu.woolplatform.utils.json;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This class can read JSON values and tokens from a stream. This is a
 * higher-level interface built upon {@link JsonStreamReader JsonStreamReader},
 * which returns atomic tokens. There are read methods that you can call when
 * you expect a specific type of value (for example a string or an integer) or
 * token (for example START_LIST). Each method validates and consumes the
 * relevant tokens.
 * 
 * @author Dennis Hofs (RRD)
 */
public class JsonObjectStreamReader implements Closeable {
	private JsonStreamReader reader;
	
	/**
	 * Constructs a new JSON object stream reader. It reads UTF-8 characters
	 * from the specified input stream.
	 * 
	 * @param input the input stream
	 */
	public JsonObjectStreamReader(InputStream input) {
		this.reader = new JsonStreamReader(input);
	}
	
	/**
	 * Constructs a new JSON object stream reader.
	 * 
	 * @param reader the underlying reader
	 */
	public JsonObjectStreamReader(Reader reader) {
		this.reader = new JsonStreamReader(reader);
	}
	
	/**
	 * Constructs a new JSON object stream reader.
	 * 
	 * @param reader the underlying reader
	 */
	public JsonObjectStreamReader(JsonStreamReader reader) {
		this.reader = reader;
	}
	
	/**
	 * Closes this reader and the underlying reader.
	 * 
	 * @throws IOException if the underlying reader can't be closed
	 */
	public void close() throws IOException {
		reader.close();
	}
	
	/**
	 * Returns the current line number in the document. The first line is 1.
	 * If the reader is positioned at a token, then the document position is at
	 * the end of that token.
	 * 
	 * @return the current line number
	 */
	public int getDocumentLine() {
		return reader.getDocumentLine();
	}

	/**
	 * Returns the current character number in the current line in the
	 * document. The first character is 1. If the reader is positioned at a
	 * token, then the document position is at the end of that token.
	 * 
	 * @return the current character number
	 */
	public int getDocumentLinePos() {
		return reader.getDocumentLinePos();
	}
	
	/**
	 * Returns the line number in the document where the current token starts.
	 * The first line is 1. If the reader is before the first token, then this
	 * method returns 1. If the reader is after the last token, then this method
	 * return the line at the end of the document.
	 * 
	 * @return the line number where the current token starts
	 */
	public int getTokenStartLine() {
		return reader.getTokenStartLine();
	}
	
	/**
	 * Returns the character number in the line in the document where the
	 * current token starts. The first character is 1. If the reader is before
	 * the first token, then this method returns 1. If the reader is after the
	 * last token, then this method return the line at the end of the document.
	 * 
	 * @return the character number where the current token starts
	 */
	public int getTokenStartLinePos() {
		return reader.getTokenStartLinePos();
	}

	/**
	 * Returns the current token. If the reader is at the end of the document,
	 * this method returns null.
	 * 
	 * @return the current token
	 * @throws JsonParseException if the JSON content is invalid
	 * @throws IOException if a reading error occurs
	 */
	public JsonAtomicToken getToken() throws JsonParseException, IOException {
		if (!moveToToken())
			return null;
		return reader.getToken();
	}
	
	/**
	 * Reads the next token. After this method the reader will be positioned
	 * after the returned token. If there are no more tokens, this method
	 * returns null.
	 * 
	 * @return the token or null
	 * @throws JsonParseException if the JSON content is invalid
	 * @throws IOException if a reading error occurs
	 */
	public JsonAtomicToken readToken()
			throws JsonParseException, IOException {
		if (!moveToToken())
			return null;
		JsonAtomicToken token = reader.getToken();
		reader.moveNext();
		return token;
	}
	
	/**
	 * Reads the next token and validates that it has the specified type. If
	 * this method succeeds, the reader will be positioned after the returned
	 * token. Otherwise it will remain at the current position.
	 * 
	 * @param type the token type
	 * @return the token with the specified type
	 * @throws JsonParseException if the JSON content is invalid, there is no
	 * more token, or the next token has a different type
	 * @throws IOException if a reading error occurs
	 */
	public JsonAtomicToken readToken(JsonAtomicToken.Type type)
			throws JsonParseException, IOException {
		validateCurrentToken(type);
		return readToken();
	}
	
	/**
	 * Validates whether the reader is positioned at a token of the specified
	 * type.
	 * 
	 * @param type the token type
	 * @return the current token
	 * @throws JsonParseException if the JSON content is invalid, there is no
	 * more token, or the current token has a different type
	 * @throws IOException if a reading error occurs
	 */
	private JsonAtomicToken validateCurrentToken(JsonAtomicToken.Type type)
			throws JsonParseException, IOException {
		if (!moveToToken()) {
			throw new JsonParseException("Expected token " + type +
					", found end of document", reader.getDocumentLine(),
					reader.getDocumentLinePos());
		}
		JsonAtomicToken token = reader.getToken();
		if (token.getType() != type) {
			throw new JsonParseException("Expected token " + type +
					", found " + token.getType(), reader.getDocumentLine(),
					reader.getDocumentLinePos());
		}
		return token;
	}
	
	/**
	 * Reads a string value (not null). If this method succeeds, the reader
	 * will be positioned after the string.
	 * 
	 * @return the string (not null)
	 * @throws JsonParseException if the JSON content is invalid or the reader
	 * is not positioned at the start of a string
	 * @throws IOException if a reading error occurs
	 */
	public String readString() throws JsonParseException, IOException {
		if (reader.isStringAtomic()) {
			JsonAtomicToken token = readToken(JsonAtomicToken.Type.STRING);
			return (String)token.getValue();
		} else {
			StringBuilder builder = new StringBuilder();
			readToken(JsonAtomicToken.Type.START_STRING);
			JsonAtomicToken token = readToken();
			while (token.getType() != JsonAtomicToken.Type.END_STRING) {
				builder.append((String)token.getValue());
				token = readToken();
			}
			return builder.toString();
		}
	}
	
	/**
	 * Reads a byte value. This method validates whether the reader is
	 * positioned at a number token and its value is a byte. If this method
	 * succeeds, the reader will be positioned after the byte value.
	 * 
	 * @return the byte value
	 * @throws JsonParseException if the JSON content is invalid or the reader
	 * is not positioned at the start of a byte value
	 * @throws IOException if a reading error occurs
	 */
	public byte readByte() throws JsonParseException, IOException {
		JsonAtomicToken token = validateCurrentToken(
				JsonAtomicToken.Type.NUMBER);
		if (token.getValue() instanceof Double) {
			throw new JsonParseException("Number is not a byte: " +
					token.getValue(), reader.getDocumentLine(),
					reader.getDocumentLinePos());
		}
		long val = ((Number)token.getValue()).longValue();
		if (val < Byte.MIN_VALUE || val > Byte.MAX_VALUE) {
			throw new JsonParseException("Number value out of byte range: " +
					val, reader.getDocumentLine(),
					reader.getDocumentLinePos());
		}
		readToken();
		return (byte)val;
	}
	
	/**
	 * Reads a short value. This method validates whether the reader is
	 * positioned at a number token and its value is a short. If this method
	 * succeeds, the reader will be positioned after the short value.
	 * 
	 * @return the short value
	 * @throws JsonParseException if the JSON content is invalid or the reader
	 * is not positioned at the start of a short value
	 * @throws IOException if a reading error occurs
	 */
	public short readShort() throws JsonParseException, IOException {
		JsonAtomicToken token = validateCurrentToken(
				JsonAtomicToken.Type.NUMBER);
		if (token.getValue() instanceof Double) {
			throw new JsonParseException("Number is not a short: " +
					token.getValue(), reader.getDocumentLine(),
					reader.getDocumentLinePos());
		}
		long val = ((Number)token.getValue()).longValue();
		if (val < Short.MIN_VALUE || val > Short.MAX_VALUE) {
			throw new JsonParseException("Number value out of short range: " +
					val, reader.getDocumentLine(),
					reader.getDocumentLinePos());
		}
		readToken();
		return (short)val;
	}
	
	/**
	 * Reads an integer value. This method validates whether the reader is
	 * positioned at a number token and its value is an integer. If this method
	 * succeeds, the reader will be positioned after the integer.
	 * 
	 * @return the integer value
	 * @throws JsonParseException if the JSON content is invalid or the reader
	 * is not positioned at the start of an integer
	 * @throws IOException if a reading error occurs
	 */
	public int readInt() throws JsonParseException, IOException {
		JsonAtomicToken token = validateCurrentToken(
				JsonAtomicToken.Type.NUMBER);
		if (token.getValue() instanceof Double) {
			throw new JsonParseException("Number is not an int: " +
					token.getValue(), reader.getDocumentLine(),
					reader.getDocumentLinePos());
		}
		long val = ((Number)token.getValue()).longValue();
		if (val < Integer.MIN_VALUE || val > Integer.MAX_VALUE) {
			throw new JsonParseException("Number value out of int range: " +
					val, reader.getDocumentLine(),
					reader.getDocumentLinePos());
		}
		readToken();
		return (int)val;
	}
	
	/**
	 * Reads a long value. This method validates whether the reader is
	 * positioned at a number token and its value is a long. If this method
	 * succeeds, the reader will be positioned after the long value.
	 * 
	 * @return the long value
	 * @throws JsonParseException if the JSON content is invalid or the reader
	 * is not positioned at the start of a long value
	 * @throws IOException if a reading error occurs
	 */
	public long readLong() throws JsonParseException, IOException {
		JsonAtomicToken token = validateCurrentToken(
				JsonAtomicToken.Type.NUMBER);
		if (token.getValue() instanceof Double) {
			throw new JsonParseException("Number is not a long: " +
					token.getValue(), reader.getDocumentLine(),
					reader.getDocumentLinePos());
		}
		readToken();
		return ((Number)token.getValue()).longValue();
	}
	
	/**
	 * Reads a number token and returns its float value. If this method
	 * succeeds, the reader will be positioned after the number.
	 * 
	 * @return the float value
	 * @throws JsonParseException if the JSON content is invalid or the reader
	 * is not positioned at the start of a number
	 * @throws IOException if a reading error occurs
	 */
	public float readFloat() throws JsonParseException, IOException {
		JsonAtomicToken token = readToken(JsonAtomicToken.Type.NUMBER);
		return ((Number)token.getValue()).floatValue();
	}
	
	/**
	 * Reads a number token and returns its double value. If this method
	 * succeeds, the reader will be positioned after the number.
	 * 
	 * @return the double value
	 * @throws JsonParseException if the JSON content is invalid or the reader
	 * is not positioned at the start of a number
	 * @throws IOException if a reading error occurs
	 */
	public double readDouble() throws JsonParseException, IOException {
		JsonAtomicToken token = readToken(JsonAtomicToken.Type.NUMBER);
		return ((Number)token.getValue()).doubleValue();
	}
	
	/**
	 * Reads a boolean value. If this method succeeds, the reader will be
	 * positioned after the boolean value.
	 * 
	 * @return the boolean value
	 * @throws JsonParseException if the JSON content is invalid or the reader
	 * is not positioned at the start of a boolean value
	 * @throws IOException if a reading error occurs
	 */
	public boolean readBoolean() throws JsonParseException, IOException {
		JsonAtomicToken token = readToken(JsonAtomicToken.Type.BOOLEAN);
		return (Boolean)token.getValue();
	}
	
	/**
	 * Reads an object value. If this method succeeds, the reader will be
	 * positioned after the object value.
	 * 
	 * @return the object value
	 * @throws JsonParseException if the JSON content is invalid or the reader
	 * is not positioned at the start of an object value
	 * @throws IOException if a reading error occurs
	 */
	public Map<String,?> readObject() throws JsonParseException, IOException {
		Map<String,Object> result = new LinkedHashMap<>();
		readToken(JsonAtomicToken.Type.START_OBJECT);
		while (reader.getToken().getType() !=
				JsonAtomicToken.Type.END_OBJECT) {
			String key = readString();
			readToken(JsonAtomicToken.Type.OBJECT_KEY_VALUE_SEPARATOR);
			result.put(key, readValue());
			if (reader.getToken().getType() ==
					JsonAtomicToken.Type.OBJECT_PAIR_SEPARATOR) {
				readToken();
			}
		}
		readToken();
		return result;
	}
	
	/**
	 * Reads a list value. If this method succeeds, the reader will be
	 * positioned after the list value.
	 * 
	 * @return the list value
	 * @throws JsonParseException if the JSON content is invalid or the reader
	 * is not positioned at the start of a list value
	 * @throws IOException if a reading error occurs
	 */
	public List<?> readList() throws JsonParseException, IOException {
		List<Object> result = new ArrayList<>();
		readToken(JsonAtomicToken.Type.START_LIST);
		while (reader.getToken().getType() != JsonAtomicToken.Type.END_LIST) {
			result.add(readValue());
			if (reader.getToken().getType() ==
					JsonAtomicToken.Type.LIST_ITEM_SEPARATOR) {
				readToken();
			}
		}
		readToken();
		return result;
	}
	
	/**
	 * Reads a JSON value. If this method succeeds, the reader will be
	 * positioned after the value. It returns one of the following types:
	 * 
	 * <p><ul>
	 * <li>{@link String String}</li>
	 * <li>{@link Integer Integer}</li>
	 * <li>{@link Long Long}</li>
	 * <li>{@link Double Double}</li>
	 * <li>{@link Boolean Boolean}</li>
	 * <li>{@link Map Map}</li>
	 * <li>{@link List List}</li>
	 * <li>null</li>
	 * </ul></p>
	 * 
	 * @return the value
	 * @throws JsonParseException if the JSON content is invalid or the reader
	 * is not positioned at the start of a value
	 * @throws IOException if a reading error occurs
	 */
	public Object readValue() throws JsonParseException, IOException {
		if (!moveToToken()) {
			throw new JsonParseException("End of document",
					reader.getDocumentLine(), reader.getDocumentLinePos());
		}
		JsonAtomicToken token = reader.getToken();
		switch (token.getType()) {
		case STRING:
		case START_STRING:
			return readString();
		case NUMBER:
			token = readToken(JsonAtomicToken.Type.NUMBER);
			return token.getValue();
		case BOOLEAN:
			return readBoolean();
		case NULL:
			readToken(JsonAtomicToken.Type.NULL);
			return null;
		case START_OBJECT:
			return readObject();
		case START_LIST:
			return readList();
		default:
			throw new JsonParseException(
					"Expected start of value, found token " + token.getType(),
					reader.getDocumentLine(), reader.getDocumentLinePos());
		}
	}
	
	/**
	 * If the reader is positioned at the start of the document, it moves the
	 * reader to the first token. It returns true if the reader is at a token.
	 * If it returns false, it means that the reader is at the end of the
	 * document.
	 * 
	 * @return true if the reader is at a token, false if it is at the end of
	 * the document
	 * @throws JsonParseException if the JSON content is invalid
	 * @throws IOException if a reading error occurs
	 */
	private boolean moveToToken() throws JsonParseException, IOException {
		if (reader.getToken() == null)
			return reader.moveNext();
		else
			return true;
	}
}
