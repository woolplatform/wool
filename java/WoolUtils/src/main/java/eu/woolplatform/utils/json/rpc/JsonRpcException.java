package eu.woolplatform.utils.json.rpc;

/**
 * This exception is thrown when a JSON-RPC request results in a response with
 * an error.
 * 
 * @author Dennis Hofs
 */
public class JsonRpcException extends Exception {
	private static final long serialVersionUID = 1L;
	
	private int code;
	private Object data;

	/**
	 * Constructs a new JSON-RPC exception.
	 * 
	 * @param message the error message
	 * @param code the error code
	 */
	public JsonRpcException(String message, int code) {
		this(message, code, null);
	}
	
	/**
	 * Constructs a new JSON-RPC exception. The value of "data" can be
	 * converted to a JSON string.
	 * 
	 * @param message the error message
	 * @param code the error code
	 * @param data additional data about the error or null
	 */
	public JsonRpcException(String message, int code, Object data) {
		super(message);
		this.code = code;
		this.data = data;
	}
	
	/**
	 * Returns the error code.
	 * 
	 * @return the error code
	 */
	public int getCode() {
		return code;
	}
	
	/**
	 * Returns additional data about the error. This may be null. The data can
	 * be converted to a JSON string.
	 * 
	 * @return additional data about the error or null
	 */
	public Object getData() {
		return data;
	}
}
