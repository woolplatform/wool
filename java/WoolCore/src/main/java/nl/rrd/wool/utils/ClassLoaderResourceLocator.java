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

package nl.rrd.wool.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * This resource locator can locate resources using the class loader or a
 * specified class. When using a specified class, the resource is searched from
 * the package path of that class. Otherwise the resource is searched from
 * the root.
 *
 * @author Dennis Hofs (RRD)
 */
public class ClassLoaderResourceLocator implements ResourceLocator {
	private Class<?> loadClass = null;

	/**
	 * Constructs a new resource locator that will locate resources using the
	 * class loader.
	 */
	public ClassLoaderResourceLocator() {
	}

	/**
	 * Constructs a new resource locator that will locate resources using the
	 * specified class. If you set the load class to null, it will locate
	 * resources using the class loader.
	 *
	 * @param loadClass the load class or null
	 */
	public ClassLoaderResourceLocator(Class<?> loadClass) {
		this.loadClass = loadClass;
	}

	@Override
	public boolean resourceExists(String path) {
		return findResource(path) != null;
	}

	@Override
	public InputStream openResource(String path) throws IOException {
		URL url = findResource(path);
		if (url == null)
			throw new FileNotFoundException("Resource not found: " + path);
		return url.openStream();
	}

	/**
	 * Tries to find the resource at the specified path. If the resource doesn't
	 * exist, this method returns null.
	 *
	 * @param path the resource path
	 * @return the resource URL or null
	 */
	private URL findResource(String path) {
		if (loadClass == null)
			return getClass().getClassLoader().getResource(path);
		else
			return loadClass.getResource(path);
	}
}
