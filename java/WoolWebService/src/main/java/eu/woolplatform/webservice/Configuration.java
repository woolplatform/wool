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
 */
@AppComponent
public class Configuration extends LinkedHashMap<String,String> {
	private static final long serialVersionUID = 1L;

	public static final String VERSION = "version";
	public static final String BASE_URL = "baseUrl";
	public static final String DATA_DIR = "dataDir";

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
}
