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
