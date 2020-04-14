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
