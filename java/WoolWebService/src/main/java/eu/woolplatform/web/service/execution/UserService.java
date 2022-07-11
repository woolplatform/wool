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
package eu.woolplatform.web.service.execution;

import eu.woolplatform.utils.AppComponents;
import eu.woolplatform.utils.exception.DatabaseException;
import eu.woolplatform.utils.exception.ParseException;
import eu.woolplatform.utils.i18n.I18nLanguageFinder;
import eu.woolplatform.utils.i18n.I18nUtils;
import eu.woolplatform.web.service.Configuration;
import eu.woolplatform.web.service.model.*;
import eu.woolplatform.wool.exception.WoolException;
import eu.woolplatform.wool.execution.*;
import eu.woolplatform.wool.i18n.WoolTranslationContext;
import eu.woolplatform.wool.model.*;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.*;

/**
 * A {@link UserService} is a service class that handles all communication with the WOOL
 * Web Service for a specific User (identified by a {@code userId}).
 * 
 * @author Harm op den Akker
 * @author Tessa Beinema
 */
public class UserService {

	public String userId;
	public UserServiceManager userServiceManager;
	public WoolVariableStore variableStore;

	private final Logger logger;
	private final DialogueExecutor dialogueExecutor;
	private WoolTranslationContext translationContext = null;

	// dialogueLanguageMap: map from dialogue name -> language -> dialogue description
	protected Map<String, Map<String,WoolDialogueDescription>> dialogueLanguageMap = new LinkedHashMap<>();

	// ----- Constructors
	
	/**
	 * Instantiates a {@link UserService} for a given user, identified
	 * by the associated {@code accountId} and {@code userId}. The UserService creates a {@link WoolVariableStore} instance
	 * and loads in all known variables for the user.
	 * @param userId - A unique identifier of the current user this {@link UserService} is interacting with.
	 * @param userServiceManager the server's {@link UserServiceManager} instance.
	 * @param onVarChangeListener the {@link WoolVariableStoreOnChangeListener} to be added to the {@link WoolVariableStore}
	 *                            instance that this {@link UserService} creates.
	 */
	public UserService(String userId, UserServiceManager userServiceManager,
			WoolVariableStoreOnChangeListener onVarChangeListener)
			throws DatabaseException, IOException {

		this.logger = AppComponents.getLogger(getClass().getSimpleName());
		this.userId = userId;
		this.userServiceManager = userServiceManager;

		Configuration config = AppComponents.get(Configuration.class);
		WoolVariableStoreStorageHandler storageHandler = new WoolVariableStoreJSONStorageHandler(config.getDataDir()+"/variables");
		try {
			this.variableStore = storageHandler.read(userId);
		} catch (ParseException ex) {
			throw new DatabaseException("Failed to read initial variables: " +
					ex.getMessage(), ex);
		}

		this.variableStore.addOnChangeListener(onVarChangeListener);
		dialogueExecutor = new DialogueExecutor(this);

		// create dialogueLanguageMap
		List<WoolDialogueDescription> dialogues =
				userServiceManager.getDialogueDescriptions();
		for (WoolDialogueDescription dialogue : dialogues) {
			String name = dialogue.getDialogueName();
			Map<String, WoolDialogueDescription> langMap =
					dialogueLanguageMap.computeIfAbsent(name, k -> new LinkedHashMap<>());
			langMap.put(dialogue.getLanguage(), dialogue);
		}
	}

	// ----- Getters & Setters
	
	/**
	 * Returns the user identifier which this {@link UserService} is serving.
	 * @return the user identifier which this {@link UserService} is serving.
	 */
	public String getUserId() {
		return userId;
	}

	public WoolTranslationContext getTranslationContext() {
		return translationContext;
	}

	public void setTranslationContext(WoolTranslationContext translationContext) {
		this.translationContext = translationContext;
	}
	
	/**
	 * Returns the application's {@link UserServiceManager} that is governing this {@link UserService}.
	 * @return the application's {@link UserServiceManager} that is governing this {@link UserService}.
	 */
	public UserServiceManager getServiceManager() {
		return userServiceManager;
	}

	public WoolVariableStore getVariableStore() {
		return this.variableStore;
	}

	// ----- Methods (Dialogue Execution)

