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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.woolplatform.utils.AppComponents;
import eu.woolplatform.utils.exception.ParseException;

/**
 * This class provides JSON-RPC over a HTTP connection. It can only be used for
 * outgoing requests and notifications. This class was designed to make the
 * remote procedure calls look like normal method calls. You can send requests
 * with the <code>request()</code> methods. They wait for the response and
 * return it as soon as it's received. In case of an error, they throw an
 * exception. Notifications are like void methods. You can send notifications
 * with the <code>notify()</code> methods.
 * 
 * @author Dennis Hofs
 */
public class JsonRpcHttp {
	public static final String LOGTAG = "JsonRpc";
	
	private URL url;
	private final Object lock = new Object();
	private int nextID = 1;
	private boolean closed = false;
	private List<HttpURLConnection> connections = new ArrayList<>();
	
	/**
	 * Constructs a new instance.
	 * 
	 * @param url the URL (e.g. http://localhost:8080/jsonrpc)
	 */
	public JsonRpcHttp(URL url) {
		this.url = url;
	}

	/**
	 * Closes any open connections.
	 */
	public void close() {
		synchronized (lock) {
			if (closed)
				return;
			closed = true;
			while (!connections.isEmpty()) {
				HttpURLConnection conn = connections.remove(0);
				conn.disconnect();
			}
		}
	}
	
	/**
	 * Sends a request without parameters. The result object is obtained by
	 * parsing the JSON result string.
	 * 
	 * @param methodName the method name
	 * @return the result
	 * @throws IOException if an error occurs while sending or receiving
	 * @throws JsonRpcException if the remote method returned an error response
	 * @throws ParseException if the server response can't be parsed
	 */
	public Object request(String methodName) throws IOException,
	JsonRpcException, ParseException {
		int id;
		synchronized (lock) {
			id = nextID++;
		}
		JsonRpcRequest request = JsonRpcRequest.create(methodName, id);
		return sendRequest(request);
	}

	/**
	 * Sends a request with parameters as a JSON object. This method waits for
	 * the result and returns it. The result object is obtained by parsing the
	 * JSON result string.
	 * 
	 * @param methodName the method name
	 * @param params the parameters
	 * @return the result
	 * @throws IOException if an error occurs while sending or receiving
	 * @throws JsonRpcException if the remote method returned an error response
	 * @throws ParseException if the server response can't be parsed
	 */
	public Object request(String methodName, Map<String,?> params) throws
	IOException, JsonRpcException, ParseException {
		int id;
		synchronized (lock) {
			id = nextID++;
		}
		JsonRpcRequest request = JsonRpcRequest.create(methodName, params, id);
		return sendRequest(request);
	}

	/**
	 * Sends a request with parameters as a JSON array. This method waits for
	 * the result and returns it. The result object is obtained by parsing the
	 * JSON result string.
	 * 
	 * @param methodName the method name
	 * @param params the parameters
	 * @return the result
	 * @throws IOException if an error occurs while sending or receiving
	 * @throws JsonRpcException if the remote method returned an error response
	 * @throws ParseException if the server response can't be parsed
	 */
	public Object request(String methodName, Object... params) throws
	IOException, JsonRpcException, ParseException {
		int id;
		synchronized (lock) {
			id = nextID++;
		}
		List<Object> list = Arrays.asList(params);
		JsonRpcRequest request = JsonRpcRequest.create(methodName, list, id);
		return sendRequest(request);
	}
	
