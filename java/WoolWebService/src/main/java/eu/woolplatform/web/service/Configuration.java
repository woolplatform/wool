/*
 * Copyright 2019-2022 WOOL Foundation.
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
package eu.woolplatform.web.service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Properties;

import eu.woolplatform.utils.AppComponent;

/**
 * Configuration of the WOOL Web Service. This is initialized from resources
 * service.properties and deployment.properties. Known property keys are defined
 * as constants in this class.
 * 
 * @author Dennis Hofs (RRD)
 * @author Tessa Beinema
 * @author Harm op den Akker
 */
@AppComponent
public class Configuration extends LinkedHashMap<String,String> {
	private static final long serialVersionUID = 1L;

	public static final String VERSION = "version";
	public static final String BUILD_TIME = "buildTime";
	public static final String BASE_URL = "baseUrl";
	public static final String JWT_SECRET_KEY = "jwtSecretKey";
	public static final String DATA_DIR = "dataDir";
	public static final String EXTERNAL_VARIABLE_SERVICE_ENABLED = "externalVariableServiceEnabled";
	public static final String EXTERNAL_VARIABLE_SERVICE_URL = "externalVariableServiceUrl";
	public static final String EXTERNAL_VARIABLE_SERVICE_API_VERSION = "externalVariableServiceAPIVersion";
	public static final String EXTERNAL_VARIABLE_SERVICE_API_TOKEN = "externalVariableServiceApiToken";

	private static final Object LOCK = new Object();
	private static Configuration instance = null;
	
	/**
	 * Returns the configuration. At startup of the service it should be
	 * initialized with {@link #loadProperties(URL) loadProperties()}.
	 * 
	 * @return the configuration
	 */
	public static Configuration getInstance() {
		synchronized (LOCK) {
			if (instance == null)
				instance = new Configuration();
			return instance;
		}
	}

	/**
	 * This private constructor is used in {@link #getInstance()
	 * getInstance()}.
	 */
	private Configuration() {
	}

	/**
	 * Loads the resource service.properties or deployment.properties into this
	 * configuration. This should only be called once at startup of the
	 * service.
	 * 
	 * @param url the URL of service.properties or deployment.properties
	 * @throws IOException if a reading error occurs
	 */
	public void loadProperties(URL url) throws IOException {
		Properties props = new Properties();
		try (Reader reader = new InputStreamReader(url.openStream(),
				StandardCharsets.UTF_8)) {
			props.load(reader);
		}
		for (String name : props.stringPropertyNames()) {
			put(name, props.getProperty(name));
		}
	}

	// ----- Getters:
	// -----
	// ----- Note that this Configuration is a LinkedHashMap, so all
	// ----- parameters can simply be retrieved by using this.get("parameterName")
	// ----- however, using the getters below we can add some robustness (e.g. null
	// ----- checking).

	/**
	 * Returns whether an "External WOOL Variable Service" has been configured to be used.
	 * @return whether an "External WOOL Variable Service" has been configured to be used.
	 */
	public boolean getExternalVariableServiceEnabled() {
		if(get(EXTERNAL_VARIABLE_SERVICE_ENABLED) == null) return false;
		else return Boolean.parseBoolean(get(EXTERNAL_VARIABLE_SERVICE_ENABLED));
	}

	/**
	 * Returns the URL of the External Variable Service, or an empty string if incorrectly configured.
	 * @return the URL of the External Variable Service, or an empty string if incorrectly configured.
	 */
	public String getExternalVariableServiceURL() {
		if(containsKey(EXTERNAL_VARIABLE_SERVICE_URL)) {
			String returnValue = get(EXTERNAL_VARIABLE_SERVICE_URL);
			if(returnValue != null) return returnValue;
		}
		return "";
	}

	/**
	 * Returns the API Version of the External Variable Service as a String, or an empty string if incorrectly configured.
	 * @return the API Version of the External Variable Service as a String, or an empty string if incorrectly configured.
	 */
	public String getExternalVariableServiceAPIVersion() {
		if(containsKey(EXTERNAL_VARIABLE_SERVICE_API_VERSION)) {
			String returnValue = get(EXTERNAL_VARIABLE_SERVICE_API_VERSION);
			if(returnValue != null) return returnValue;
		}
		return "";
	}

	/**
	 * Returns a date-time {@link String} representing the date and time that this version
	 * of the deployed web service was built.
	 * @return the build-time as a date-time {@link String}.
	 */
	public String getBuildTime() {
		if(containsKey(BUILD_TIME)) {
			String returnValue = get(BUILD_TIME);
			if(returnValue != null) return returnValue;
		}
		return "";
	}
}
