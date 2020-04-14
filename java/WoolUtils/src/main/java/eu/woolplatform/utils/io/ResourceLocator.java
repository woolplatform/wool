package eu.woolplatform.utils.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * Interface to locate and open resources. Implementations can locate resources
 * for example from the file system, class loader or Android asset manager.
 *
 * @author Dennis Hofs (RRD)
 */
public interface ResourceLocator {

	/**
	 * Returns whether the specified resource exists.
	 *
	 * @param path the resource path
	 * @return true if the resource exists, false otherwise
	 */
	boolean resourceExists(String path);

	/**
	 * Opens the resource at the specified path.
	 *
	 * @param path the resource path
	 * @return the input stream
	 * @throws IOException if the resource doesn't exist or can't be opened
	 */
	InputStream openResource(String path) throws IOException;
}
