package eu.woolplatform.utils.json.rpc;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

import eu.woolplatform.utils.exception.ParseException;

/**
 * <p>This is the base class for a JSON-RPC 2.0 message. There are three
 * subclasses:</p>
 * 
 * <ul>
 * <li>{@link JsonRpcRequest JSONRPCRequest}</li>
 * <li>{@link JsonRpcNotification JSONRPCNotification}</li>
 * <li>{@link JsonRpcResponse JSONRPCResponse}</li>
 * </ul>
 * 
 * @author Dennis Hofs
 */
public abstract class JsonRpcMessage {
	/**
	 * Writes this message as a JSON string to the specified writer.
	 * 
	 * @param out the writer
	 * @throws IOException if a writing error occurs
	 */
	public abstract void write(Writer out) throws IOException;

	/**
	 * Reads a JSON-RPC 2.0 message from the specified map, which should
	 * represent a JSON object.
	 * 
	 * @param map the map
	 * @return the message
	 * @throws ParseException if the map is not a valid JSON-RPC message
	 */
	public static JsonRpcMessage read(Map<?,?> map) throws ParseException {
		if (!map.containsKey("jsonrpc"))
			throw new ParseException("Member \"jsonrpc\" not found");
		if (map.containsKey("method") && map.containsKey("id")) {
			return JsonRpcRequest.read(map);
		} else if (map.containsKey("method")) {
			return JsonRpcNotification.read(map);
		} else if ((map.containsKey("result") || map.containsKey("error")) &&
				map.containsKey("id")) {
			return JsonRpcResponse.read(map);
		} else {
			throw new ParseException("Invalid JSON-RPC message: " + map);
		}
	}
	
	@Override
	public String toString() {
		StringWriter writer = new StringWriter();
		try {
			write(writer);
			return writer.toString();
		} catch (IOException ex) {
			return super.toString();
		}
	}
}
