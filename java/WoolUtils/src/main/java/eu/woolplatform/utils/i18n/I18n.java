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

package eu.woolplatform.utils.i18n;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.woolplatform.utils.AppComponents;
import eu.woolplatform.utils.io.ClassLoaderResourceLocator;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * This class defines a set of i18n message resources. An instance of this
 * class is normally obtained from {@link I18nLoader I18nLoader}. It uses
 * {@link I18nResourceFinder I18nResourceFinder} to find a matching file,
 * in .properties format, properties .xml format, or key-string pair .json
 * format. Note that a .properties file will be loaded as UTF-8.
 * 
 * <p>When you have an instance, you can call {@link #get(String) get()} to
 * get message strings.</p>
 * 
 * @author Dennis Hofs (RRD)
 */
public class I18n {
	private static final String LOGTAG = "I18n";
	
	private String baseName;
	private List<Locale> locales;
	private boolean honorifics;
	private Class<?> loadClass;

	private List<Map<String,String>> properties = new ArrayList<>();
	
	/**
	 * Constructs a new instance. It finds a matching resource (.properties or
	 * .xml) for the specified parameters and tries to load it. Note that a
	 * .properties file will be loaded as UTF-8. For more details see {@link
	 * I18nResourceFinder I18nResourceFinder}.
	 * 
	 * @param baseName the base name
	 * @param locales the preferred locales
	 * @param honorifics true if the resource should use honorifics, false
	 * otherwise
	 * @param loadClass the resource loading class or null
	 * @throws RuntimeException if no matching resource is found, or a matching
	 * resource can't be loaded
	 */
	public I18n(String baseName, List<Locale> locales, boolean honorifics,
			Class<?> loadClass) throws RuntimeException {
		this.baseName = baseName;
		this.locales = locales;
		this.honorifics = honorifics;
		this.loadClass = loadClass;
		loadMessages();
	}

	/**
	 * Returns the message string with the specified code.
	 * 
	 * @param code the message code
	 * @return the message string
	 */
	public String get(String code) {
		String s;
		for (Map<String,String> map : properties) {
			s = map.get(code);
			if (s != null)
				return s;
		}
		return code;
	}
	
	/**
	 * Tries to find a matching resource and then load the messages.
	 * 
	 * @throws RuntimeException if no matching resource is found, or a matching
	 * resource can't be loaded
	 */
	private void loadMessages() throws RuntimeException {
		Logger logger = AppComponents.getLogger(LOGTAG);
		properties = new ArrayList<>();
		I18nResourceFinder finder = new I18nResourceFinder(baseName);
		finder.setUserLocales(locales);
		finder.setHonorifics(honorifics);
		finder.setExtension("properties");
		finder.setResourceLocator(new ClassLoaderResourceLocator(loadClass));
		try {
			if (finder.find()) {
				properties = loadMessagesFromProperties(finder);
			}
			if (properties.isEmpty()) {
				finder.setExtension("xml");
				if (finder.find())
					properties = loadMessagesFromXml(finder);
			}
			if (properties.isEmpty()) {
				finder.setExtension("json");
				if (finder.find())
					properties = loadMessagesFromJson(finder);
			}
		} catch (IOException ex) {
			throw new RuntimeException("Can't read message resources from " +
					finder.getName() + ": " + ex.getMessage(), ex);
		}
		if (properties.isEmpty())
			throw new RuntimeException("No message resources found");
		logger.info("Loaded i18n messages from resource: " + finder.getName());
	}

	private List<Map<String,String>> loadMessagesFromProperties(
			I18nResourceFinder finder) throws IOException {
		List<Map<String,String>> result = new ArrayList<>();
		List<I18nResourceFinder.FoundResource> resources = finder.findList();
		for (I18nResourceFinder.FoundResource resource : resources) {
			result.add(loadMessagesFromProperties(finder, resource));
		}
		return result;
	}

	/**
	 * Loads a properties file in .properties format. The file is loaded as
	 * UTF-8.
	 * 
	 * @param finder the finder where the resource was found
	 * @param resource the found resource
	 * @return the properties
	 * @throws IOException if a reading error occurs
	 */
	private Map<String,String> loadMessagesFromProperties(
			I18nResourceFinder finder,
			I18nResourceFinder.FoundResource resource) throws IOException {
		Properties properties = new Properties();
		try (Reader reader = new InputStreamReader(finder.openStream(resource),
				StandardCharsets.UTF_8)) {
			properties.load(reader);
			HashMap<String,String> map = new HashMap<>();
			for (Object key : properties.keySet()) {
				String strKey = (String)key;
				map.put(strKey, properties.getProperty(strKey));
			}
			return map;
		}
	}

	private List<Map<String,String>> loadMessagesFromXml(
			I18nResourceFinder finder) throws IOException {
		List<Map<String,String>> result = new ArrayList<>();
		List<I18nResourceFinder.FoundResource> resources = finder.findList();
		for (I18nResourceFinder.FoundResource resource : resources) {
			result.add(loadMessagesFromXml(finder, resource));
		}
		return result;
	}

	/**
	 * Loads a properties file in .xml format.
	 * 
	 * @param finder the finder where the resource was found
	 * @param resource the found resource
	 * @return the properties
	 * @throws IOException if a reading error occurs
	 */
	private Map<String,String> loadMessagesFromXml(I18nResourceFinder finder,
			I18nResourceFinder.FoundResource resource) throws IOException {
		Properties properties = new Properties();
		try (InputStream input = finder.openStream(resource)) {
			properties.loadFromXML(input);
			HashMap<String,String> map = new HashMap<>();
			for (Object key : properties.keySet()) {
				String strKey = (String)key;
				map.put(strKey, properties.getProperty(strKey));
			}
			return map;
		}
	}

	private List<Map<String,String>> loadMessagesFromJson(
			I18nResourceFinder finder) throws IOException {
		List<Map<String,String>> result = new ArrayList<>();
		List<I18nResourceFinder.FoundResource> resources = finder.findList();
		for (I18nResourceFinder.FoundResource resource : resources) {
			result.add(loadMessagesFromJson(finder, resource));
		}
		return result;
	}

	private Map<String,String> loadMessagesFromJson(I18nResourceFinder finder,
			I18nResourceFinder.FoundResource resource) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		try (Reader reader = new InputStreamReader(finder.openStream(resource),
				StandardCharsets.UTF_8)) {
			return mapper.readValue(reader,
					new TypeReference<Map<String,String>>() {});
		}
	}
}
