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
