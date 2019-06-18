package nl.rrd.wool.json;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import nl.rrd.wool.exception.ParseException;

public class JsonMapper {

	/**
	 * Converts the specified JSON string to an object of the specified class
	 * using the Jackson {@link ObjectMapper ObjectMapper}.
	 * 
	 * @param json the JSON string
	 * @param clazz the result class
	 * @param <T> the type of object to return
	 * @return the result object
	 * @throws ParseException if a JSON parsing error occurs
	 */
	public static <T> T parse(String json, Class<T> clazz)
			throws ParseException {
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.readValue(json, clazz);
		} catch (JsonParseException ex) {
			throw new ParseException("Can't parse JSON code: " +
					ex.getMessage(), ex);
		} catch (JsonMappingException ex) {
			throw new ParseException("Can't map JSON code to object: " +
					ex.getMessage(), ex);
		} catch (IOException ex) {
			throw new RuntimeException("I/O error when reading from string: " +
					ex.getMessage(), ex);
		}
	}

	/**
	 * Converts the specified JSON string to an object of the specified result
	 * type using the Jackson {@link ObjectMapper ObjectMapper}.
	 * 
	 * @param json the JSON string
	 * @param typeRef the result type
	 * @param <T> the type of object to return
	 * @return the result object
	 * @throws ParseException if a JSON parsing error occurs
	 */
	public static <T> T parse(String json, TypeReference<T> typeRef)
			throws ParseException {
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.readValue(json, typeRef);
		} catch (JsonParseException ex) {
			throw new ParseException("Can't parse JSON code: " +
					ex.getMessage(), ex);
		} catch (JsonMappingException ex) {
			throw new ParseException("Can't map JSON code to object: " +
					ex.getMessage(), ex);
		} catch (IOException ex) {
			throw new RuntimeException("I/O error when reading from string: " +
					ex.getMessage(), ex);
		}
	}

	/**
	 * Converts the specified JSON object (the result of parsing JSON code to
	 * a basic Java type) to an object of the specified class using the Jackson
	 * {@link ObjectMapper ObjectMapper}.
	 *
	 * @param json the JSON object
	 * @param clazz the result class
	 * @param <T> the type of object to return
	 * @return the result object
	 * @throws ParseException if the JSON object can't be converted to the
	 * specified class
	 */
	public static <T> T convert(Object json, Class<T> clazz)
			throws ParseException {
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.convertValue(json, clazz);
		} catch (IllegalArgumentException ex) {
			throw new ParseException("Can't map JSON code to object: " +
					ex.getMessage(), ex);
		}
	}

	/**
	 * Converts the specified JSON object (the result of parsing JSON code to
	 * a basic Java type) to an object of the specified result type using the
	 * Jackson {@link ObjectMapper ObjectMapper}.
	 *
	 * @param json the JSON object
	 * @param typeRef the result type
	 * @param <T> the type of object to return
	 * @return the result object
	 * @throws ParseException if the JSON object can't be converted to the
	 * specified type
	 */
	public static <T> T convert(Object json, TypeReference<T> typeRef)
			throws ParseException {
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.convertValue(json, typeRef);
		} catch (IllegalArgumentException ex) {
			throw new ParseException("Can't map JSON code to object: " +
					ex.getMessage(), ex);
		}
	}

	/**
	 * Generates a JSON string from the specified object. If the object can't
	 * be converted to JSON, this method throws a {@link RuntimeException
	 * RuntimeException}.
	 *
	 * @param obj the object
	 * @return the JSON string
	 */
	public static String generate(Object obj) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.writeValueAsString(obj);
		} catch (JsonProcessingException ex) {
			throw new RuntimeException("Can't convert object to JSON: " +
					ex.getMessage(), ex);
		}
	}
}
