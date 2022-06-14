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
package eu.woolplatform.webservice.execution;

import eu.woolplatform.utils.AppComponents;
import eu.woolplatform.utils.exception.DatabaseException;
import eu.woolplatform.utils.exception.ParseException;
import eu.woolplatform.webservice.UserCredentials;
import eu.woolplatform.webservice.UserFile;
import eu.woolplatform.webservice.exception.HttpFieldError;
import eu.woolplatform.wool.exception.WoolException;
import eu.woolplatform.wool.i18n.WoolTranslationContext;
import eu.woolplatform.wool.model.WoolDialogue;
import eu.woolplatform.wool.model.WoolDialogueDescription;
import eu.woolplatform.wool.model.WoolProject;
import eu.woolplatform.wool.parser.WoolFileLoader;
import eu.woolplatform.wool.parser.WoolProjectParser;
import eu.woolplatform.wool.parser.WoolProjectParserResult;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The WOOL Web Service maintains one instance of a {@link UserServiceManager}. This
 * class keeps track of the different active {@link UserService} instances that are
 * needed to serve individual user's of the WOOL Web Service.
 *
 * @author Harm op den Akker
 * @author Tessa Beinema
 */
public class UserServiceManager {

	private Logger logger = AppComponents.getLogger(getClass().getSimpleName());
	private WoolProject woolProject;
	private List<UserService> activeUserServices = new ArrayList<>();
	private List<UserCredentials> userCredentials;

	// ----- Constructors
	
