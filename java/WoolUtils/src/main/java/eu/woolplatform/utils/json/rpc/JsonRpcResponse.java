package eu.woolplatform.utils.json.rpc;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.woolplatform.utils.exception.ParseException;

/**
 * This class models a JSON-RPC Response.
 * 
 * @author Dennis Hofs
 */
public class JsonRpcResponse extends JsonRpcMessage {
	private Object result = null;
	private Error error = null;
	private Object id = null;
	
	/**
	 * Construct an instance through static methods.
	 */
	private JsonRpcResponse() {
	}
	
	/**
	 * Reads a JSON-RPC response from the specified map, which should represent
	 * a JSON object.
	 * 
	 * @param map the map
	 * @return the response
	 * @throws ParseException if the map is not a valid JSON-RPC response
	 * @see JsonRpcMessage#read(Map)
	 */
	public static JsonRpcResponse read(Map<?,?> map) throws ParseException {
		JsonRpcResponse response = new JsonRpcResponse();
		if (!map.containsKey("jsonrpc"))
			throw new ParseException("Member \"jsonrpc\" not found");
		Object jsonrpc = map.get("jsonrpc");
		if (!jsonrpc.equals("2.0")) {
			throw new ParseException(
					"Value of member \"jsonrpc\" is not \"2.0\": " + jsonrpc);
		}
		if (!map.containsKey("result") && !map.containsKey("error")) {
			throw new ParseException(
					"Member \"result\" or \"error\" not found");
		}
		if (map.containsKey("result") && map.containsKey("error")) {
			throw new ParseException(
					"Found both member \"result\" and \"error\"");
		}
		if (map.containsKey("result"))
			response.result = map.get("result");
		if (map.containsKey("error")) {
			Object error = map.get("error");
			if (!(error instanceof Map)) {
				throw new ParseException(
						"Invalid value for member \"error\": " + error);
			}
			response.error = Error.read((Map<?,?>)error);
		}
		if (!map.containsKey("id"))
			throw new ParseException("Member \"id\" not found");
		response.id = map.get("id");
		if (response.id != null && !(response.id instanceof Number) &&
				!(response.id instanceof String)) {
			throw new ParseException("Invalid value for member \"id\": " +
				response.id);
		}
		return response;
	}
	
	/**
	 * Creates a JSON-RPC response with a result.
	 * 
	 * @param result the result
	 * @param id the response ID (string, number or null)
	 * @return the response
	 */
	public static JsonRpcResponse createResult(Object result, Object id) {
		return create(result, null, id);
	}
	
	/**
	 * Creates a JSON-RPC response with an error.
	 * 
	 * @param error the error
	 * @param id the response ID (string, number or null)
	 * @return the response
	 */
	public static JsonRpcResponse createError(Error error, Object id) {
		return create(null, error, id);
	}
	
	/**
	 * Creates a new JSON-RPC response. You should specify either "result" or
	 * "error", but not both.
	 * 
	 * @param result the result or null
	 * @param error the error or null
	 * @param id the response ID (string, number or null)
	 * @return the response
	 */
	private static JsonRpcResponse create(Object result, Error error,
			Object id) {
		JsonRpcResponse response = new JsonRpcResponse();
		if (result == null && error == null) {
			throw new IllegalArgumentException(
					"Result or error must be specified");
		}
		if (result != null && error != null) {
			throw new IllegalArgumentException(
					"Both result and error specified");
		}
		response.result = result;
		response.error = error;
		if (id != null && !(id instanceof Number) && !(id instanceof String)) {
			throw new IllegalArgumentException("Invalid ID: " + id);
		}
		response.id = id;
		return response;
	}
	
	/**
	 * Returns the result. If this response doesn't contain a result, this
	 * method returns null and {@link #getError() getError()} should return an
	 * error.
	 * 
	 * @return the result or null
	 */
	public Object getResult() {
		return result;
	}
	
	/**
	 * Returns the error, if this response contains an error. Otherwise it
	 * returns null and {@link #getResult() getResult()} should return a
	 * result.
	 * 
	 * @return the error or null
	 */
	public Error getError() {
		return error;
	}
	
	/**
	 * Returns the request ID (string, number or null).
	 * 
	 * @return the request ID (string, number or null)
	 */
	public Object getID() {
		return id;
	}
	
	@Override
	public void write(Writer out) throws IOException {
		String json = toString();
		out.write(json);
		out.flush();
	}
	
	@Override
	public String toString() {
		Map<String,Object> map = new LinkedHashMap<String,Object>();
		map.put("jsonrpc", "2.0");
		if (result != null)
			map.put("result", result);
		else if (error != null)
			map.put("error", error.write());
		map.put("id", id);
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.writeValueAsString(map);
		} catch (JsonProcessingException ex) {
			throw new RuntimeException("Can't write JSON string: " +
					ex.getMessage(), ex);
		}
	}

	/**
	 * An error in a response message.
	 */
	public static class Error {
		private int code = 0;
		private String message = null;
		private Object data = null;
		
		/**
		 * Construct an instance through static methods.
		 */
		private Error() {
		}
		
		/**
		 * Reads an error from the specified map, which should represent a JSON
		 * object.
		 * 
		 * @param map the map
		 * @return the error
		 * @throws ParseException if the map is not a valid error
		 */
		public static Error read(Map<?,?> map) throws ParseException {
			Error error = new Error();
			if (!map.containsKey("code"))
				throw new ParseException("Member \"code\" not found");
			Object code = map.get("code");
			if (!(code instanceof Number)) {
				throw new ParseException("Invalid value for member \"code\": " +
						code);
			}
			error.code = ((Number)code).intValue();
			if (!map.containsKey("message"))
				throw new ParseException("Member \"message\" not found");
			Object message = map.get("message");
			if (!(message instanceof String)) {
				throw new ParseException(
						"Invalid value for member \"message\": " + message);
			}
			error.message = (String)message;
			error.data = map.get("data");
			return error;
		}
		
		/**
		 * Creates a new error.
		 * 
		 * @param code the error code
		 * @param message the error message
		 * @return the error
		 */
		public static Error create(int code, String message) {
			return create(code, message, null);
		}

		/**
		 * Creates a new error.
		 * 
		 * @param code the error code
		 * @param message the error message
		 * @param data additional data about the error or null
		 * @return the error
		 */
		public static Error create(int code, String message, Object data) {
			Error error = new Error();
			error.code = code;
			error.message = message;
			error.data = data;
			return error;
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
		 * Returns the error message.
		 * 
		 * @return the error message
		 */
		public String getMessage() {
			return message;
		}
		
		/**
		 * Returns additional data about the error. This may be null.
		 * 
		 * @return additional data about the error or null
		 */
		public Object getData() {
			return data;
		}
		
		/**
		 * Writes the error to a map that can be written as a JSON object
		 * string.
		 * 
		 * @return the map
		 */
		private Map<?,?> write() {
			Map<String,Object> map = new HashMap<String,Object>();
			map.put("code", code);
			map.put("message", message);
			if (data != null)
				map.put("data", data);
			return map;
		}
	}
}
