package eu.woolplatform.utils.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class EnumLowercaseSerializer extends JsonSerializer<Enum<?>> {
	@Override
	public void serialize(Enum<?> value, JsonGenerator gen,
			SerializerProvider serializers) throws IOException {
		gen.writeString(value == null ? null : value.toString().toLowerCase());
	}
}
