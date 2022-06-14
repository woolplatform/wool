/*
 * Copyright 2019-2022 WOOL Foundation.
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
package eu.woolplatform.webservice.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.woolplatform.utils.exception.DatabaseException;
import eu.woolplatform.utils.io.FileUtils;
import eu.woolplatform.utils.json.JsonMapper;
import eu.woolplatform.webservice.Configuration;
import eu.woolplatform.wool.model.WoolLoggedInteraction;
import eu.woolplatform.wool.model.WoolMessageSource;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class LoggedDialogueStoreIO {
	private static final Object LOCK = new Object();

	private static final String DIR = "logged_dialogues";

	public static LoggedDialogue findLoggedDialogue(String user, String id)
			throws DatabaseException, IOException {
		List<LoggedDialogue> loggedDialogues = read(user);
		for (LoggedDialogue dialogue : loggedDialogues) {
			if (dialogue.getId().equals(id))
				return dialogue;
		}
		return null;
	}

	public static LoggedDialogue findLatestOngoingDialogue(String user,
			String dialogueName) throws DatabaseException, IOException {
		List<LoggedDialogue> loggedDialogues = read(user);
		for (int i = loggedDialogues.size() - 1; i >= 0; i--) {
			LoggedDialogue dialogue = loggedDialogues.get(i);
			if (dialogue.getDialogueName().equals(dialogueName) &&
					!dialogue.isCompleted() &&
					!dialogue.isCancelled()) {
				return dialogue;
			}
		}
		return null;
	}

	public static void createLoggedDialogue(LoggedDialogue loggedDialogue)
			throws DatabaseException, IOException {
		synchronized (LOCK) {
			String user = loggedDialogue.getUser();
			String id = UUID.randomUUID().toString().toLowerCase()
					.replaceAll("-", "");
			loggedDialogue.setId(id);
			List<LoggedDialogue> loggedDialogues = read(user);
			loggedDialogues.add(loggedDialogue);
			save(user, loggedDialogues);
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
			save(loggedDialogue);
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
		save(loggedDialogue);
	}

	public static void setDialogueCompleted(LoggedDialogue loggedDialogue)
			throws DatabaseException, IOException {
		loggedDialogue.setCompleted(true);
		save(loggedDialogue);
	}

	public static void setDialogueCancelled(LoggedDialogue loggedDialogue)
			throws DatabaseException, IOException {
		loggedDialogue.setCancelled(true);
		save(loggedDialogue);
	}

	private static List<LoggedDialogue> read(String user)
			throws DatabaseException, IOException {
		List<LoggedDialogue> result;
		synchronized (LOCK) {
			Configuration config = Configuration.getInstance();
			File dataDir = new File(config.get(Configuration.DATA_DIR));
			dataDir = new File(dataDir, DIR);
			FileUtils.mkdir(dataDir);
			File dataFile = new File(dataDir, user + ".json");
			if (!dataFile.exists())
				return new ArrayList<>();
			ObjectMapper mapper = new ObjectMapper();
			try {
				result = mapper.readValue(dataFile,
						new TypeReference<List<LoggedDialogue>>() {});
			} catch (JsonProcessingException ex) {
				throw new DatabaseException(
						"Failed to parse logged dialogues: " +
						dataFile.getAbsolutePath() + ": " + ex.getMessage(),
						ex);
			}
		}
		result.sort(Comparator.comparingLong(LoggedDialogue::getUtcTime));
		return result;
	}

	private static List<LoggedDialogue> readListWith(
			LoggedDialogue loggedDialogue) throws DatabaseException,
			IOException {
		List<LoggedDialogue> dialogues = read(loggedDialogue.getUser());
		dialogues.removeIf(dialogue -> dialogue.getId().equals(
				loggedDialogue.getId()));
		dialogues.add(loggedDialogue);
		dialogues.sort(Comparator.comparingLong(LoggedDialogue::getUtcTime));
		return dialogues;
	}

	private static void save(String user, List<LoggedDialogue> dialogues)
			throws IOException {
		synchronized (LOCK) {
			String json = JsonMapper.generate(dialogues);
			Configuration config = Configuration.getInstance();
			File dataDir = new File(config.get(Configuration.DATA_DIR));
			dataDir = new File(dataDir, DIR);
			FileUtils.mkdir(dataDir);
			File dataFile = new File(dataDir, user + ".json");
			FileUtils.writeFileString(dataFile, json);
		}
	}

	private static void save(LoggedDialogue dialogue) throws DatabaseException,
			IOException {
		synchronized (LOCK) {
			List<LoggedDialogue> dialogues = readListWith(dialogue);
			save(dialogue.getUser(), dialogues);
		}
	}
}
