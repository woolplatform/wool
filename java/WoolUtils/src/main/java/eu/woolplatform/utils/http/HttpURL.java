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

package eu.woolplatform.utils.http;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu.woolplatform.utils.exception.ParseException;

public class HttpURL {
	private String protocol;
	private String host;
	private Integer port = null;
	private String path;
	private Map<String,String> params = new LinkedHashMap<>();
	
	public static HttpURL parse(String url) throws ParseException {
		HttpURL result = new HttpURL();
		String hostPartRegex = "([a-zA-Z0-9])|([a-zA-Z0-9][a-zA-Z0-9\\-]*[a-zA-Z0-9])";
		String hostRegex = "(" + hostPartRegex + "\\.)*" + hostPartRegex;
		String pathRegex = "(/[^?]*)?";
		Pattern regex = Pattern.compile("^(https?)://(" + hostRegex +
				")(:([0-9]+))?(" + pathRegex + ")(\\?.*)?$");
		Matcher m = regex.matcher(url);
		if (!m.matches())
			throw new ParseException("Invalid HTTP(S) URL: " + url);
		result.protocol = m.group(1);
		result.host = m.group(2);
		String portStr = m.group(9);
		if (portStr != null) {
			try {
				result.port = Integer.parseInt(portStr);
			} catch (NumberFormatException ex) {
				throw new ParseException("Invalid HTTP(S) URL: " + url);
			}
		}
		result.path = m.group(10);
		Map<String,String> params = URLParameters.extractParameters(url);
		if (params != null)
			result.params = params;
		return result;
	}

	public String getProtocol() {
		return protocol;
	}

	public String getHost() {
		return host;
	}

	public Integer getPort() {
		return port;
	}

	public String getPath() {
		return path;
	}

	public Map<String, String> getParams() {
		return params;
	}
}
