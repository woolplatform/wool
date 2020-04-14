/*
 * Copyright 2019 Roessingh Research and Development.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

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
