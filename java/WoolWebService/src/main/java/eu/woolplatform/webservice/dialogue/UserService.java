package eu.woolplatform.webservice.dialogue;

import eu.woolplatform.utils.AppComponents;
import eu.woolplatform.utils.exception.DatabaseException;
import eu.woolplatform.utils.exception.ParseException;
import eu.woolplatform.utils.i18n.I18nLanguageFinder;
import eu.woolplatform.utils.i18n.I18nUtils;
import eu.woolplatform.webservice.model.LoggedDialogue;
import eu.woolplatform.webservice.model.LoggedDialogueStoreIO;
import eu.woolplatform.webservice.model.VariableStoreIO;
import eu.woolplatform.wool.exception.WoolException;
import eu.woolplatform.wool.execution.ActiveWoolDialogue;
import eu.woolplatform.wool.execution.ExecuteNodeResult;
import eu.woolplatform.wool.execution.WoolVariableStore;
import eu.woolplatform.wool.i18n.WoolTranslationContext;
import eu.woolplatform.wool.model.*;
import org.joda.time.DateTime;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.*;

/**
 * An {@link UserService} class models an instance for conversations with one or more virtual agents, belonging to a specific customer's account,
 * and talking to a specific user.
 * 
 * @author Harm op den Akker
 * @author Tessa Beinema
 */
public class UserService {
	public static final Logger logger = ServiceManager.getLogger(UserService.class);
	
	public String userId;
	public ServiceManager serviceManager;
	
	public WoolVariableStore variableStore;

	// dialogueLanguageMap: map from dialogue name -> language -> dialogue description
	protected Map<String, Map<String,WoolDialogueDescription>> dialogueLanguageMap =
			new LinkedHashMap<>();

	private ConversationalAgent conversationalAgent;

	private WoolTranslationContext translationContext = null;

	// -------------------- Constructors
	
	/**
	 * Instantiates an {@link UserService} for a given user, identified
	 * by the associated {@code accountId} and {@code userId}.
	 * @param userId - A unique identifier of the current user this {@link UserService} is interacting with.
	 * @param serviceManager the server's {@link ServiceManager} instance.
	 */
	public UserService(String userId, ServiceManager serviceManager,
			WoolVariableStore.OnChangeListener onVarChangeListener)
			throws DatabaseException, IOException {
		this.userId = userId;
		this.serviceManager = serviceManager;
		this.variableStore = new WoolVariableStore();
		Map<String, ?> vars;
		try {
			vars = VariableStoreIO.readVariables(userId);
		} catch (ParseException ex) {
			throw new DatabaseException("Failed to read initial variables: " +
					ex.getMessage(), ex);
		}
		Map<String,Object> varStoreMap = this.variableStore.getModifiableMap(
				false, null);
		varStoreMap.putAll(vars);
		this.variableStore.addOnChangeListener(onVarChangeListener);
		conversationalAgent = new ConversationalAgent(this, variableStore);

		// create dialogueLanguageMap
		List<WoolDialogueDescription> dialogues =
				serviceManager.getDialogueDescriptions();
		for (WoolDialogueDescription dialogue : dialogues) {
			String name = dialogue.getDialogueName();
			Map<String,WoolDialogueDescription> langMap =
					dialogueLanguageMap.get(name);
			if (langMap == null) {
				langMap = new LinkedHashMap<>();
				dialogueLanguageMap.put(name, langMap);
			}
			langMap.put(dialogue.getLanguage(), dialogue);
		}
	}

	public WoolTranslationContext getTranslationContext() {
		return translationContext;
	}

	public void setTranslationContext(WoolTranslationContext translationContext) {
		this.translationContext = translationContext;
	}

	// ---------- Getters:
	
	/**
	 * Returns the user identifier which this {@link UserService} is serving.
	 * @return the user identifier which this {@link UserService} is serving.
	 */
	public String getUserId() {
		return userId;
	}
	
	/**
	 * Returns the application's {@link ServiceManager} that is governing this {@link UserService}.
	 * @return the application's {@link ServiceManager} that is governing this {@link UserService}.
	 */
	public ServiceManager getServiceManager() {
		return serviceManager;
	}

