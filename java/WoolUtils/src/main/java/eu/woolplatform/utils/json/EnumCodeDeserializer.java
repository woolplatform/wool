package eu.woolplatform.utils.json;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

/**
 * This deserializer can read an integer value and convert it to an enum using
 * a static method T forCode().
 * 
 * @author Dennis Hofs (RRD)
 *
 * @param <T> the enum type
 */
public class EnumCodeDeserializer<T extends Enum<?>> extends
JsonDeserializer<T> {
	private Class<T> enumClass;
	
	/**
	 * Constructs a new instance. The enum class must have a static method
	 * forCode(int code).
	 * 
	 * @param enumClass the enum class
	 */
	public EnumCodeDeserializer(Class<T> enumClass) {
		this.enumClass = enumClass;
	}
	
	@Override
	public T deserialize(JsonParser p, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		int code = p.getIntValue();
		Exception exception = null;
		T result = null;
		try {
			Method method = enumClass.getMethod("forCode", Integer.TYPE);
			Object resultObj = method.invoke(null, code);
			result = enumClass.cast(resultObj);
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
			throw new RuntimeException("Can't invoke forCode(): " +
					exception.getMessage(), exception);
		}
		return result;
	}
}