	/**
	 * Starts a dialogue with the given {@code dialogueId} and preferred
	 * language, returning the first step of the dialogue. If you specify a
	 * {@code nodeId}, it will start at that node. Otherwise, it starts at the
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
		return dialogueExecutor.startDialogue(dialogueDescription,
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
		return dialogueExecutor.progressDialogue(state, replyId, time);
	}

	public ExecuteNodeResult backDialogue(DialogueState state, DateTime time)
			throws WoolException {
		ActiveWoolDialogue dialogue = state.getActiveDialogue();
		String dialogueName = dialogue.getDialogueName();
		String nodeName = dialogue.getCurrentNode().getTitle();
		logger.info(String.format(
				"User %s goes back in dialogue from node %s.%s",
				userId, dialogueName, nodeName));
		return dialogueExecutor.backDialogue(state, time);
	}

	public ExecuteNodeResult executeCurrentNode(DialogueState state,
												DateTime time) throws WoolException {
		return dialogueExecutor.executeCurrentNode(state, time);
	}

	/**
	 * Cancels the current dialogue.
	 *
	 * @throws DatabaseException if a database error occurs
	 * @throws IOException if a communication error occurs
	 */
	public void cancelDialogue(String loggedDialogueId)
			throws DatabaseException, IOException {
		logger.info("User '" + this.getUserId() + "' cancels dialogue with Id '"+loggedDialogueId+"'.");
		LoggedDialogue loggedDialogue =
				LoggedDialogueStoreIO.findLoggedDialogue(userId,
						loggedDialogueId);
		if(loggedDialogue != null)
			LoggedDialogueStoreIO.setDialogueCancelled(loggedDialogue);
		else
			logger.warn("User '" + this.getUserId() + "' attempted to cancel dialogue with Id '"+loggedDialogueId+"', but the dialogue could not be found.");
	}

	// ----- Methods (Variables):

	/**
	 * Stores the specified variables in the variable store.
	 * @param state
	 * @param variables the variables
	 * @param time
	 */
	public void storeReplyInput(DialogueState state, Map<String,?> variables,
								DateTime time) throws WoolException {
		ActiveWoolDialogue dialogue = state.getActiveDialogue();
		dialogue.storeReplyInput(variables, time);
	}

	/**
	 * This function ensures that for all WOOL Variables in the given {@link Set}, of
	 * {@code variableNames} an up-to-date value is loaded into the {@link WoolVariableStore}
	 * for this user represented by this {@link UserService} through an external WOOL Variable Service
	 * if, and only if one has been configured. If {@code config.getExternalVariableServiceEnabled() == false}
	 * this method will cause no changes to occur.
	 * @param variableNames the set of WOOL Variables that need to have their values updated.
	 */
	public void updateVariablesFromExternalService(Set<String> variableNames) {
		logger.info("Attempting to update values from external service for the following set of variables: "+variableNames);

		Configuration config = AppComponents.get(Configuration.class);

		if(!config.getExternalVariableServiceEnabled()) {
			logger.info("No external WOOL Variable Service has been configured, no variables have been updated.");
		} else {
			logger.info("An external WOOL Variable Service is configured to be enabled, with parameters:");
			logger.info("URL: "+config.getExternalVariableServiceURL());
			logger.info("API Version: "+config.getExternalVariableServiceAPIVersion());

			List<WoolVariable> varsToUpdate = new ArrayList<>();
			for(String variableName : variableNames) {
				WoolVariable woolVariable = variableStore.getWoolVariable(variableName);
				if(woolVariable != null) {
					logger.info("A WOOL Variable '"+variableName+"' exists for User '"+userId+"': "+woolVariable);
					varsToUpdate.add(woolVariable); //TODO: Remove WoolVariableResponse
				} else {
					varsToUpdate.add(new WoolVariable(variableName,null,DateTime.now()));
				}

			}

			// Construct the api end-point to call for retrieving variable updates
			String retrieveUpdatesUrl = config.getExternalVariableServiceURL()
					+ "/v"+config.getExternalVariableServiceAPIVersion()
					+ "/variables/retrieve-updates/"
					+ getUserId();

			logger.info("Retrieve updates URL: "+retrieveUpdatesUrl);

			RestTemplate restTemplate = new RestTemplate();

			ResponseEntity<WoolVariableResponse[]> response = restTemplate.postForEntity(
					retrieveUpdatesUrl,
					varsToUpdate,
					WoolVariableResponse[].class);

			WoolVariableResponse[] woolVariableResponses = response.getBody();

			if(woolVariableResponses != null) {
				if(woolVariableResponses.length == 0) {
					logger.info("Received response from WOOL Variable Service: no variable updates needed.");
				} else {
					logger.info("Received response from WOOL Variable Service: the following variables have updated values:");
					for(WoolVariableResponse wvr : woolVariableResponses) {
						logger.info(wvr.toString());
						String varName = wvr.getName();
						String varValue = wvr.getValue();
						Long varUpdated = wvr.getLastUpdated();

						variableStore.setValue(varName,varValue,true,new DateTime(varUpdated));
					}
				}
			}
		}
	}

	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder.build();
	}

	// ----- Methods (Retrieval)

	/**
	 * Returns the available dialogues for all agents in the specified preferred
	 * language. You can specify an ISO language tag such as "en-US".
	 *
	 * @param language an ISO language tag
	 * @return a list of dialogue names
	 */
	public List<WoolDialogueDescription> getAvailableDialogues(String language) {
		List<WoolDialogueDescription> filteredAvailableDialogues =
				new ArrayList<>();
		Locale prefLocale;
		try {
			prefLocale = I18nUtils.languageTagToLocale(language);
		} catch (ParseException ex) {
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
		return this.userServiceManager.getDialogueDefinition(dialogueDescription,
				translationContext);
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
