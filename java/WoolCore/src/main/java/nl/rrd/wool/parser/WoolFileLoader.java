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

package nl.rrd.wool.parser;

import nl.rrd.wool.exception.ParseException;

import java.io.IOException;
import java.io.Reader;
import java.util.List;

/**
 * A WOOL file loader is used by a {@link WoolProjectParser WoolProjectParser}
 * to list and open WOOL dialogue files (.wool) and translation files (.json)
 * within a WOOL project. The default implementation is {@link
 * WoolResourceFileLoader WoolResourceFileLoader}, which can load files from
 * resources on the classpath.
 *
 * @author Dennis Hofs (RRD)
 */
public interface WoolFileLoader {

	/**
	 * Lists all WOOL files in the project. The files should be dialogue files
	 * (.wool) or translation files (.json).
	 *
	 * @return the files
	 * @throws IOException if a reading error occurs
	 */
	List<WoolFileDescription> listWoolFiles() throws IOException;

	/**
	 * Opens the specified WOOL file. This should be a dialogue file (.wool)
	 * or a translation file (.json).
	 *
	 * @param descr the file description
	 * @return the reader for the file
	 * @throws IOException if the file cannot be opened
	 */
	Reader openFile(WoolFileDescription descr) throws IOException;
}
