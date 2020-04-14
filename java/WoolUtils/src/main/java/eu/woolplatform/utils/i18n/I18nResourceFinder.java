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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import eu.woolplatform.utils.io.ClassLoaderResourceLocator;
import eu.woolplatform.utils.io.ResourceLocator;

/**
 * This class can find i18n resources. After construction you can set
 * properties with the set methods. Then call {@link #find() find()}. If it
 * returns true, you can get information about the found resource with the
 * get methods.
 * 
 * <p><b>Example</b></p>
 * 
 * <p>Find properties:</p>
 * 
 * <p><ul>
 * <li>baseName: "messages"</li>
 * <li>userLocales: ["nl_NL"]</li>
 * <li>honorifics: true</li>
 * <li>extension: "properties"</li>
 * </ul></p>
 * 
 * <p>It will try the following resources, in this order:</p>
 * 
 * <p><ul>
 * <li>messages_nl_NL_v.properties (if the locale contains a country; v = vos,
 * t = tu)</li>
 * <li>messages_nl_NL.properties (if the locale contains a country)</li>
 * <li>messages_nl_v.properties</li>
 * <li>messages_nl.properties</li>
 * <li>messages_v.properties</li>
 * <li>messages.properties</li>
 * </ul></p>
 * 
 * <p>If nothing is found, it will try the following locales: en_GB, en_US,
 * en.</p>
 * 
 * @author Dennis Hofs (RRD)
 */
public class I18nResourceFinder {
	private String baseName;
	private List<Locale> userLocales = new ArrayList<>();
	private boolean honorifics = true;
	private String extension = "properties";

	private Locale locale = null;
	private String name = null;
	private ResourceLocator resourceLocator = new ClassLoaderResourceLocator();

	/**
	 * Constructs a new i18n resource finder.
	 * 
	 * @param baseName the base name of the resource file to find
	 */
	public I18nResourceFinder(String baseName) {
		this.baseName = baseName;
		this.userLocales.add(Locale.getDefault());
	}
	
	/**
	 * Sets the preferred locale of the user. If no resource is found for this
	 * locale, the resource finder will try en_GB, en_US or en. The default is
	 * the locale of the system.
	 * 
	 * @param userLocale the preferred locale of the user
	 * @see #setUserLocales(List)
	 */
	public void setUserLocale(Locale userLocale) {
		List<Locale> locales = new ArrayList<>();
		locales.add(userLocale);
		setUserLocales(locales);
	}
	
	/**
	 * Sets the preferred locales of the user. If no resource is found for
	 * these locales, the resource finder will try en_GB, en_US or en. The
	 * default is the locale of the system.
	 * 
	 * @param userLocales the preferred locales of the user (at least one)
	 */
	public void setUserLocales(List<Locale> userLocales) {
		this.userLocales = userLocales;
	}

	/**
	 * Sets whether the resource should use honorifics. This is true for vos
	 * (u, vous, Sie) in tu-vos distinction, and false for tu (jij, tu, du).
	 * For languages without honorifics, such as English, there will be no
	 * resources with tu-vos designation, so the value of this property is not
	 * relevant. The default is true.
	 * 
	 * @param honorifics true if the resource should use honorifics, false
	 * otherwise
	 */
	public void setHonorifics(boolean honorifics) {
		this.honorifics = honorifics;
	}

	/**
	 * Sets the extension of the resource file to find. You can set this to null
	 * if the file has no extension. The default is "properties".
	 * 
	 * @param extension the extension of the resource file to find or null
	 */
	public void setExtension(String extension) {
		this.extension = extension;
	}
	
	/**
	 * Returns the extension of the resource file to find. This can be null if
	 * the file has no extension. The default is "properties".
	 * 
	 * @return the extension of the resource file to find or null
	 */
	public String getExtension() {
		return extension;
	}

	/**
	 * Sets the resource locator that should be used to find the resource. The
	 * default is a {@link ClassLoaderResourceLocator
	 * ClassLoaderResourceLocator} that locates resources from the class loader.
	 * 
	 * @param resourceLocator the resource locator
	 */
	public void setResourceLocator(ResourceLocator resourceLocator) {
		this.resourceLocator = resourceLocator;
	}

	/**
	 * Tries to find a resource matching the specified properties. If a
	 * resource is found, this method will return true and you can get details
	 * with the get methods.
	 * 
	 * @return true if a resource was found, false otherwise
	 */
	public boolean find() {
		locale = null;
		name = null;
		List<Locale> prefLocales = new ArrayList<>(userLocales);
		prefLocales.add(Locale.UK);
		prefLocales.add(Locale.US);
		prefLocales.add(Locale.ENGLISH);
		for (Locale locale : prefLocales) {
			if (findResource(locale))
				return true;
		}
		return false;
	}
	
	/**
	 * You can call this method after {@link #find() find()} returned true. It
	 * returns the locale of the found resource. This may be different than the
	 * preferred locale, because the resource finder also tries en_GB, en_US
	 * and en.
	 * 
	 * @return the locale of the found resource
	 */
	public Locale getLocale() {
		return locale;
	}

	/**
	 * You can call this method after {@link #find() find()} returned true. It
	 * returns the name of the found resource.
	 * 
	 * @return the name of the found resource
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * You can call this method after {@link #find() find()} returned true. It
	 * opens the resource stream.
	 * 
	 * @return the input stream for the resource
	 * @throws IOException if the resource can't be opened
	 */
	public InputStream openStream() throws IOException {
		return resourceLocator.openResource(name);
	}
	
	/**
	 * Tries to find a resource matching the specified locale (and only that
	 * locale). If a resource is found, it will set the member variables
	 * "locale", "name" and "url" and it returns true.
	 * 
	 * @param locale the locale
	 * @return true if a matching resource was found, false otherwise
	 */
	private boolean findResource(Locale locale) {
		List<String> prefResources = new ArrayList<>();
		if (locale.getCountry().length() > 0) {
			addPrefResource(prefResources, String.format("%s_%s_%s",
					locale.getLanguage(), locale.getCountry(),
					honorifics ? "v" : "t"));
			addPrefResource(prefResources, String.format("%s_%s",
					locale.getLanguage(), locale.getCountry()));
		}
		addPrefResource(prefResources, String.format("%s_%s",
				locale.getLanguage(), honorifics ? "v" : "t"));
		addPrefResource(prefResources, locale.getLanguage());
		addPrefResource(prefResources, honorifics ? "v" : "t");
		addPrefResource(prefResources, null);
		for (String name : prefResources) {
			if (resourceLocator.resourceExists(name)) {
				this.locale = locale;
				this.name = name;
				return true;
			}
		}
		return false;
	}

	private void addPrefResource(List<String> prefResources, String tokens) {
		String name = baseName;
		if (name == null)
			name = "";
		if (name.length() > 0 && tokens != null && tokens.length() > 0)
			name += "_";
		if (tokens != null)
			name += tokens;
		if (extension != null)
			name += "." + extension;
		prefResources.add(name);
	}
}
