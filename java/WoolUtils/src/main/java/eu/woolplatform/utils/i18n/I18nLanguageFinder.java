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

import eu.woolplatform.utils.io.ResourceLocator;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

/**
 * This class can find a preferred language from a list of available languages.
 * After construction you can set properties with the set methods. Then call
 * {@link #find() find()}.
 *
 * <p><b>Example</b></p>
 *
 * <p>Find properties:</p>
 *
 * <p><ul>
 * <li>userLocales: ["nl_NL"]</li>
 * <li>honorifics: true</li>
 * </ul></p>
 *
 * <p>It will try the following keys, in this order:</p>
 *
 * <p><ul>
 * <li>"nl_NL_v" (if the locale contains a country; v = vos, t = tu)</li>
 * <li>"nl_NL" (if the locale contains a country)</li>
 * <li>"nl_v"</li>
 * <li>"nl"</li>
 * <li>"v"</li>
 * <li>""</li>
 * </ul></p>
 *
 * <p>If nothing is found, it will try the following locales: en_GB, en_US,
 * en.</p>
 *
 * @author Dennis Hofs (RRD)
 */
public class I18nLanguageFinder {
	private I18nResourceFinder finder;
	private List<String> available;

	public I18nLanguageFinder(List<String> available) {
		this.available = available;
		finder = new I18nResourceFinder("");
		finder.setExtension(null);
		finder.setResourceLocator(new StringMapLocator());
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
		finder.setUserLocale(userLocale);
	}

	/**
	 * Sets the preferred locales of the user. If no resource is found for
	 * these locales, the resource finder will try en_GB, en_US or en. The
	 * default is the locale of the system.
	 *
	 * @param userLocales the preferred locales of the user (at least one)
	 */
	public void setUserLocales(List<Locale> userLocales) {
		finder.setUserLocales(userLocales);
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
		finder.setHonorifics(honorifics);
	}

	/**
	 * Tries to find a preferred language matching the specified properties. If
	 * a language is found, this method will return the code. Otherwise it
	 * returns null.
	 *
	 * @return the string map
	 */
	public String find() {
		if (!finder.find())
			return null;
		return finder.getName();
	}

	private class StringMapLocator implements ResourceLocator {
		@Override
		public boolean resourceExists(String path) {
			return available.contains(path);
		}

		@Override
		public InputStream openResource(String path) throws IOException {
			return null;
		}
	}
}
