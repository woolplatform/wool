package eu.woolplatform.utils;

public class ReferenceParameter<T> {
	private T value = null;

	public ReferenceParameter() {
	}

	public ReferenceParameter(T value) {
		this.value = value;
	}

	public T get() {
		return value;
	}

	public void set(T value) {
		this.value = value;
	}

	@Override
	public int hashCode() {
		return value == null ? 0 : value.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ReferenceParameter<?> other = (ReferenceParameter<?>)obj;
		if ((value == null) != (other.value == null))
			return false;
		if (value != null && !value.equals(other.value))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return value == null ? "null" : value.toString();
	}
}
