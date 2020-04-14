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
 * This class models a JSON-RPC Notification.
 * 
 * @author Dennis Hofs
 */
public class JsonRpcNotification extends JsonRpcMessage {
	private String method = null;
	private Map<?,?> mapParams = null;
	private List<?> listParams = null;
	
	/**
	 * Construct an instance through static methods.
	 */
	private JsonRpcNotification() {
	}
	
	/**
	 * Reads a JSON-RPC notification from the specified map, which should
	 * represent a JSON object.
	 * 
	 * @param map the map
	 * @return the notification
	 * @throws ParseException if the map is not a valid JSON-RPC notification
	 * @see JsonRpcMessage#read(Map)
	 */
	public static JsonRpcNotification read(Map<?,?> map) throws ParseException {
		JsonRpcNotification notification = new JsonRpcNotification();
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
		notification.method = (String)method;
		if (map.containsKey("params")) {
			Object params = map.get("params");
			if (params instanceof Map) {
				notification.mapParams = (Map<?,?>)params;
			} else if (params instanceof List) {
				notification.listParams = (List<?>)params;
			} else {
				throw new ParseException(
						"Invalid value for member \"params\": " + params);
			}
		}
		return notification;
	}
	
	/**
	 * Creates a new JSON-RPC notification without parameters.
	 * 
	 * @param method the method name
	 * @return the notification
	 */
	public static JsonRpcNotification create(String method) {
		return create(method, null, null);
	}

	/**
	 * Creates a new JSON-RPC notification with parameters as a JSON object.
	 * 
	 * @param method the method name
	 * @param params the parameters
	 * @return the notification
	 */
	public static JsonRpcNotification create(String method, Map<String,?> params) {
		return create(method, params, null);
	}

	/**
	 * Creates a new JSON-RPC notification with parameters as a JSON array.
	 * 
	 * @param method the method name
	 * @param params the parameters
	 * @return the notification
	 */
	public static JsonRpcNotification create(String method, List<?> params) {
		return create(method, null, params);
	}
	
	/**
	 * Creates a new JSON-RPC notification. The parameters can be null. If you
	 * add parameters, specify either "mapParams" or "listParams", but not
	 * both.
	 * 
	 * @param method the method name
	 * @param mapParams the parameters as a JSON object or null
	 * @param listParams the parameters as a JSON array or null
	 * @return the notification
	 */
	private static JsonRpcNotification create(String method,
			Map<String,?> mapParams, List<?> listParams) {
		JsonRpcNotification notification = new JsonRpcNotification();
		if (method == null)
			throw new NullPointerException("Method is null");
		notification.method = method;
		notification.mapParams = mapParams;
		notification.listParams = listParams;
		return notification;
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
	 * Returns the parameters, if the notification contains parameters as a
	 * JSON object. If the notification has no parameters, or it has parameters
	 * as a JSON array, this method returns null.
	 * 
	 * @return the parameters as a JSON object or null
	 * @see #getListParams()
	 */
	public Map<?,?> getMapParams() {
		return mapParams;
	}
	
	/**
	 * Returns the parameters, if the notification contains parameters as a
	 * JSON array. If the notification has no parameters, or it has parameters
	 * as a JSON object, this method returns null.
	 * 
	 * @return the parameters as a JSON array or null
	 * @see #getMapParams()
	 */
	public List<?> getListParams() {
		return listParams;
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
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.writeValueAsString(map);
		} catch (JsonProcessingException ex) {
			throw new RuntimeException("Can't write JSON string: " +
					ex.getMessage(), ex);
		}
	}
}