	/**
	 * Sends the specified JSON-RPC request and returns the response.
	 * 
	 * @param request the request
	 * @return the response
	 * @throws IOException if an error occurs while sending or receiving
	 * @throws JsonRpcException if the remote method returned an error response
	 * @throws ParseException if the server response can't be parsed
	 */
	private Object sendRequest(JsonRpcRequest request) throws IOException,
	JsonRpcException, ParseException {
		String responseStr = sendMessage(request);
		Logger logger = AppComponents.getLogger(LOGTAG);
		if (logger.isTraceEnabled()) {
			logger.trace("Received message: " + responseStr);
		}
		ObjectMapper mapper = new ObjectMapper();
		Map<?,?> map;
		try {
			map = mapper.readValue(responseStr, Map.class);
		} catch (JsonParseException ex) {
			throw new ParseException("Can't parse JSON string: " +
					ex.getMessage(), ex);
		} catch (JsonMappingException ex) {
			throw new ParseException("Can't convert JSON string to map: " +
					ex.getMessage(), ex);
		}
		JsonRpcMessage recvMsg = JsonRpcMessage.read(map);
		if (!(recvMsg instanceof JsonRpcResponse)) {
			throw new ParseException("Expected JSONRPCResponse, received " +
					recvMsg.getClass().getName());
		}
		JsonRpcResponse response = (JsonRpcResponse)recvMsg;
		if (response.getError() != null) {
			JsonRpcResponse.Error error = response.getError();
			throw new JsonRpcException(error.getMessage(), error.getCode(),
					error.getData());
		}
		return response.getResult();
	}
	
	/**
	 * Sends the specified JSON-RPC request or notification and returns the
	 * string response. For a request the result will be the JSON string for
	 * a JSON-RPC response. For a notification the result can be ignored.
	 * 
	 * @param message the request or notification
	 * @return the string response
	 * @throws IOException if an error occurs while sending or receiving
	 */
	private String sendMessage(JsonRpcMessage message) throws IOException {
		if (closed)
			throw new IOException("JsonRpcHttp closed");
		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		synchronized (lock) {
			if (closed) {
				conn.disconnect();
				throw new IOException("JsonRpcHttp closed");
			}
			this.connections.add(conn);
		}
		try {
			StringBuilder buf = new StringBuilder();
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestMethod("POST");
			conn.setDoOutput(true);
			OutputStream out = conn.getOutputStream();
			Writer writer = null;
			try {
				writer = new OutputStreamWriter(out, "UTF-8");
				message.write(writer);
				Logger logger = AppComponents.getLogger(LOGTAG);
				if (logger.isTraceEnabled()) {
					StringWriter strWriter = new StringWriter();
					message.write(strWriter);
					logger.trace("Sent message: " + strWriter.toString());
				}
			} finally {
				if (writer != null)
					writer.close();
				else
					out.close();
			}
			InputStream in = conn.getInputStream();
			Reader reader = new InputStreamReader(in, "UTF-8");
			char[] cs = new char[2048];
			int len;
			while ((len = reader.read(cs)) > 0) {
				buf.append(cs, 0, len);
			}
			return buf.toString();
		} finally {
			synchronized (lock) {
				if (!closed) {
					conn.disconnect();
					this.connections.remove(conn);
				}
			}
		}
	}
	
	/**
	 * Sends a notification without parameters.
	 * 
	 * @param methodName the method name
	 * @throws IOException if an error occurs while sending the notification
	 */
	public void notify(String methodName) throws IOException {
		sendMessage(JsonRpcNotification.create(methodName));
	}
	
	/**
	 * Sends a notification with parameters as a JSON object.
	 * 
	 * @param methodName the method name
	 * @param params the parameters
	 * @throws IOException if an error occurs while sending the notification
	 */
	public void notify(String methodName, Map<String,?> params)
	throws IOException {
		sendMessage(JsonRpcNotification.create(methodName, params));
	}
	
	/**
	 * Sends a notification with parameters as a JSON array.
	 * 
	 * @param methodName the method name
	 * @param params the parameters
	 * @throws IOException if an error occurs while sending the notification
	 */
	public void notify(String methodName, Object... params)
	throws IOException {
		List<Object> list = Arrays.asList(params);
		sendMessage(JsonRpcNotification.create(methodName, list));
	}
}
