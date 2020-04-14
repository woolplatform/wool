package eu.woolplatform.utils.json;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

import org.joda.time.LocalTime;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * This deserializer can convert a string in format HH:mm:ss to a {@link
 * LocalTime LocalTime}.
 * 
 * @author Dennis Hofs (RRD)
 */
public class SqlTimeDeserializer
extends JsonDeserializer<LocalTime> {
	@Override
	public LocalTime deserialize(JsonParser jp, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		String val = jp.readValueAs(String.class);
		DateTimeFormatter parser = DateTimeFormat.forPattern("HH:mm:ss");
		try {
			return parser.parseLocalTime(val);
		} catch (IllegalArgumentException ex) {
			throw new JsonParseException(jp, "Invalid time string: " + val +
					": " + ex.getMessage(), jp.getTokenLocation(), ex);
		}
	}
}
