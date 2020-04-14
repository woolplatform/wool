package eu.woolplatform.utils.json;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * This serializer calls toString() on an enum and writes it as a string
 * value. It also supports null values.
 * 
 * @author Dennis Hofs (RRD)
 */
public class EnumCustomStringSerializer extends JsonSerializer<Enum<?>> {

	@Override
	public void serialize(Enum<?> value, JsonGenerator gen,
			SerializerProvider serializers) throws IOException,
			JsonProcessingException {
		gen.writeString(value == null ? null : value.toString());
	}
}
