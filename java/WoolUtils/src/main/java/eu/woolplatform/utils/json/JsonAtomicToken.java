package eu.woolplatform.utils.json;

/**
 * Atomic token in a JSON string.
 * 
 * @author Dennis Hofs (RRD)
 */
public class JsonAtomicToken extends JsonObject {
	
	/**
	 * The possible token types.
	 */
	public enum Type {
		/**
		 * Start of an object: "{"<br />
		 * Value: null
		 */
		START_OBJECT,
		
		/**
		 * Separator between key and value in an object: ":"<br />
		 * Value: null
		 */
		OBJECT_KEY_VALUE_SEPARATOR,
		
		/**
		 * Separator between key/value pairs in an object: ","<br />
		 * Value: null
		 */
		OBJECT_PAIR_SEPARATOR,
		
		/**
		 * End of an object: "}"<br />
		 * Value: null
		 */
		END_OBJECT,
		
		/**
		 * Start of a list: "["<br />
		 * Value: null
		 */
		START_LIST,
		
		/**
		 * Separator between items in a list: ","<br />
		 * Value: null
		 */
		LIST_ITEM_SEPARATOR,
		
		/**
		 * End of a list: "]"<br />
		 * Value: null
		 */
		END_LIST,
		
		/**
		 * String value. This token is only returned if {@link
		 * JsonStreamReader#isStringAtomic() isStringAtomic()} returns
		 * true.<br />
		 * Value: {@link String String}
		 */
		STRING,
		
		/**
		 * Start of a string ("). This token is only returned if {@link
		 * JsonStreamReader#isStringAtomic() isStringAtomic()} returns
		 * false.<br />
		 * Value: null
		 */
		START_STRING,
		
		/**
		 * Character token in a string. This token is only returned if {@link
		 * JsonStreamReader#isStringAtomic() isStringAtomic()} returns false.
		 * The token can consist of one or more characters. It can be more than
		 * one character as the result of parsing a Unicode code point.<br />
		 * Value: {@link String String}
		 */
		STRING_CHARACTER,

		/**
		 * End of a string ("). This token is only returned if {@link
		 * JsonStreamReader#isStringAtomic() isStringAtomic()} returns
		 * false.<br />
		 * Value: null
		 */
		END_STRING,
		
		/**
		 * Number. A number is parsed into a {@link Double Double} if it
		 * contains a fraction or exponent. Otherwise it is parsed into an
		 * {@link Integer Integer} (if the value fits in the signed 32-bit
		 * integer range) or a {@link Long Long}.<br />
		 * Value: {@link Integer Integer}, {@link Long Long} or {@link Double
		 * Double}
		 */
		NUMBER,
		
		/**
		 * Boolean value<br />
		 * Value: {@link Boolean Boolean}
		 */
		BOOLEAN,
		
		/**
		 * Null value<br />
		 * Value: null
		 */
		NULL
	}
	
	private Type type;
	private Object value = null;
	
	/**
	 * Constructs a new instance that still needs to be initialized.
	 */
	public JsonAtomicToken() {
	}
	
	/**
	 * Constructs a new token with the specified type and value null.
	 * 
	 * @param type the type
	 */
	public JsonAtomicToken(Type type) {
		this.type = type;
	}
	
	/**
	 * Constructs a new token with the specified type and value. The value
	 * depends on the type.
	 * 
	 * @param type the type
	 * @param value the value
	 */
	public JsonAtomicToken(Type type, Object value) {
		this.type = type;
		this.value = value;
	}

	/**
	 * Returns the type.
	 * 
	 * @return the type
	 */
	public Type getType() {
		return type;
	}

	/**
	 * Sets the type.
	 * 
	 * @param type the type
	 */
	public void setType(Type type) {
		this.type = type;
	}

	/**
	 * Returns the value. The value depends on the type and can be null.
	 * 
	 * @return the value or null
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * Sets the value. The value depends on the type and can be null.
	 * 
	 * @param value the value or null
	 */
	public void setValue(Object value) {
		this.value = value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + type.hashCode();
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		JsonAtomicToken other = (JsonAtomicToken)obj;
		if (type != other.type)
			return false;
		if ((value == null) != (other.value == null))
			return false;
		if (value != null && !value.equals(other.value))
			return false;
		return true;
	}
}
