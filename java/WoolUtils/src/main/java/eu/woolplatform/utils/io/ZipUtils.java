package eu.woolplatform.utils.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * This class contains static utility methods for zip files.
 * 
 * @author Dennis Hofs (RRD)
 */
public class ZipUtils {
	private static final int COPY_BUFFER_SIZE = 2048;
	
	/**
	 * Unzips a zip file to the specified output directory.
	 * 
	 * @param zipFile the zip file
	 * @param outputDir the output directory
	 * @throws IOException if a reading or writing error occurs
	 */
	public static void unzip(File zipFile, File outputDir) throws IOException {
		FileUtils.mkdir(outputDir);
		ZipInputStream zipIn = new ZipInputStream(new FileInputStream(
				zipFile));
		try {
			byte[] bs = new byte[COPY_BUFFER_SIZE];
			int len;
			ZipEntry entry;
			while ((entry = zipIn.getNextEntry()) != null) {
				try {
					File outFile = new File(outputDir, entry.getName());
					FileOutputStream output = new FileOutputStream(outFile);
					try {
						while ((len = zipIn.read(bs)) > 0) {
							output.write(bs, 0, len);
						}
					} finally {
						output.close();
					}
					outFile.setLastModified(entry.getTime());
				} finally {
					zipIn.closeEntry();
				}
			}
		} finally {
			zipIn.close();
		}
	}
	
	/**
	 * Creates a ZIP file containing only the specified source file.
	 * 
	 * @param srcFile the source file
	 * @param zipFile the ZIP file
	 * @throws IOException if a reading or writing error occurs
	 */
	public static void zipFile(File srcFile, File zipFile) throws IOException {
		ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(
				zipFile));
		try {
			ZipEntry entry = new ZipEntry(srcFile.getName());
			entry.setTime(srcFile.lastModified());
			InputStream input = new FileInputStream(srcFile);
			try {
				addZipFile(entry, input, zipOut);
			} finally {
				input.close();
			}
		} finally {
			zipOut.close();
		}
	}

	/**
	 * Creates a ZIP file containing a specified set of files within a root
	 * directory. The root directory is used to obtain the relative path to each
	 * file in the set. The relative paths are stored in the ZIP file.
	 *
	 * @param rootDir the root directory
	 * @param files the files to add to the ZIP file
	 * @param zipFile the ZIP file
	 * @throws IOException if a reading or writing error occurs
	 */
	public static void zipFileSet(File rootDir, List<File> files, File zipFile)
			throws IOException {
		ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(
				zipFile));
		try {
			for (File file : files) {
				ZipEntry entry = new ZipEntry(getRelativePath(rootDir, file));
				entry.setTime(file.lastModified());
				InputStream input = new FileInputStream(file);
				try {
					addZipFile(entry, input, zipOut);
				} finally {
					input.close();
				}
			}
		} finally {
			zipOut.close();
		}
	}

	/**
	 * Adds a file to the specified ZIP output stream.
	 *
	 * @param entry the ZIP entry for the file to add
	 * @param source the input stream from the file to add
	 * @param zipOut the ZIP output stream
	 * @throws IOException if a reading or writing error occurs
	 */
	private static void addZipFile(ZipEntry entry, InputStream source,
			ZipOutputStream zipOut) throws IOException {
		zipOut.putNextEntry(entry);
		byte[] bs = new byte[COPY_BUFFER_SIZE];
		int len;
		while ((len = source.read(bs)) > 0) {
			zipOut.write(bs, 0, len);
		}
		zipOut.closeEntry();
	}

	/**
	 * Returns the relative path to a file from the specified directory. It
	 * uses a forward slash (/) as directory separator.
	 *
	 * @param dir the directory
	 * @param file the file
	 * @return the relative path
	 * @throws IOException if the directory or file can't be converted to
	 * canonical form
	 */
	private static String getRelativePath(File dir, File file)
			throws IOException {
		dir = dir.getCanonicalFile();
		file = file.getCanonicalFile();
		List<String> path = new ArrayList<String>();
		path.add(file.getName());
		File parent = file.getParentFile();
		while (parent != null && !parent.equals(dir)) {
			path.add(0, parent.getName());
		}
		StringBuilder pathStr = new StringBuilder();
		boolean first = true;
		for (String component : path) {
			if (!first)
				pathStr.append("/");
			first = false;
			pathStr.append(component);
		}
		return pathStr.toString();
	}
}
