package eu.woolplatform.webservice.dialogue;

import eu.woolplatform.utils.exception.DatabaseException;
import eu.woolplatform.utils.exception.ParseException;
import eu.woolplatform.wool.exception.WoolException;
import eu.woolplatform.wool.model.WoolDialogue;
import eu.woolplatform.wool.model.WoolDialogueDescription;
import eu.woolplatform.wool.model.WoolProject;
import eu.woolplatform.wool.parser.WoolFileLoader;
import eu.woolplatform.wool.parser.WoolProjectParser;
import eu.woolplatform.wool.parser.WoolProjectParserResult;
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

	/**
	 * Retrieves the dialogue definition for the specified description, or throws
	 * a {@link WoolException WoolException} with {@link
	 * WoolException.Type#DIALOGUE_NOT_FOUND DIALOGUE_NOT_FOUND} if no such
	 * dialogue definition exists in this service manager.
	 * 
	 * @param soughtDialogueDescription the sought dialogue description
	 * @return the {@link WoolDialogue} containing the Wool dialogue representation.
	 * @throws WoolException if the dialogue definition is not found
	 */
	public WoolDialogue getDialogueDefinition(WoolDialogueDescription soughtDialogueDescription) throws WoolException {
		WoolDialogue dlg = woolProject.getDialogues().get(
				soughtDialogueDescription);
		if (dlg != null)
			return dlg;
		throw new WoolException(WoolException.Type.DIALOGUE_NOT_FOUND,
				"Pre-loaded dialogue not found for dialogue '" + soughtDialogueDescription.getDialogueName() +
				"' with main speaker '" + soughtDialogueDescription.getMainSpeaker() +
				"' in language '"+soughtDialogueDescription.getLanguage() + "'.");
	}
	
	public List<WoolDialogueDescription> getAvailableDialogues() {
		List<WoolDialogueDescription> result = new ArrayList<>();
		result.addAll(woolProject.getDialogues().keySet());
		return result;
	}
}
