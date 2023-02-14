/*
 * Copyright 2019-2022 WOOL Foundation - Licensed under the MIT License:
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package eu.woolplatform.web.service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serial;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Properties;

import nl.rrd.utils.AppComponent;

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

	@Serial
	private static final long serialVersionUID = 1L;

	// Known property keys
	public static final String VERSION = "version";
	public static final String BUILD_TIME = "buildTime";
	public static final String BASE_URL = "baseUrl";
	public static final String JWT_SECRET_KEY = "jwtSecretKey";
	public static final String DATA_DIR = "dataDir";
	public static final String EXTERNAL_VARIABLE_SERVICE_ENABLED = "externalVariableServiceEnabled";
	public static final String EXTERNAL_VARIABLE_SERVICE_URL = "externalVariableServiceUrl";
	public static final String EXTERNAL_VARIABLE_SERVICE_API_VERSION = "externalVariableServiceAPIVersion";
	public static final String EXTERNAL_VARIABLE_SERVICE_USERNAME = "externalVariableServiceUsername";
	public static final String EXTERNAL_VARIABLE_SERVICE_PASSWORD = "externalVariableServicePassword";
	public static final String AZURE_STORAGE_ACCOUNT_URL = "azureStorageAccountUrl";
	public static final String AZURE_SAS_TOKEN = "azureSASToken";
	public static final String AZURE_FILE_SYSTEM_NAME = "azureFileSystemName";
	public static final String AZURE_STORAGE_DIRECTORY = "azureStorageDirectory";
	public static final String AZURE_DATA_LAKE_ENABLED = "azureDataLakeEnabled";
	public static final String AZURE_DATA_LAKE_ACCOUNT_NAME = "azureDataLakeAccountName";
	public static final String AZURE_DATA_LAKE_ACCOUNT_KEY = "azureDataLakeAccountKey";

	// Hardcoded parameters
	public static final String DIRECTORY_NAME_LOGS = "logs";
	public static final String DIRECTORY_NAME_DIALOGUES = "dialogues";
	public static final String DIRECTORY_NAME_VARIABLES = "variables";

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
	 * Returns the location of the data directory used by the web service as a String.
	 * @return the location of the data directory used by the web service as a String.
	 */
	public String getDataDir() {
		if(get(DATA_DIR) == null) return "";
		else return get(DATA_DIR);
	}

	/**
	 * Returns whether an "External WOOL Variable Service" has been configured to be used.
	 * @return whether an "External WOOL Variable Service" has been configured to be used.
	 */
	public boolean getExternalVariableServiceEnabled() {
		return Boolean.parseBoolean(get(EXTERNAL_VARIABLE_SERVICE_ENABLED));
	}

	/**
	 * Returns the URL of the External Variable Service, or an empty string if incorrectly
	 * configured.
	 * @return the URL of the External Variable Service, or an empty string if incorrectly
	 *         configured.
	 */
	public String getExternalVariableServiceURL() {
		if(containsKey(EXTERNAL_VARIABLE_SERVICE_URL)) {
			String returnValue = get(EXTERNAL_VARIABLE_SERVICE_URL);
			if(returnValue != null) return returnValue;
		}
		return "";
	}

	/**
	 * Returns the API Version of the External Variable Service as a String, or an empty string if
	 * incorrectly configured.
	 * @return the API Version of the External Variable Service as a String, or an empty string if
	 *         incorrectly configured.
	 */
	public String getExternalVariableServiceAPIVersion() {
		if(containsKey(EXTERNAL_VARIABLE_SERVICE_API_VERSION)) {
			String returnValue = get(EXTERNAL_VARIABLE_SERVICE_API_VERSION);
			if(returnValue != null) return returnValue;
		}
		return "";
	}

	/**
	 * Returns the username for the External Variable Service as a String, or an empty string if
	 * incorrectly configured.
	 * @return the username for the External Variable Service as a String, or an empty string if
	 *         incorrectly configured.
	 */
	public String getExternalVariableServiceUsername() {
		if(containsKey(EXTERNAL_VARIABLE_SERVICE_USERNAME)) {
			String returnValue = get(EXTERNAL_VARIABLE_SERVICE_USERNAME);
			if(returnValue != null) return returnValue;
		}
		return "";
	}

	/**
	 * Returns the password for the External Variable Service as a String, or an empty string if
	 * incorrectly configured.
	 * @return the password for the External Variable Service as a String, or an empty string if
	 *         incorrectly configured.
	 */
	public String getExternalVariableServicePassword() {
		if(containsKey(EXTERNAL_VARIABLE_SERVICE_PASSWORD)) {
			String returnValue = get(EXTERNAL_VARIABLE_SERVICE_PASSWORD);
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

	/**
	 * Returns whether the Azure Data Lake is enabled.
	 * @return {@code true} if the Azure Data Lake is enabled, {@code false} otherwise.
	 */
	public boolean getAzureDataLakeEnabled() {
		return Boolean.parseBoolean(get(AZURE_DATA_LAKE_ENABLED));
	}

	/**
	 * Returns the Azure Storage Account URL, or an empty {@link String} if not configured.
	 * @return the Azure Storage Account URL, or an empty {@link String} if not configured.
	 */
	public String getAzureStorageAccountUrl() {
		if(containsKey(AZURE_STORAGE_ACCOUNT_URL)) {
			String returnValue = get(AZURE_STORAGE_ACCOUNT_URL);
			if(returnValue != null) return returnValue;
		}
		return "";
	}

	/**
	 * Returns the Azure SAS Token, or an empty {@link String} if not configured.
	 * @return the Azure SAS Token, or an empty {@link String} if not configured.
	 */
	public String getAzureSASToken() {
		if(containsKey(AZURE_SAS_TOKEN)) {
			String returnValue = get(AZURE_SAS_TOKEN);
			if(returnValue != null) return returnValue;
		}
		return "";
	}

	/**
	 * Returns the Azure File System Name, or an empty {@link String} if not configured.
	 * @return the Azure File System Name, or an empty {@link String} if not configured.
	 */
	public String getAzureFileSystemName() {
		if(containsKey(AZURE_FILE_SYSTEM_NAME)) {
			String returnValue = get(AZURE_FILE_SYSTEM_NAME);
			if(returnValue != null) return returnValue;
		}
		return "";
	}

	/**
	 * Returns the Azure Storage Directory, or an empty {@link String} if not configured.
	 * @return the Azure Storage Directory, or an empty {@link String} if not configured.
	 */
	public String getAzureStorageDirectory() {
		if(containsKey(AZURE_STORAGE_DIRECTORY)) {
			String returnValue = get(AZURE_STORAGE_DIRECTORY);
			if(returnValue != null) return returnValue;
		}
		return "";
	}

	/**
	 * Returns the Azure Data Lake Account Name, or an empty {@link String} if not configured.
	 * @return the Azure Data Lake Account Name, or an empty {@link String} if not configured.
	 */
	public String getAzureDataLakeAccountName() {
		if(containsKey(AZURE_DATA_LAKE_ACCOUNT_NAME)) {
			String returnValue = get(AZURE_DATA_LAKE_ACCOUNT_NAME);
			if(returnValue != null) return returnValue;
		}
		return "";
	}

	/**
	 * Returns the Azure Data Lake Account Key, or an empty {@link String} if not configured.
	 * @return the Azure Data Lake Account Key, or an empty {@link String} if not configured.
	 */
	public String getAzureDataLakeAccountKey() {
		if(containsKey(AZURE_DATA_LAKE_ACCOUNT_KEY)) {
			String returnValue = get(AZURE_DATA_LAKE_ACCOUNT_KEY);
			if(returnValue != null) return returnValue;
		}
		return "";
	}

	public String getDirectoryNameLogs() {
		return DIRECTORY_NAME_LOGS;
	}

	public String getDirectoryNameDialogues() {
		return DIRECTORY_NAME_DIALOGUES;
	}

	public String getDirectoryNameVariables() {
		return DIRECTORY_NAME_VARIABLES;
	}
}
