package eu.woolplatform.webservice;

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
	public static final String BUILD = "build";
	public static final String BASE_URL = "baseUrl";
	public static final String JWT_SECRET_KEY = "jwtSecretKey";
	public static final String DATA_DIR = "dataDir";
	public static final String EXTERNAL_VARIABLE_SERVICE_ENABLED = "externalVariableServiceEnabled";
	public static final String EXTERNAL_VARIABLE_SERVICE_URL = "externalVariableServiceUrl";
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
}