	/**
	 * Returns the available dialogues for all agents in the specified preferred
	 * language. You can specify an ISO language tag such as "en-US".
	 *
	 * @param language an ISO language tag
	 * @return a list of dialogue names
	 */
	public List<WoolDialogueDescription> getAvailableDialogues(
			String language) {
		List<WoolDialogueDescription> filteredAvailableDialogues =
				new ArrayList<>();
		Locale prefLocale;
		try {
			prefLocale = I18nUtils.languageTagToLocale(language);
		} catch (ParseException ex) {
			Logger logger = AppComponents.getLogger(getClass().getSimpleName());
			logger.error(String.format(
					"Invalid language tag \"%s\", falling back to system locale",
					language) + ": " + ex.getMessage());
			prefLocale = Locale.getDefault();
		}
		for (Map<String,WoolDialogueDescription> langMap :
				dialogueLanguageMap.values()) {
			List<String> keys = new ArrayList<>(langMap.keySet());
			I18nLanguageFinder i18nFinder = new I18nLanguageFinder(keys);
			i18nFinder.setUserLocale(prefLocale);
			String lang = i18nFinder.find();
			if (lang != null)
				filteredAvailableDialogues.add(langMap.get(lang));
			else if (!keys.isEmpty())
				filteredAvailableDialogues.add(langMap.get(keys.get(0)));
		}
		return filteredAvailableDialogues;
	}

	/**
	 * Returns the dialogue description for the specified dialogue ID and
	 * preferred language.
	 *
	 * <p>If no dialogue with the specified ID is found, then this method
	 * returns null.</p>
	 *
	 * @param dialogueId the dialogue ID
	 * @param language an ISO language tag or null
	 * @return the dialogue description or null
	 */
	public WoolDialogueDescription getDialogueDescriptionFromId(
			String dialogueId, String language) {
		for (WoolDialogueDescription dialogueDescription :
				this.getAvailableDialogues(language)) {
			if (dialogueDescription.getDialogueName().equals(dialogueId)) {
				return dialogueDescription;
			}
		}
		return null;
	}

	public WoolVariableStore getVariableStore() {
		return this.variableStore;
	}

	// ---------- Functions:

	/**
	 * Starts a dialogue with the given {@code dialogueId} and preferred
	 * language, returning the first step of the dialogue. If you specify a
	 * {@code nodeId}, it will start at that node. Otherwise it starts at the
	 * Start node.
	 *
	 * <p>You can specify an ISO language tag such as "en-US".</p>
	 *
	 * @param dialogueId the dialogue ID
	 * @param nodeId a node ID or null
	 * @param language an ISO language tag
	 * @param time the time in the time zone of the user
	 * @return the dialogue node result with the start node or specified node
	 */
	public ExecuteNodeResult startDialogue(String dialogueId, String nodeId,
			String language, DateTime time) throws DatabaseException,
			IOException, WoolException {
		logger.info("User '" + getUserId() + "' is starting dialogue '" +
				dialogueId + "'");
		WoolDialogueDescription dialogueDescription =
				getDialogueDescriptionFromId(dialogueId, language);
		if (dialogueDescription == null) {
			throw new WoolException(WoolException.Type.DIALOGUE_NOT_FOUND,
					"Dialogue not found: " + dialogueId);
		}
		WoolDialogue dialogue = getDialogueDefinition(dialogueDescription);
		return conversationalAgent.startDialogue(dialogueDescription,
				dialogue, nodeId, time);
	}
	
	/**
	 * Continues the dialogue after the user selected the specified reply. This
	 * method stores the reply as a user action in the database, and it performs
	 * any "set" actions associated with the reply. Then it determines the next
	 * node, if any.
	 * 
	 * <p>If there is no next node, this method will complete the current
	 * dialogue, and this method returns null.</p>
	 * 
	 * <p>If the reply points to another dialogue, this method will complete the
	 * current dialogue and start the other dialogue.</p>
	 * 
	 * <p>For the returned node, this method executes the agent statement and
	 * reply statements using the variable store. It executes ("if" and "set")
	 * commands and resolves variables. The returned node contains any content
	 * that should be sent to the client. This content can be text or client
	 * commands, with all variables resolved.</p>
	 *
	 * @param state the state from which the dialogue should progress
	 * @param replyId the reply ID
	 * @param time the time in the time zone of the user
	 * @return the next node or null
	 * @throws DatabaseException if a database error occurs
	 * @throws IOException if a communication error occurs
	 * @throws WoolException if the request is invalid
	 */
	public ExecuteNodeResult progressDialogue(DialogueState state, int replyId,
			DateTime time) throws DatabaseException, IOException,
			WoolException {
		ActiveWoolDialogue dialogue = state.getActiveDialogue();
		String dialogueName = dialogue.getDialogueName();
		String nodeName = dialogue.getCurrentNode().getTitle();
		logger.info(String.format(
				"User %s progresses dialogue with reply %s.%s.%s",
				userId, dialogueName, nodeName, replyId));
		return conversationalAgent.progressDialogue(state, replyId, time);
	}