	/**
	 * Creates an instance of a {@link UserServiceManager}, that loads in a
	 * predefined list of Wool dialogues.
	 */
	public UserServiceManager(WoolFileLoader woolFileLoader) {
		logger.info("Initializing UserServiceManager.");
		long startMS = System.currentTimeMillis();
		
		UserServiceFactory appConfig = UserServiceFactory.getInstance();
		WoolProjectParser woolProjectParser = new WoolProjectParser(
				woolFileLoader);
		WoolProjectParserResult readResult;
		try {
			readResult = woolProjectParser.parse();
		} catch (IOException ex) {
			throw new RuntimeException("Error while reading WOOL project: " +
					ex.getMessage(), ex);
		}
		for (String path : readResult.getParseErrors().keySet()) {
			logger.error("Failed to parse " + path + ":");
			for (ParseException ex : readResult.getParseErrors().get(path)) {
				logger.error("*** " + ex.getMessage());
			}
		}
		for (String path : readResult.getWarnings().keySet()) {
			logger.warn("Warning at parsing " + path + ":");
			for (String warning : readResult.getWarnings().get(path)) {
				logger.warn("*** " + warning);
			}
		}
		if (!readResult.getParseErrors().isEmpty())
			throw new RuntimeException("Failed to load all dialogues.");
		woolProject = readResult.getProject();
		appConfig.setWoolProject(woolProject);

		// Read all UserCredentials from users.xml
		try {
			userCredentials = UserFile.read();
		} catch (
				ParseException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		long endMS = System.currentTimeMillis();
		long procTime = endMS - startMS;
		logger.info("UserServiceManager initialized in "+procTime+"ms.");
	}
	
	// ---------- Getters:
	
	public List<WoolDialogueDescription> getDialogueDescriptions() {
		return new ArrayList<>(woolProject.getDialogues().keySet());
	}

	/**
	 * Returns the list of {@link UserCredentials} available for this {@link UserServiceManager}.
	 * @return the list of {@link UserCredentials} available for this {@link UserServiceManager}.
	 */
	public List<UserCredentials> getUserCredentials() {
		return userCredentials;
	}

	/**
	 * Returns the {@link UserCredentials} object associated with the given {@code username}, or {@code null}
	 * if no such user is known.
	 * @param username the username of the user to look for.
	 * @return the {@link UserCredentials} object or {@code null}.
	 */
	public UserCredentials getUserCredentialsForUsername(String username) {
		for(UserCredentials uc : userCredentials) {
			if(uc.getUsername().equals(username)) return uc;
		}
		return null;
	}
	
	// ---------- Service Management:
	
	/**
	 * Returns an active {@link UserService} object for the given {@code accountId} and {@code userId}. Retrieves
	 * from an internal list of active {@link UserService}s, or instantiates a new {@link UserService} if no {@link UserService} is active
	 * for the given parameters.
	 * @param userId the identifier of the specific user that is interacting with the {@link UserService}.
	 * @return an {@link UserService} object that can handle the communication with the user.
	 */
	public UserService getActiveUserService(String userId) throws DatabaseException, IOException {
		
		for(UserService userService : activeUserServices) {
			if(userService.getUserId().equals(userId)) {
				return userService;
			}
		}
		
		logger.info("No active UserService for userId '"+userId+"' creating UserService instance.");
		
		// Initialize new userService
		UserServiceFactory appConfig = UserServiceFactory.getInstance();
		UserService newUserService;
		try {
			newUserService = appConfig.createUserService(userId, this);
		} catch (IOException ex) {
			String error = "Can't create userService: " + ex.getMessage();
			logger.error(error, ex);
			throw new RuntimeException(error, ex);
		}
		
		// Store as "active userService"
		activeUserServices.add(newUserService);
		return newUserService;
	}
	
	/**
	 * Removes the given {@link UserService} from the set of active {@link UserService}s in
	 * this {@link UserServiceManager}.
	 * @param userService the {@link UserService} to remove.
	 * @return {@code true} if the given was successfully removed, {@code false} if
	 * it was not present on the list of active {@link UserService}s in the first place.
	 */
	public boolean removeUserService(UserService userService) {
		return activeUserServices.remove(userService);
	}
	
	// ---------- Dialogue Management:

	public WoolDialogue getDialogueDefinition(WoolDialogueDescription descr,
			WoolTranslationContext translationContext) throws WoolException {
		WoolDialogue dlg;
		if (translationContext == null)
			dlg = woolProject.getDialogues().get(descr);
		else
			dlg = woolProject.getTranslatedDialogue(descr, translationContext);
		if (dlg != null)
			return dlg;
		throw new WoolException(WoolException.Type.DIALOGUE_NOT_FOUND,
				"Pre-loaded dialogue not found for dialogue '" + descr.getDialogueName() +
				"' in language '"+descr.getLanguage() + "'.");
	}
	
	public List<WoolDialogueDescription> getAvailableDialogues() {
		List<WoolDialogueDescription> result = new ArrayList<>();
		result.addAll(woolProject.getDialogues().keySet());
		return result;
	}

	public static DateTime parseTimeParameters(String time, String timezone,
			List<HttpFieldError> errors) {
		DateTimeZone parsedTimezone = null;
		LocalDateTime parsedTime = null;
		int errorsStart = errors.size();
		if (timezone == null || timezone.length() == 0) {
			parsedTimezone = DateTimeZone.getDefault();
		} else {
			try {
				parsedTimezone = DateTimeZone.forID(timezone);
			} catch (IllegalArgumentException ex) {
				errors.add(new HttpFieldError("timezone",
						"Invalid value for field \"timezone\": " + timezone));
			}
		}
		if (time == null || time.length() == 0) {
			parsedTime = new LocalDateTime(parsedTimezone);
		} else {
			DateTimeFormatter parser = DateTimeFormat.forPattern(
					"yyyy-MM-dd'T'HH:mm:ss.SSS");
			try {
				parsedTime = parser.parseLocalDateTime(time);
			} catch (IllegalArgumentException ex) {
				errors.add(errorsStart, new HttpFieldError("time",
						"Invalid value for field \"time\": " + time));
			}
		}
		if (!errors.isEmpty())
			return null;
		return parsedTime.toDateTime(parsedTimezone);
	}
}
