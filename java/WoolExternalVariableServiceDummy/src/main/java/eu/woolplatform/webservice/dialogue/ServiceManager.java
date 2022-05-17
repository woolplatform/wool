package eu.woolplatform.webservice.dialogue;

import eu.woolplatform.utils.exception.DatabaseException;
import eu.woolplatform.utils.exception.ParseException;
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
 * A {@link ServiceManager} manages one or more services, each corresponding to a specific userId-password combination.
 * 
 * @author Harm op den Akker
 * @author Tessa Beinema
 *
 */
public class ServiceManager {
	private final Logger logger = getLogger(ServiceManager.class);

	private WoolProject woolProject;
		
	private List<UserService> activeUserServices = new ArrayList<>();

	// ---------- Constructors:
	
	/**
	 * Creates an instance of an {@link ServiceManager}, that loads in a
	 * predefined list of Wool dialogues.
	 */
	public ServiceManager(WoolFileLoader woolFileLoader) {
		logger.info("Initializing ServiceManager.");
		long startMS = System.currentTimeMillis();
		
		ServiceManagerConfig appConfig = ServiceManagerConfig.getInstance();
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
			throw new RuntimeException("Failed to load all dialogues");
		woolProject = readResult.getProject();
		appConfig.setWoolProject(woolProject);
		long endMS = System.currentTimeMillis();
		long procTime = endMS - startMS;
		logger.info("ServiceManager initialized in "+procTime+"ms.");
	}

	// ---------- Logging

	private static ILoggerFactory loggerFactory =
			LoggerFactory.getILoggerFactory();

	public static Logger getLogger(Class<?> clazz) {
		return loggerFactory.getLogger(clazz.getName());
	}

	public static Logger getLogger(String name) {
		return loggerFactory.getLogger(name);
	}

	public static ILoggerFactory getLoggerFactory() {
		return loggerFactory;
	}

	public static void setLoggerFactory(ILoggerFactory loggerFactory) {
		ServiceManager.loggerFactory = loggerFactory;
	}
	
	// ---------- Getters:
	
	public List<WoolDialogueDescription> getDialogueDescriptions() {
		return new ArrayList<>(woolProject.getDialogues().keySet());
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
		ServiceManagerConfig appConfig = ServiceManagerConfig.getInstance();
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
	 * this {@link ServiceManager}.
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
