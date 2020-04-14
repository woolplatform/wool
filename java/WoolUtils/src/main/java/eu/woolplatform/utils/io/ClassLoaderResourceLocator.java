package eu.woolplatform.utils.io;

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
