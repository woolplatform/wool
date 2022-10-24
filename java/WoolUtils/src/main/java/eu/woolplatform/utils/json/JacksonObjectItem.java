package eu.woolplatform.utils.json;

public class JacksonObjectItem<T> {
	private boolean ignore;
	private T value;

	private JacksonObjectItem(boolean ignore, T value) {
		this.ignore = ignore;
		this.value = value;
	}

	public static <T> JacksonObjectItem<T> createIgnore() {
		return new JacksonObjectItem<>(true, null);
	}

	public static <T> JacksonObjectItem<T> createValue(T value) {
		return new JacksonObjectItem<>(false, value);
	}

	public boolean isIgnore() {
		return ignore;
	}

	public T getValue() {
		return value;
	}
}
