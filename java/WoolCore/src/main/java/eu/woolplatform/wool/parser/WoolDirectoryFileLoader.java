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

package eu.woolplatform.wool.parser;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class WoolDirectoryFileLoader implements WoolFileLoader {
	private File directory;

	public WoolDirectoryFileLoader(File directory) {
		this.directory = directory;
	}

	@Override
	public List<WoolFileDescription> listWoolFiles() throws IOException {
		List<WoolFileDescription> result = new ArrayList<>();
		File[] children = directory.listFiles();
		for (File child : children) {
			if (!child.isDirectory() || child.getName().startsWith("."))
				continue;
			String language = child.getName();
			result.addAll(listDir(language, "", child));
		}
		return result;
	}

	private List<WoolFileDescription> listDir(String language, String path,
			File file) {
		List<WoolFileDescription> result = new ArrayList<>();
		File[] children = file.listFiles();
		for (File child : children) {
			if (child.isDirectory() && !child.getName().startsWith(".")) {
				result.addAll(listDir(language, path + child.getName() + "/",
						child));
			} else if (child.isFile() && (child.getName().endsWith(".wool") ||
					child.getName().endsWith(".json"))) {
				result.add(new WoolFileDescription(language,
						path + child.getName()));
			}
		}
		return result;
	}

	@Override
	public Reader openFile(WoolFileDescription descr) throws IOException {
		File file = new File(directory, descr.getLanguage() + "/" +
				descr.getFilePath());
		return new InputStreamReader(new FileInputStream(file),
				StandardCharsets.UTF_8);
	}
}
