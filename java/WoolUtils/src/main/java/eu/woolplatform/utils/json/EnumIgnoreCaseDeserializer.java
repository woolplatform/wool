package eu.woolplatform.utils.json;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import eu.woolplatform.utils.ReflectionUtils;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;

public class EnumIgnoreCaseDeserializer<T extends Enum<?>>
		extends JsonDeserializer<T> {
	private Class<T> enumClass;

	/**
	 * Constructs a new instance. The enum class must have a static method
	 * fromStringValue(String s).
	 *
	 * @param enumClass the enum class
	 */
	public EnumIgnoreCaseDeserializer(Class<T> enumClass) {
		this.enumClass = enumClass;
	}

	@Override
	public T deserialize(JsonParser p, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		if (!p.getCurrentToken().isScalarValue()) {
			throw new JsonParseException(p,
					"Expected string, found non-scalar value");
		}
		String s = p.getValueAsString();
		if (s == null)
			return null;
		try {
			Object array = ReflectionUtils.invokeStaticMethod(enumClass,
					Object.class, "values");
			for (int i = 0; i < Array.getLength(array); i++) {
				T item = enumClass.cast(Array.get(array, i));
				if (item.toString().equalsIgnoreCase(s))
					return item;
			}
			throw new JsonParseException(p, "Value not found: " + s);
		} catch (NoSuchMethodException | InvocationTargetException ex) {
			throw new RuntimeException("Can't invoke values(): " +
					ex.getMessage(), ex);
		}
	}
}
