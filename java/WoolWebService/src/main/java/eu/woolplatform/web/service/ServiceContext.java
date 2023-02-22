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

import nl.rrd.utils.AppComponents;
import nl.rrd.utils.exception.ParseException;
import nl.rrd.utils.http.HttpURL;

public class ServiceContext {

	/**
	 * Returns the base URL.
	 * @return the base URL
	 */
	public static String getBaseUrl() {
		Configuration config = AppComponents.get(Configuration.class);
		return config.get(Configuration.BASE_URL);
	}
	
	/**
	 * Returns the base path.
	 * @return the base path
	 */
	public static String getBasePath() {
		String url = getBaseUrl();
		HttpURL httpUrl;
		try {
			httpUrl = HttpURL.parse(url);
		} catch (ParseException ex) {
			throw new RuntimeException("Invalid base URL: " + url + ": " + ex.getMessage(), ex);
		}
		return httpUrl.getPath();
	}

	/**
	 * Returns the current protocol version.
	 * @return the current protocol version
	 */
	public static String getCurrentVersion() {
		ProtocolVersion[] versions = ProtocolVersion.values();
		return versions[versions.length - 1].versionName();
	}

}
