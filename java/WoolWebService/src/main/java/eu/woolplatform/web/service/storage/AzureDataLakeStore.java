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

package eu.woolplatform.web.service.storage;

import com.azure.core.http.rest.PagedIterable;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.file.datalake.*;
import com.azure.storage.file.datalake.models.ListPathsOptions;
import com.azure.storage.file.datalake.models.PathItem;
import eu.woolplatform.web.service.Configuration;
import nl.rrd.utils.AppComponents;
import org.slf4j.Logger;

import java.io.*;

public class AzureDataLakeStore {

	private final Logger logger = AppComponents.getLogger(getClass().getSimpleName());

	private final Configuration config;
	private DataLakeFileSystemClient dataLakeFileSystemClient;

	public AzureDataLakeStore() {

		config = Configuration.getInstance();

		// Option 1: Using SAS Token
		DataLakeServiceClient dataLakeServiceClient = new DataLakeServiceClientBuilder()
				.endpoint(config.getAzureStorageAccountUrl())
				.sasToken(config.getAzureSASToken())
				.buildClient();

		// Option 2: Using accountName and accountKey
		//StorageSharedKeyCredential sharedKeyCredential =
		//		new StorageSharedKeyCredential(config.getAzureDataLakeAccountName(),
		//				config.getAzureDataLakeAccountKey());

		//DataLakeServiceClientBuilder builder = new DataLakeServiceClientBuilder();
		//builder.credential(sharedKeyCredential);
		//builder.endpoint("https://" + config.getAzureDataLakeAccountName() + ".dfs.core.windows.net");
		//DataLakeServiceClient dataLakeServiceClient = builder.buildClient();

		dataLakeFileSystemClient =
				dataLakeServiceClient.getFileSystemClient(config.getAzureFileSystemName());

		//listFilesInDirectory(dataLakeFileSystemClient);

		logger.info("Successfully initiated Azure Data Lake Client for account '" +
				config.getAzureDataLakeAccountName() + "' and file system '" +
				config.getAzureFileSystemName() + "'.");
	}

	/**
	 * Writes the given {@code file} for the given {@code user} to the Azure Data Lake.
	 * @param user the identifier of the user to which the file belongs.
	 * @param file the file to write to the Azure Data Lake.
	 */
	public void writeLoggedDialogueFile(String user, File file) {
		DataLakeDirectoryClient directoryClient =
				dataLakeFileSystemClient.getDirectoryClient(config.getDirectoryNameDialogues() + "/" + user);
		DataLakeFileClient fileClient = directoryClient.getFileClient(file.getName());
		try {
			fileClient.uploadFromFile(file.getAbsolutePath(),true);
			logger.info("Successfully uploaded dialogue log session '" + file.getAbsolutePath() + "' to Azure Data Lake.");
		} catch(UncheckedIOException e) {
			logger.error("Failed to upload dialogue log session '"+ file.getAbsolutePath() + "' to Azure Data Lake.");
		}
	}

	/**
	 * Writes an application log file to the Azure Data Lake.
	 * @param file the log file to write.
	 */
	public void writeApplicationLogFile(File file) {
		DataLakeDirectoryClient directoryClient =
				dataLakeFileSystemClient.getDirectoryClient(config.getDirectoryNameLogs());
		DataLakeFileClient fileClient = directoryClient.getFileClient(file.getName());
		try {
			fileClient.uploadFromFile(file.getAbsolutePath(),true);
			logger.info("Successfully uploaded application log '" + file.getAbsolutePath() + "' to Azure Data Lake.");
		} catch(UncheckedIOException e) {
			logger.error("Failed to upload application log '"+ file.getAbsolutePath() + "' to Azure Data Lake.");
		}
	}

	public void listFilesInDirectory(DataLakeFileSystemClient fileSystemClient){

		ListPathsOptions options = new ListPathsOptions();
		options.setPath("my-directory");

		PagedIterable<PathItem> pagedIterable =
				fileSystemClient.listPaths(options, null);

		java.util.Iterator<PathItem> iterator = pagedIterable.iterator();


		PathItem item = iterator.next();

		while (item != null)
		{
			System.out.println(item.getName());


			if (!iterator.hasNext())
			{
				break;
			}

			item = iterator.next();
		}

	}


}
