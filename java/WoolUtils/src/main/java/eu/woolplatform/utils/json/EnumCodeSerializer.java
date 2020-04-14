package eu.woolplatform.utils.json;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * This serializer calls method code() on an enum and writes it as an integer
 * value. The enum class must have a method int code().
 * 
 * @author Dennis Hofs (RRD)
 */
public class EnumCodeSerializer extends JsonSerializer<Enum<?>> {

	@Override
	public void serialize(Enum<?> value, JsonGenerator gen,
			SerializerProvider serializers) throws IOException,
			JsonProcessingException {
		int code = 0;
		Exception exception = null;
		try {
			Method method = value.getClass().getMethod("code");
			code = (Integer)method.invoke(value);
		} catch (NoSuchMethodException ex) {
			exception = ex;
		} catch (InvocationTargetException ex) {
			exception = ex;
		} catch (IllegalAccessException ex) {
			exception = ex;
		} catch (IllegalArgumentException ex) {
			exception = ex;
		}
		if (exception != null) {
			throw new RuntimeException("Can't invoke code(): " +
					exception.getMessage(), exception);
		}
		gen.writeNumber(code);
	}
}
