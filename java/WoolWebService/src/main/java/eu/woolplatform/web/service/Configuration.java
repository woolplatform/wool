/*
 * Copyright 2019-2023 WOOL Foundation - Licensed under the MIT License:
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
 * Configuration of the WOOL Web Service. This is initialized from resources service.properties and
 * deployment.properties. Known property keys are defined as constants in this class.
 *
 * @author Harm op den Akker
 * @author Dennis Hofs
 * @author Tessa Beinema
 */
@AppComponent
public class Configuration extends LinkedHashMap<String,String> {

	@Serial
	private static final long serialVersionUID = 1L;

	// ----------------------------------------- //
	// ---------- Known property keys ---------- //
	// ----------------------------------------- //

	// ----- General

	public static final String VERSION = "version";
	public static final String BUILD_TIME = "buildTime";
	public static final String BASE_URL = "baseUrl";
	public static final String JWT_SECRET_KEY = "jwtSecretKey";
	public static final String DATA_DIR = "dataDir";

	// ----- External Variable Service

	public static final String EXTERNAL_VARIABLE_SERVICE_ENABLED = "externalVariableServiceEnabled";
	public static final String EXTERNAL_VARIABLE_SERVICE_URL = "externalVariableServiceUrl";
	public static final String EXTERNAL_VARIABLE_SERVICE_API_VERSION = "externalVariableServiceAPIVersion";
	public static final String EXTERNAL_VARIABLE_SERVICE_USERNAME = "externalVariableServiceUsername";
	public static final String EXTERNAL_VARIABLE_SERVICE_PASSWORD = "externalVariableServicePassword";

	// ----- Azure Data Lake

	public static final String AZURE_DATA_LAKE_ENABLED = "azureDataLakeEnabled";
	public static final String AZURE_DATA_LAKE_AUTHENTICATION_METHOD = "azureDataLakeAuthenticationMethod";
	public static final String AZURE_DATA_LAKE_ACCOUNT_NAME = "azureDataLakeAccountName";
	public static final String AZURE_DATA_LAKE_ACCOUNT_KEY = "azureDataLakeAccountKey";
	public static final String AZURE_DATA_LAKE_SAS_ACCOUNT_URL = "azureDataLakeSASAccountUrl";
	public static final String AZURE_DATA_LAKE_SAS_TOKEN = "azureDataLakeSASToken";
	public static final String AZURE_DATA_LAKE_FILE_SYSTEM_NAME = "azureDataLakeFileSystemName";

	// ------------------------------------------ //
	// ---------- Hardcoded parameters ---------- //
	// ------------------------------------------ //

	public static final String DIRECTORY_NAME_APPLICATION_LOGS = "logs";
	public static final String DIRECTORY_NAME_DIALOGUES = "dialogues";
	public static final String DIRECTORY_NAME_VARIABLES = "variables";

	private static final Object LOCK = new Object();
	private static Configuration instance = null;

	// --------------------------------------------------- //
	// ---------- Constructors / Initialisation ---------- //
	// --------------------------------------------------- //

	/**
	 * Returns the configuration. At startup of the service it should be initialized with
	 * {@link #loadProperties(URL) loadProperties()}.
	 * 
	 * @return the configuration.
	 */
	public static Configuration getInstance() {
		synchronized (LOCK) {
			if (instance == null)
				instance = new Configuration();
			return instance;
		}
	}

	/**
	 * This private constructor is used in {@link #getInstance() getInstance()}.
	 */
	private Configuration() { }

	/**
	 * Loads the resource service.properties or deployment.properties into this configuration. This
	 * should only be called once at startup of the service.
	 * 
	 * @param url the {@link URL} of a service.properties or deployment.properties file.
	 * @throws IOException if a reading error occurs.
	 */
	public void loadProperties(URL url) throws IOException {
		Properties props = new Properties();
		try (Reader reader = new InputStreamReader(url.openStream(), StandardCharsets.UTF_8)) {
			props.load(reader);
		}
		for (String name : props.stringPropertyNames()) {
			put(name, props.getProperty(name));
		}
	}

	// ----- Note that this Configuration is a LinkedHashMap, so all parameters can simply be
	// ----- retrieved by using this.get("parameterName") however, using the getters below we can
	// ----- add some robustness (e.g. null checking).

	// -------------------------------------- //
	// ---------- Getters: General ---------- //
	// -------------------------------------- //

	/**
	 * Returns the application version identifier (e.g. "1.0.0") as a String.
	 * @return the application version identifier (e.g. "1.0.0") as a String.
	 */
	public String getVersion() {
		if(get(VERSION) == null) return "";
		else return get(VERSION);
	}

	/**
	 * Returns a date-time {@link String} representing the date and time that this version of the
	 * deployed web service was built.
	 * @return the build-time as a date-time {@link String}.
	 */
	public String getBuildTime() {
		if(get(BUILD_TIME) == null) return "";
		else return get(BUILD_TIME);
	}

	/**
	 * Returns the configured Base URL for the WOOL Web Service.
	 * @return the configured Base URL for the WOOL Web Service.
	 */
	public String getBaseUrl() {
		if(get(BASE_URL) == null) return "";
		else return get(BASE_URL);
	}

	/**
	 * Returns the secret key used for encoding/decoding the JSON Web Tokens.
	 * @return the secret key used for encoding/decoding the JSON Web Tokens.
	 */
	public String getJwtSecretKey() {
		if(get(JWT_SECRET_KEY) == null) return "";
		else return get(JWT_SECRET_KEY);
	}

	/**
	 * Returns the location of the data directory used by the web service as a String.
	 * @return the location of the data directory used by the web service as a String.
	 */
	public String getDataDir() {
		if(get(DATA_DIR) == null) return "";
		else return get(DATA_DIR);
	}