	public ExecuteNodeResult backDialogue(DialogueState state, DateTime time)
			throws DatabaseException, IOException, WoolException {
		ActiveWoolDialogue dialogue = state.getActiveDialogue();
		String dialogueName = dialogue.getDialogueName();
		String nodeName = dialogue.getCurrentNode().getTitle();
		logger.info(String.format(
				"User %s goes back in dialogue from node %s.%s",
				userId, dialogueName, nodeName));
		return conversationalAgent.backDialogue(state, time);
	}

	public ExecuteNodeResult executeCurrentNode(DialogueState state,
			DateTime time) throws WoolException {
		return conversationalAgent.executeCurrentNode(state, time);
	}

	/**
	 * Cancels the current dialogue.
	 * 
	 * @throws DatabaseException if a database error occurs
	 * @throws IOException if a communication error occurs
	 */
	public void cancelDialogue(String loggedDialogueId)
			throws DatabaseException, IOException {
		logger.info("User '" + this.getUserId() + "' cancels dialogue");
		LoggedDialogue loggedDialogue =
				LoggedDialogueStoreIO.findLoggedDialogue(userId,
				loggedDialogueId);
		LoggedDialogueStoreIO.setDialogueCancelled(loggedDialogue);
	}
	
	/**
	 * Retrieves the dialogue definition for the specified description, or throws
	 * a {@link WoolException WoolException} with {@link
	 * WoolException.Type#DIALOGUE_NOT_FOUND DIALOGUE_NOT_FOUND} if no such
	 * dialogue definition exists in this service manager.
	 * 
	 * @param dialogueDescription the sought dialogue description
	 * @return the {@link WoolDialogue} containing the Wool dialogue representation.
	 * @throws WoolException if the dialogue definition is not found
	 */
	public WoolDialogue getDialogueDefinition(
			WoolDialogueDescription dialogueDescription) throws WoolException {
		return this.serviceManager.getDialogueDefinition(dialogueDescription,
				translationContext);
	}
	
	/**
	 * Stores the specified variables in the variable store.
	 *
	 * @param variables the variables
	 */
	public void storeReplyInput(DialogueState state, Map<String,?> variables,
			DateTime time) throws WoolException {
		ActiveWoolDialogue dialogue = state.getActiveDialogue();
		dialogue.storeReplyInput(variables, time);
	}

	public DialogueState getDialogueState(String loggedDialogueId,
			int loggedInteractionIndex) throws WoolException, DatabaseException,
			IOException {
		LoggedDialogue loggedDialogue =
				LoggedDialogueStoreIO.findLoggedDialogue(userId,
				loggedDialogueId);
		if (loggedDialogue == null) {
			throw new WoolException(WoolException.Type.DIALOGUE_NOT_FOUND,
					"Logged dialogue not found");
		}
		return getDialogueState(loggedDialogue, loggedInteractionIndex);
	}

	public DialogueState getDialogueState(LoggedDialogue loggedDialogue,
			int loggedInteractionIndex) throws WoolException {
		String dialogueName = loggedDialogue.getDialogueName();
		WoolDialogueDescription dialogueDescription =
				getDialogueDescriptionFromId(dialogueName,
				loggedDialogue.getLanguage());
		if (dialogueDescription == null) {
			throw new WoolException(WoolException.Type.DIALOGUE_NOT_FOUND,
					"Dialogue not found: " + dialogueName);
		}
		WoolDialogue dialogueDefinition = getDialogueDefinition(
				dialogueDescription);
		List<WoolLoggedInteraction> interactions =
				loggedDialogue.getInteractionList();
		if (loggedInteractionIndex < 0 || loggedInteractionIndex >= interactions.size()) {
			throw new WoolException(WoolException.Type.INTERACTION_NOT_FOUND,
					String.format(
					"Interaction \"%s\" not found in logged dialogue \"%s\"",
					loggedInteractionIndex, loggedDialogue.getId()));
		}
		String nodeId = loggedDialogue.getInteractionList()
				.get(loggedInteractionIndex).getNodeId();
		WoolNode node = dialogueDefinition.getNodeById(nodeId);
		if (node == null) {
			throw new WoolException(WoolException.Type.NODE_NOT_FOUND,
					String.format("Node \"%s\" not found in dialogue \"%s\"",
							nodeId, dialogueName));
		}
		ActiveWoolDialogue activeDialogue = new ActiveWoolDialogue(
				dialogueDescription, dialogueDefinition);
		activeDialogue.setWoolVariableStore(variableStore);
		activeDialogue.setCurrentNode(node);
		return new DialogueState(dialogueDescription, dialogueDefinition,
				loggedDialogue, loggedInteractionIndex, activeDialogue);
	}
}
