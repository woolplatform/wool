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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.woolplatform.web.service.Configuration;
import eu.woolplatform.web.service.execution.UserService;
import nl.rrd.utils.AppComponents;
import nl.rrd.utils.exception.DatabaseException;
import nl.rrd.utils.io.FileUtils;
import nl.rrd.utils.json.JsonMapper;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * A {@link LoggedDialogueStore} is a class that acts as the storage for {@link LoggedDialogue}
 * objects, used in the execution of dialogues by the WOOL Web Service.
 *
 * <p>A {@link LoggedDialogueStore} does not maintain any data in-memory, but immediately stores
 * any changes made to the configured storage mechanism.</p>
 *
 * @author Harm op den Akker
 */
public class LoggedDialogueStore {

	private final Logger logger = AppComponents.getLogger(getClass().getSimpleName());
	private final Configuration config = Configuration.getInstance();
	private final UserService userService;
	private final String woolUserId;
	private final File userLogDirectory;
	private static final Object LOCK = new Object();
	private LoggedDialogue latestStoredLoggedDialogue = null;

	// ------------------------------------ //
	// ---------- Constructor(s) ---------- //
	// ------------------------------------ //

	/**
	 * Creates an instance of a {@link LoggedDialogueStore} for the user identified by the given
	 * {@code woolUserId}, with a reference to that user's {@link UserService}. Upon instantiation,
	 * this {@link LoggedDialogueStore} attempts to create the directory to be used for logging
	 * dialogues (as defined in the {@link Configuration}).
	 * @param woolUserId the identifier of the WOOL User for which to instantiate this {@link
	 *                   LoggedDialogueStore}
	 * @param userService the {@link UserService} associated with this LoggedDialogueStore
	 * @throws IOException in case of an error instantiating the log folder.
	 */
	public LoggedDialogueStore(String woolUserId, UserService userService) throws IOException {
		logger.info("Initializing LoggedDialogueStore for user '" + woolUserId + "'.");

		this.userService = userService;
		this.woolUserId = woolUserId;

		File dialogueLogDirectory = new File(config.getDataDir() + "/"
				+ config.getDirectoryNameDialogues());

		// If the application's dialogue log directory doesn't exist yet
		if(!dialogueLogDirectory.exists()) {

			// Create this directory, and if it fails throw an error
			if(!dialogueLogDirectory.mkdirs()) {
				throw new IOException("Unable to create the dialogue log folder at "
						+ dialogueLogDirectory.getAbsolutePath());
			} else {
				logger.info("Created dialogue log directory at: "+dialogueLogDirectory);
			}
		}

		// Now instantiate this user's specific directory
		this.userLogDirectory = new File(dialogueLogDirectory, woolUserId);

		// If the folder doesn't exist, initialize it
		if(!userLogDirectory.exists()) {
			if(userLogDirectory.mkdirs()) {
				logger.info("Created user's dialogue log directory at: "+userLogDirectory);
				// The user  directory was created. In case an Azure Data Lake backup service is
				// enabled, check if there is data to populate this directory here.
				if(config.getAzureDataLakeEnabled()) {
					try {
						userService.getServiceManager().getAzureDataLakeStore().
								populateLocalDialogueLogs(woolUserId);
					} catch(IOException e) {
						logger.error("Error populating local dialogue log folder from Azure Data " +
							"Lake. It is possible that dialogue log information that is " +
							"available on the Azure Data Lake should have been synchronised to " +
							"the local WOOL Web Service storage, but something went wrong in " +
							"doing so. This does not warrant interrupting the current request, " +
							"so operation has continued as if no log information was available.");
					}
				}
			} else {
				throw new IOException("Unable to create the user's log folder at: "
						+ userLogDirectory.getAbsolutePath());
			}
		}
	}

	// --------------------------------------- //
	// ---------- Public Operations ---------- //
	// --------------------------------------- //

	public LoggedDialogue findLoggedDialogue(String id)
			throws DatabaseException, IOException {
		return readLatestDialogueWithConditions(false,null,id);
	}

	public LoggedDialogue findLatestOngoingDialogue(String dialogueName)
			throws DatabaseException, IOException {
		return readLatestDialogueWithConditions(true,dialogueName,null);
	}

	public LoggedDialogue findLatestOngoingDialogue()
			throws IOException, DatabaseException {
		return readLatestDialogueWithConditions(true, null, null);
	}

