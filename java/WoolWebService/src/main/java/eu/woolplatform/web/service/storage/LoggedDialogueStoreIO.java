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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.rrd.utils.AppComponents;
import nl.rrd.utils.exception.DatabaseException;
import nl.rrd.utils.io.FileUtils;
import nl.rrd.utils.json.JsonMapper;
import eu.woolplatform.web.service.Configuration;
import eu.woolplatform.wool.model.WoolLoggedInteraction;
import eu.woolplatform.wool.model.WoolMessageSource;
import org.slf4j.Logger;
import org.springframework.util.ClassUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class LoggedDialogueStoreIO {
	private static final Object LOCK = new Object();

	private static final String LOG_DIRECTORY = "logged_dialogues";

	public static LoggedDialogue findLoggedDialogue(String user, String id)
			throws DatabaseException, IOException {
		return readLatestDialogueWithConditions(user,false,null,id);
	}

	public static LoggedDialogue findLatestOngoingDialogue(String user,
														   String dialogueName) throws DatabaseException, IOException {
		return readLatestDialogueWithConditions(user,true,dialogueName,null);
	}

	public static LoggedDialogue findLatestOngoingDialogue(String user) throws IOException, DatabaseException {
		return readLatestDialogueWithConditions(user, true, null, null);
	}

	public static void createLoggedDialogue(LoggedDialogue loggedDialogue)
			throws DatabaseException, IOException {

		Logger logger = AppComponents.getLogger("LoggedDialogueStoreIO");
		logger.info("Creating loggedDialogue. sessionId: '"+loggedDialogue.getSessionId()+"'.");

		synchronized (LOCK) {
			String id = UUID.randomUUID().toString().toLowerCase()
					.replaceAll("-", "");
			loggedDialogue.setId(id);
			List<LoggedDialogue> loggedDialogues = readSession(
					loggedDialogue.getUser(),
					loggedDialogue.getSessionId(),
					loggedDialogue.getSessionStartTime());

			loggedDialogues.add(loggedDialogue);

			saveToSession(
					loggedDialogue.getUser(),
					loggedDialogue.getSessionId(),
					loggedDialogue.getSessionStartTime(),
					loggedDialogues);
		}
	}

	public static void addLoggedAgentInteraction(long timestamp, String speaker,
												 LoggedDialogue loggedDialogue, String nodeId, int previousIndex,
												 String statement) throws DatabaseException, IOException {
		synchronized (LOCK) {
			loggedDialogue.getInteractionList().add(new WoolLoggedInteraction(
					timestamp, WoolMessageSource.AGENT, speaker,
					loggedDialogue.getDialogueName(), nodeId, previousIndex,
					statement, -1));
			saveToSession(loggedDialogue);
		}
	}

	public static void addLoggedUserInteraction(long timestamp, String speaker,
												LoggedDialogue loggedDialogue, String nodeId, int previousIndex,
												String statement, int replyId) throws DatabaseException,
			IOException {
		loggedDialogue.getInteractionList().add(new WoolLoggedInteraction(
				timestamp, WoolMessageSource.USER, speaker,
				loggedDialogue.getDialogueName(), nodeId, previousIndex,
				statement, replyId));
		saveToSession(loggedDialogue);
	}

	public static void setDialogueCompleted(LoggedDialogue loggedDialogue)
			throws DatabaseException, IOException {
		loggedDialogue.setCompleted(true);
		saveToSession(loggedDialogue);
	}

	public static void setDialogueCancelled(LoggedDialogue loggedDialogue)
			throws DatabaseException, IOException {
		loggedDialogue.setCancelled(true);
		saveToSession(loggedDialogue);
	}

	// ---------------------------------------------
	// ---------- New Save & Read Methods ----------
	// ---------------------------------------------

	private static void saveToSession(LoggedDialogue dialogue)
			throws DatabaseException, IOException {
		synchronized(LOCK) {
			List<LoggedDialogue> dialogues = readSessionWith(dialogue);
			saveToSession(dialogue.getUser(), dialogue.getSessionId(), dialogue.getSessionStartTime(),
					dialogues);
		}
	}

	private static void saveToSession(String user, String sessionId, long sessionStartTime,
									  List<LoggedDialogue> dialogues) throws IOException {
		synchronized (LOCK) {
			String json = JsonMapper.generate(dialogues);
			Configuration config = Configuration.getInstance();
			File dataDir = new File(config.get(Configuration.DATA_DIR));
			File logDir = new File(dataDir, LOG_DIRECTORY);
			File userDir = new File(logDir, user);
			createDirectory(userDir);
			File dataFile = new File(userDir, sessionStartTime + " " + sessionId + ".json");
			FileUtils.writeFileString(dataFile, json);
		}
	}

	private static List<LoggedDialogue> readSession(String user, String sessionId,
													long sessionStartTime)
			throws DatabaseException, IOException {
		List<LoggedDialogue> result;
		synchronized (LOCK) {
			Configuration config = Configuration.getInstance();
			File dataDir = new File(config.get(Configuration.DATA_DIR));
			File logDir = new File(dataDir, LOG_DIRECTORY);
			File userDir = new File(logDir, user);
			createDirectory(userDir);
			File dataFile = new File(userDir, sessionStartTime + " " + sessionId + ".json");
			if (!dataFile.exists())
				return new ArrayList<>();
			ObjectMapper mapper = new ObjectMapper();
			try {
				result = mapper.readValue(dataFile,
						new TypeReference<List<LoggedDialogue>>() {});
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
	 *
	 * @param loggedDialogue
	 * @return
	 * @throws DatabaseException
	 * @throws IOException
	 */
	private static List<LoggedDialogue> readSessionWith(LoggedDialogue loggedDialogue)
			throws DatabaseException, IOException {
		// Read all loggeddialogues in this session from file
		List<LoggedDialogue> dialogues = readSession(loggedDialogue.getUser(),
				loggedDialogue.getSessionId(), loggedDialogue.getSessionStartTime());

		// Remove any loggedDialogue (well, it should only be 1) that has the same Id as the one
		// we are adding.
		dialogues.removeIf(dialogue -> dialogue.getId().equals(
				loggedDialogue.getId()));

		// Add the new (updated) loggedDialogue provided
		dialogues.add(loggedDialogue);

		// Sort by time
		dialogues.sort(Comparator.comparingLong(LoggedDialogue::getUtcTime));
		return dialogues;
	}

	/**
	 * Dig through the given {@code user}'s log files, and look for the latest
	 * {@link LoggedDialogue} that matches the conditions provided. This method will look through
	 * all the user's dialogue log files in order (newest to oldest), and return the first occurence
	 * of a {@link LoggedDialogue} that matches all conditions.
	 *
	 * <p>If {@code mustBeOngoing} is {@code true} this method will only return a {@link LoggedDialogue}
	 * for which the #isCancelled and #isCompleted parameters are both false. Otherwise, these
	 * parameters are ignored.</p>
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
	 * @param user the user id for whom to look for dialogues.
	 * @param mustBeOngoing true if this method should only look for "ongoing" dialogues.
	 * @param dialogueName an optional dialogue name to look for (or {@code null}).
	 * @param id an optional id to look for (or {@code null}).
	 * @return the {@link LoggedDialogue} that matches the conditions, or {@code null} if none can
	 *         be found.
	 * @throws DatabaseException
	 * @throws IOException
	 */
	private static LoggedDialogue readLatestDialogueWithConditions(String user,
																   boolean mustBeOngoing,
																   String dialogueName, String id)
			throws DatabaseException, IOException {

		Logger logger = AppComponents.getLogger("LoggedDialogueStoreIO");
		LoggedDialogue result = null;

		logger.info("Finding latest ongoing dialogue for user "+user);

		Configuration config = Configuration.getInstance();

		File[] userLogFiles;

		synchronized (LOCK) {
			File dataDir = new File(config.get(Configuration.DATA_DIR));
			File logDir = new File(dataDir, LOG_DIRECTORY);
			File userDir = new File(logDir, user);
			createDirectory(userDir);

			userLogFiles = userDir.listFiles();
		}

		Arrays.sort(userLogFiles);

		logger.info("Found the following list of log files: ");

		for(File f : userLogFiles) {
			logger.info(f.getName());
		}

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

	private static List<LoggedDialogue> readSessionFile(File sessionFile)
			throws DatabaseException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		List<LoggedDialogue> result;
		synchronized(LOCK) {
			try {
				result = mapper.readValue(sessionFile,
						new TypeReference<List<LoggedDialogue>>() { });
			} catch (
					JsonProcessingException ex) {
				throw new DatabaseException("Failed to parse logged dialogues: "
						+ sessionFile.getAbsolutePath() + ": " + ex.getMessage(), ex);
			}
		}
		return result;
	}

	private static void createDirectory(File directory) throws IOException {
		if(!directory.exists()) {
			if(!directory.mkdirs())
				throw new IOException("Error creating log directory: "+directory);
		} else if(!directory.isDirectory()) {
			throw new IOException("Error creating log directory: "+directory);
		}
	}

}