	// --------------------------------------------------------
	// ---------- Getters: External Variable Service ----------
	// --------------------------------------------------------

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
		if(get(EXTERNAL_VARIABLE_SERVICE_URL) == null) return "";
		else return get(EXTERNAL_VARIABLE_SERVICE_URL);
	}

	/**
	 * Returns the API Version of the External Variable Service as a String, or an empty string if
	 * incorrectly configured.
	 * @return the API Version of the External Variable Service as a String, or an empty string if
	 *         incorrectly configured.
	 */
	public String getExternalVariableServiceAPIVersion() {
		if(get(EXTERNAL_VARIABLE_SERVICE_API_VERSION) == null) return "";
		else return get(EXTERNAL_VARIABLE_SERVICE_API_VERSION);
	}

	/**
	 * Returns the username for the External Variable Service as a String, or an empty string if
	 * incorrectly configured.
	 * @return the username for the External Variable Service as a String, or an empty string if
	 *         incorrectly configured.
	 */
	public String getExternalVariableServiceUsername() {
		if(get(EXTERNAL_VARIABLE_SERVICE_USERNAME) == null) return "";
		else return get(EXTERNAL_VARIABLE_SERVICE_USERNAME);
	}

	/**
	 * Returns the password for the External Variable Service as a String, or an empty string if
	 * incorrectly configured.
	 * @return the password for the External Variable Service as a String, or an empty string if
	 *         incorrectly configured.
	 */
	public String getExternalVariableServicePassword() {
		if(get(EXTERNAL_VARIABLE_SERVICE_PASSWORD) == null) return "";
		else return get(EXTERNAL_VARIABLE_SERVICE_PASSWORD);
	}

	// ---------------------------------------------- //
	// ---------- Getters: Azure Data Lake ---------- //
	// ---------------------------------------------- //

	/**
	 * Returns whether the Azure Data Lake is enabled.
	 * @return {@code true} if the Azure Data Lake is enabled, {@code false} otherwise.
	 */
	public boolean getAzureDataLakeEnabled() {
		return Boolean.parseBoolean(get(AZURE_DATA_LAKE_ENABLED));
	}

	/**
	 * Returns the authentication method that should be used for connecting to the Azure Data Lake
	 * as either "sas-token" or "account-key", or returns the empty string if not configured.
	 * @return the authentication method that should be used for connecting to the Azure Data Lake.
	 */
	public String getAzureDataLakeAuthenticationMethod() {
		if(get(AZURE_DATA_LAKE_AUTHENTICATION_METHOD) == null) return "";
		else return get(AZURE_DATA_LAKE_AUTHENTICATION_METHOD);
	}

	/**
	 * Returns the Azure Data Lake Account Name, or an empty {@link String} if not configured.
	 * @return the Azure Data Lake Account Name, or an empty {@link String} if not configured.
	 */
	public String getAzureDataLakeAccountName() {
		if(get(AZURE_DATA_LAKE_ACCOUNT_NAME) == null) return "";
		else return get(AZURE_DATA_LAKE_ACCOUNT_NAME);
	}

	/**
	 * Returns the Azure Data Lake Account Key, or an empty {@link String} if not configured.
	 * @return the Azure Data Lake Account Key, or an empty {@link String} if not configured.
	 */
	public String getAzureDataLakeAccountKey() {
		if(get(AZURE_DATA_LAKE_ACCOUNT_KEY) == null) return "";
		else return get(AZURE_DATA_LAKE_ACCOUNT_KEY);
	}

	/**
	 * Returns the Azure Storage Account URL used when authenticating with the Azure Data Lake using
	 * an SAS token, or an empty {@link String} if not configured.
	 * @return the Azure Storage Account URL, or an empty {@link String} if not configured.
	 */
	public String getAzureDataLakeSASAccountUrl() {
		if(get(AZURE_DATA_LAKE_SAS_ACCOUNT_URL) == null) return "";
		else return get(AZURE_DATA_LAKE_SAS_ACCOUNT_URL);
	}

	/**
	 * Returns the Azure SAS Token, or an empty {@link String} if not configured.
	 * @return the Azure SAS Token, or an empty {@link String} if not configured.
	 */
	public String getAzureDataLakeSASToken() {
		if(get(AZURE_DATA_LAKE_SAS_TOKEN) == null) return "";
		else return get(AZURE_DATA_LAKE_SAS_TOKEN);
	}

	/**
	 * Returns the Azure File System Name, or an empty {@link String} if not configured.
	 * @return the Azure File System Name, or an empty {@link String} if not configured.
	 */
	public String getAzureDataLakeFileSystemName() {
		if(get(AZURE_DATA_LAKE_FILE_SYSTEM_NAME) == null) return "";
		else return get(AZURE_DATA_LAKE_FILE_SYSTEM_NAME);
	}

	// --------------------------------------------------- //
	// ---------- Getters: Hardcoded parameters ---------- //
	// --------------------------------------------------- //

	/**
	 * Returns the name of the folder used for storing the application logs.
	 * @return the name of the folder used for storing the application logs.
	 */
	public String getDirectoryNameApplicationLogs() {
		return DIRECTORY_NAME_APPLICATION_LOGS;
	}

	/**
	 * Returns the name of the folder used for storing dialogue logs.
	 * @return the name of the folder used for storing dialogue logs.
	 */
	public String getDirectoryNameDialogues() {
		return DIRECTORY_NAME_DIALOGUES;
	}

	/**
	 * Returns the name of the folder used for storing wool variable stores.
	 * @return the name of the folder used for storing wool variable stores.
	 */
	public String getDirectoryNameVariables() {
		return DIRECTORY_NAME_VARIABLES;
	}
}
