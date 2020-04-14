package eu.woolplatform.utils.json.rpc;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.woolplatform.utils.exception.ParseException;

/**
 * This class models a JSON-RPC Request.
 * 
 * @author Dennis Hofs
 */
public class JsonRpcRequest extends JsonRpcMessage {
	private String method = null;
	private Map<?,?> mapParams = null;
	private List<?> listParams = null;
	private Object id = null;
	
	/**
	 * Construct an instance through static methods.
	 */
	private JsonRpcRequest() {
	}
	
	/**
	 * Reads a JSON-RPC request from the specified map, which should represent
	 * a JSON object.
	 * 
	 * @param map the map
	 * @return the request
	 * @throws ParseException if the map is not a valid JSON-RPC request
	 * @see JsonRpcMessage#read(Map)
	 */
	public static JsonRpcRequest read(Map<?,?> map) throws ParseException {
		JsonRpcRequest request = new JsonRpcRequest();
		if (!map.containsKey("jsonrpc"))
			throw new ParseException("Member \"jsonrpc\" not found");
		Object jsonrpc = map.get("jsonrpc");
		if (!jsonrpc.equals("2.0")) {
			throw new ParseException(
					"Value of member \"jsonrpc\" is not \"2.0\": " + jsonrpc);
		}
		if (!map.containsKey("method"))
			throw new ParseException("Member \"method\" not found");
		Object method = map.get("method");
		if (!(method instanceof String)) {
			throw new ParseException("Invalid value for member \"method\": " +
					method);
		}
		request.method = (String)method;
		if (map.containsKey("params")) {
			Object params = map.get("params");
			if (params instanceof Map) {
				request.mapParams = (Map<?,?>)params;
			} else if (params instanceof List) {
				request.listParams = (List<?>)params;
			} else {
				throw new ParseException(
						"Invalid value for member \"params\": " + params);
			}
		}
		if (!map.containsKey("id"))
			throw new ParseException("Member \"id\" not found");
		request.id = map.get("id");
		if (request.id != null && !(request.id instanceof Number) &&
				!(request.id instanceof String)) {
			throw new ParseException("Invalid value for member \"id\": " +
				request.id);
		}
		return request;
	}
	
	/**
	 * Creates a new JSON-RPC request without parameters.
	 * 
	 * @param method the method name
	 * @param id the request ID (string, number or null)
	 * @return the request
	 */
	public static JsonRpcRequest create(String method, Object id) {
		return create(method, null, null, id);
	}

	/**
	 * Creates a new JSON-RPC request with parameters as a JSON object.
	 * 
	 * @param method the method name
	 * @param params the parameters
	 * @param id the request ID (string, number or null)
	 * @return the request
	 */
	public static JsonRpcRequest create(String method, Map<String,?> params,
			Object id) {
		return create(method, params, null, id);
	}

	/**
	 * Creates a new JSON-RPC request with parameters as a JSON array.
	 * 
	 * @param method the method name
	 * @param params the parameters
	 * @param id the request ID (string, number or null)
	 * @return the request
	 */
	public static JsonRpcRequest create(String method, List<?> params,
			Object id) {
		return create(method, null, params, id);
	}
	
	/**
	 * Creates a new JSON-RPC request. The parameters can be null. If you add
	 * parameters, specify either "mapParams" or "listParams", but not both.
	 * 
	 * @param method the method name
	 * @param mapParams the parameters as a JSON object or null
	 * @param listParams the parameters as a JSON array or null
	 * @param id the request ID (string, number or null)
	 * @return the request
	 */
	private static JsonRpcRequest create(String method,
			Map<String,?> mapParams, List<?> listParams, Object id) {
		JsonRpcRequest request = new JsonRpcRequest();
		if (method == null)
			throw new NullPointerException("Method is null");
		request.method = method;
		request.mapParams = mapParams;
		request.listParams = listParams;
		if (id != null && !(id instanceof Number) && !(id instanceof String)) {
			throw new IllegalArgumentException("Invalid ID: " + id);
		}
		request.id = id;
		return request;
	}
	
	/**
	 * Returns the method name.
	 * 
	 * @return the method name
	 */
	public String getMethod() {
		return method;
	}
	
	/**
	 * Returns the parameters, if the request contains parameters as a JSON
	 * object. If the request has no parameters, or it has parameters as a JSON
	 * array, this method returns null.
	 * 
	 * @return the parameters as a JSON object or null
	 * @see #getListParams()
	 */
	public Map<?,?> getMapParams() {
		return mapParams;
	}
	
	/**
	 * Returns the parameters, if the request contains parameters as a JSON
	 * array. If the request has no parameters, or it has parameters as a JSON
	 * object, this method returns null.
	 * 
	 * @return the parameters as a JSON array or null
	 * @see #getMapParams()
	 */
	public List<?> getListParams() {
		return listParams;
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
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("jsonrpc", "2.0");
		map.put("method", method);
		if (mapParams != null)
			map.put("params", mapParams);
		else if (listParams != null)
			map.put("params", listParams);
		map.put("id", id);
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.writeValueAsString(map);
		} catch (JsonProcessingException ex) {
			throw new RuntimeException("Can't write JSON string: " +
					ex.getMessage(), ex);
		}
	}
}
