/*
 * Copyright 2019-2023 WOOL Foundation - Licensed under the MIT License:
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
import com.azure.storage.file.datalake.models.PathItem;
import eu.woolplatform.web.service.Configuration;
import eu.woolplatform.web.service.exception.WWSConfigurationException;
import nl.rrd.utils.AppComponents;
import org.slf4j.Logger;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * The AzureDataLakeStore class is used for handling file transfers to and from an Azure Data Lake
 * that can be configured to act as a back-up for the local file storage of the WOOL Web Service.
 *
 * @author Harm op den Akker
 */
public class AzureDataLakeStore {

	private final Logger logger = AppComponents.getLogger(getClass().getSimpleName());

	private final Configuration config;
	private final DataLakeFileSystemClient dataLakeFileSystemClient;
	public static final String AUTHENTICATION_METHOD_SAS = "sas-token";
	public static final String AUTHENTICATION_METHOD_ACCOUNT_KEY = "account-key";

	public AzureDataLakeStore() throws WWSConfigurationException {

		config = Configuration.getInstance();

		DataLakeServiceClient dataLakeServiceClient;
		String authMethod = config.getAzureDataLakeAuthenticationMethod();
		// Option 1: Create ServiceClient using SAS Token
		if(authMethod.equals(AUTHENTICATION_METHOD_SAS)) {
			dataLakeServiceClient = new DataLakeServiceClientBuilder()
				.endpoint(config.getAzureDataLakeSASAccountUrl())
				.sasToken(config.getAzureDataLakeSASToken())
				.buildClient();
		}

		// Option 2: Create ServiceClient using accountName and accountKey
		else if(authMethod.equals(AUTHENTICATION_METHOD_ACCOUNT_KEY)) {
			StorageSharedKeyCredential sharedKeyCredential =
					new StorageSharedKeyCredential(config.getAzureDataLakeAccountName(),
							config.getAzureDataLakeAccountKey());

			DataLakeServiceClientBuilder builder = new DataLakeServiceClientBuilder();
			builder.credential(sharedKeyCredential);
			builder.endpoint("https://" + config.getAzureDataLakeAccountName()
					+ ".dfs.core.windows.net");
			dataLakeServiceClient = builder.buildClient();
		} else {
			throw new WWSConfigurationException("Attempting to initialize AzureDataLakeStore, " +
				"but an unknown authentication method '"+authMethod+"' was configured.");
		}

		dataLakeFileSystemClient =
				dataLakeServiceClient.getFileSystemClient(config.getAzureDataLakeFileSystemName());

		// Log a successful connection message
		if(authMethod.equals(AUTHENTICATION_METHOD_SAS)) {
			logger.info("Successfully initiated Azure Data Lake Client using account URL '" +
					config.getAzureDataLakeSASAccountUrl() + "' and file system '" +
					config.getAzureDataLakeFileSystemName() + "'.");
		} else if(authMethod.equals(AUTHENTICATION_METHOD_ACCOUNT_KEY)) {
			logger.info("Successfully initiated Azure Data Lake Client for account: '" +
					config.getAzureDataLakeAccountName() + "' and file system '" +
					config.getAzureDataLakeFileSystemName() + "'.");
		}
	}

	/**
	 * Writes the given {@code file} for the given {@code user} to the Azure Data Lake.
	 * @param user the identifier of the user to which the file belongs.
	 * @param file the file to write to the Azure Data Lake.
	 */
	public void writeLoggedDialogueFile(String user, File file) {
		DataLakeDirectoryClient directoryClient =
				dataLakeFileSystemClient.getDirectoryClient(
						config.getDirectoryNameDialogues() + "/" + user);
		DataLakeFileClient fileClient = directoryClient.getFileClient(file.getName());
		try {
			fileClient.uploadFromFile(file.getAbsolutePath(),true);
		} catch(UncheckedIOException e) {
			logger.error("Failed to upload dialogue log session '"
					+ file.getAbsolutePath() + "' to Azure Data Lake.");
		}
	}

	/**
	 * Writes an application log file to the Azure Data Lake.
	 * @param file the log file to write.
	 */
	public void writeApplicationLogFile(File file) {
		DataLakeDirectoryClient directoryClient =
				dataLakeFileSystemClient.getDirectoryClient(config.getDirectoryNameApplicationLogs());
		DataLakeFileClient fileClient = directoryClient.getFileClient(file.getName());
		try {
			fileClient.uploadFromFile(file.getAbsolutePath(),true);
			logger.info("Successfully uploaded application log '" +
					file.getAbsolutePath() + "' to Azure Data Lake.");
		} catch(UncheckedIOException e) {
			logger.error("Failed to upload application log '" +
					file.getAbsolutePath() + "' to Azure Data Lake.");
		}
	}

	/**
	 * Populate the local dialogue log folder for the given user, identified by the
	 * {@code woolUserId}. This method will retrieve a "recent" set of LoggedDialogue log files from
	 * the Azure Data Lake, and saves them into the local dialogue log folder for the given user.
	 *
	 * @param woolUserId the id of the WOOL user for whom to look for dialogues.
	 * @throws IOException in case of an error writing to the local files.
	 */
	public void populateLocalDialogueLogs(String woolUserId) throws IOException {
		DataLakeDirectoryClient directoryClient =
				dataLakeFileSystemClient.getDirectoryClient(
						config.getDirectoryNameDialogues() + "/" + woolUserId);

		PagedIterable<PathItem> pathItems = directoryClient.listPaths();

		for (PathItem pathItem : pathItems) {
			// item.getName() returns e.g. "dialogues/user-name/{fileName}.json"
			Path path = Paths.get(config.getDataDir() + File.separator + pathItem.getName());
			String fileName = path.getFileName().toString();

			logger.info("Found file on Azure Data Lake for user '" + woolUserId
					+ "': " + pathItem.getName() + " (file name: '" + fileName + "').");

			DataLakeFileClient fileClient =
					dataLakeFileSystemClient.getFileClient(pathItem.getName());

			File localFile = new File(config.getDataDir() + File.separator +
					config.getDirectoryNameDialogues() + File.separator +
					woolUserId + File.separator + fileName);

			OutputStream targetStream = new FileOutputStream(localFile);
			fileClient.read(targetStream);
			targetStream.close();
		}
	}


}
