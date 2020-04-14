package eu.woolplatform.utils.json;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 * This deserializer can convert a string in ISO date/time format to a
 * timestamp in milliseconds.
 * 
 * @author Dennis Hofs (RRD)
 */
public class MillisFromIsoDateTimeDeserializer extends JsonDeserializer<Long> {
	@Override
	public Long deserialize(JsonParser jp, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		String val = jp.readValueAs(String.class);
		DateTimeFormatter parser = ISODateTimeFormat.dateTimeParser();
		try {
			return parser.parseMillis(val);
		} catch (IllegalArgumentException ex) {
			throw new JsonParseException(jp, "Invalid ISO date/time string: " +
					val + ": " + ex.getMessage(), jp.getTokenLocation(), ex);
		}
	}
}