	public void setDialogueCancelled(LoggedDialogue loggedDialogue)
			throws DatabaseException, IOException {
		loggedDialogue.setCancelled(true);
		saveToSession(loggedDialogue);
	}

	public void saveToSession(LoggedDialogue dialogue)
			throws DatabaseException, IOException {
		this.latestStoredLoggedDialogue = dialogue;
		synchronized(LOCK) {
			List<LoggedDialogue> dialogues = readSessionWith(dialogue);
			saveToSession(dialogue.getSessionId(), dialogue.getSessionStartTime(), dialogues);
		}
	}

	/**
	 * Checks whether the given {@code sessionId} exists for this user.
	 * @param sessionId the sessionId for which to check.
	 * @return true if the sessionId exists, false otherwise.
	 */
	public boolean existsSessionId(String sessionId) throws DatabaseException {
		// First check whether the latest stored in-memory dialogue may be a match
		if(latestStoredLoggedDialogue != null) {
			if(latestStoredLoggedDialogue.getSessionId() != null) {
				if(latestStoredLoggedDialogue.getSessionId().equals(sessionId)) return true;
			}
		}

		// If not, check through the available files
		File[] userLogFiles;

		synchronized (LOCK) {
			userLogFiles = userLogDirectory.listFiles();
		}

		if(userLogFiles == null) throw new DatabaseException("Error retrieving file listing " +
				"from dialogue log directory for user '" + woolUserId + "'.");

		for(File f : userLogFiles) {
			if (f.getName().endsWith(sessionId + ".json")) {
				return true;
			}
		}
		return false;
	}

	// -------------------------------------------------- //
	// ---------- Private Read & Write Methods ---------- //
	// -------------------------------------------------- //

	private void saveToSession(String sessionId, long sessionStartTime,
									  List<LoggedDialogue> dialogues) throws IOException {
		synchronized (LOCK) {
			String json = JsonMapper.generate(dialogues);
			File dataFile = new File(userLogDirectory, sessionStartTime + " " + sessionId +
					".json");
			FileUtils.writeFileString(dataFile, json);
			if(config.getAzureDataLakeEnabled()) {
				userService.getServiceManager().getAzureDataLakeStore()
						.writeLoggedDialogueFile(woolUserId,dataFile);
			}
		}
	}

	private List<LoggedDialogue> readSession(String sessionId, long sessionStartTime)
			throws DatabaseException, IOException {
		List<LoggedDialogue> result;
		synchronized (LOCK) {
			File dataFile = new File(userLogDirectory, sessionStartTime + " " + sessionId +
					".json");
			if (!dataFile.exists())
				return new ArrayList<>();
			ObjectMapper mapper = new ObjectMapper();
			try {
				result = mapper.readValue(dataFile,
						new TypeReference<>() {
						});
			} catch (JsonProcessingException ex) {
				throw new DatabaseException(
						"Failed to parse logged dialogues: " + dataFile.getAbsolutePath() +
								": " + ex.getMessage(), ex);
			}
		}
		result.sort(Comparator.comparingLong(LoggedDialogue::getUtcTime));
		return result;
	}

	/**
	 * Provide the complete list of all LoggedDialogues that are part of the same session as the
	 * given loggedDialogue, including itself.
	 *
	 * @param loggedDialogue the {@link LoggedDialogue} for which to retrieve all of his friends.
	 * @return a List of LoggedDialogue objects that form the complete session that the given
	 *         {@code loggedDialogue} is part of, including itself
	 * @throws DatabaseException in case of an error reading from the dialogue log files.
	 * @throws IOException in case of an error reading from the dialogue log files.
	 */
	private List<LoggedDialogue> readSessionWith(LoggedDialogue loggedDialogue)
			throws DatabaseException, IOException {
		// Read all logged dialogues in this session from file
		List<LoggedDialogue> dialogues = readSession(loggedDialogue.getSessionId(),
				loggedDialogue.getSessionStartTime());

		// Remove any loggedDialogue (well, it should only be 1) that has the same id as the one
		// we are adding.
		dialogues.removeIf(dialogue -> dialogue.getId().equals(loggedDialogue.getId()));

		// Add the new (updated) loggedDialogue provided
		dialogues.add(loggedDialogue);

		// Sort by time
		dialogues.sort(Comparator.comparingLong(LoggedDialogue::getUtcTime));
		return dialogues;
	}

