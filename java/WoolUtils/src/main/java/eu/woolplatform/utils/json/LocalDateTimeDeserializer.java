package eu.woolplatform.utils.json;

import java.io.IOException;

import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

/**
 * This deserializer can convert a string in format yyyy-MM-dd'T'HH:mm:ss.SSS to
 * a {@link LocalDateTime LocalDateTime}.
 * 
 * @author Dennis Hofs (RRD)
 */
public class LocalDateTimeDeserializer
extends JsonDeserializer<LocalDateTime> {
	@Override
	public LocalDateTime deserialize(JsonParser jp, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		String val = jp.readValueAs(String.class);
		DateTimeFormatter parser = ISODateTimeFormat.dateTimeParser();
		try {
			return parser.parseLocalDateTime(val);
		} catch (IllegalArgumentException ex) {
			throw new JsonParseException(jp, "Invalid date/time string: " +
					val + ": " + ex.getMessage(), jp.getTokenLocation(), ex);
		}
	}
}