	/**
	 * Dig through the given {@code user}'s log files, and look for the latest
	 * {@link LoggedDialogue} that matches the conditions provided. This method will look through
	 * all the user's dialogue log files in order (newest to oldest), and return the first
	 * occurrence of a {@link LoggedDialogue} that matches all conditions.
	 *
	 * <p>If {@code mustBeOngoing} is {@code true} this method will only return a
	 * {@link LoggedDialogue} for which the #isCancelled and #isCompleted parameters are both false.
	 * Otherwise, these parameters are ignored.</p>
	 *
	 * <p>If a {@code dialogueName} is provided, the returned {@link LoggedDialogue} must have this
	 * given dialogue name. If {@code null} is provided, the condition is ignored.</p>
	 *
	 * <p>If a {@code id} is provided, the returned {@link LoggedDialogue} must have this id. If
	 * {@code null} is provided, the condition is ignored.</p>
	 *
	 * <p>Finally, if no {@link LoggedDialogue} is found that matches all given conditions, this
	 * method will return {@code null}.</p>
	 *
	 * @param mustBeOngoing true if this method should only look for "ongoing" dialogues.
	 * @param dialogueName an optional dialogue name to look for (or {@code null}).
	 * @param id an optional id to look for (or {@code null}).
	 * @return the {@link LoggedDialogue} that matches the conditions, or {@code null} if none can
	 *         be found.
	 * @throws DatabaseException in case of an error reading from the dialogue log files.
	 * @throws IOException in case of an error reading from the dialogue log files.
	 */
	private LoggedDialogue readLatestDialogueWithConditions(boolean mustBeOngoing,
															String dialogueName, String id)
			throws DatabaseException, IOException {

		// We maintain a reference to the latest stored LoggedDialogue in memory, which
		// is the prime candidate for any search, so we check it first.
		if(this.latestStoredLoggedDialogue != null) {
			boolean match = true;

			if(mustBeOngoing) {
				if(latestStoredLoggedDialogue.isCancelled()
						|| latestStoredLoggedDialogue.isCompleted()) match = false;
			}

			if(match && dialogueName != null) {
				if(!latestStoredLoggedDialogue.getDialogueName().equals(dialogueName))
					match = false;
			}

			if(match && id != null) {
				if(!latestStoredLoggedDialogue.getId().equals(id)) match = false;
			}

			if(match) return latestStoredLoggedDialogue;
		}

		LoggedDialogue result = null;

		File[] userLogFiles;

		synchronized (LOCK) {
			userLogFiles = userLogDirectory.listFiles();
		}

		if(userLogFiles == null) throw new DatabaseException("Error retrieving file listing " +
				"from dialogue log directory for user '" + woolUserId + "'.");

		Arrays.sort(userLogFiles);

		for(File f : userLogFiles) {
			List<LoggedDialogue> loggedDialogues = readSessionFile(f);
			if(loggedDialogues != null) {
				for(LoggedDialogue ld : loggedDialogues) {
					boolean match = true;

					if(mustBeOngoing) {
						if(ld.isCancelled() || ld.isCompleted()) match = false;
					}

					if(match && dialogueName != null) {
						if(!ld.getDialogueName().equals(dialogueName)) match = false;
					}

					if(match && id != null) {
						if(!ld.getId().equals(id)) match = false;
					}

					if(match) return ld;
				}
			}
		}

		return null;
	}

	/**
	 * Private method used to read the JSON contents of a given dialogue log session file and return
	 * it as a List of LoggedDialogue objects.
	 * @param sessionFile the File pointer to the dialogue log session file.
	 * @return a List of LoggedDialogue objects.
	 * @throws DatabaseException in case of a read error.
	 * @throws IOException in case of a read error.
	 */
	private List<LoggedDialogue> readSessionFile(File sessionFile)
			throws DatabaseException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		List<LoggedDialogue> result;
		synchronized(LOCK) {
			try {
				result = mapper.readValue(sessionFile,
						new TypeReference<>() {
						});
			} catch (JsonProcessingException ex) {
				throw new DatabaseException("Failed to parse logged dialogues: "
						+ sessionFile.getAbsolutePath() + ": " + ex.getMessage(), ex);
			}
		}
		return result;
	}

}
