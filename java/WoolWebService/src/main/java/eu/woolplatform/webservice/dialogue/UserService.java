package eu.woolplatform.webservice.dialogue;

import eu.woolplatform.utils.exception.DatabaseException;
import eu.woolplatform.utils.exception.ParseException;
import eu.woolplatform.webservice.model.VariableStoreIO;
import eu.woolplatform.wool.exception.WoolException;
import eu.woolplatform.wool.execution.WoolVariableStore;
import eu.woolplatform.wool.model.WoolDialogue;
import eu.woolplatform.wool.model.WoolDialogueDescription;
import eu.woolplatform.wool.model.WoolNode;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

	private ConversationalAgent conversationalAgent;

	
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
	public List<String> getAvailableDialogues(String language)
			throws DatabaseException, IOException {
		List<String> availableDialogues = new ArrayList<>();
		List<WoolDialogueDescription> dialoguesForAgent =
				conversationalAgent.getAvailableDialogues(language);
		for(WoolDialogueDescription dialogueDescription : dialoguesForAgent) {
			availableDialogues.add(dialogueDescription.getDialogueName());
		}
		return availableDialogues;
	}

	public WoolVariableStore getVariableStore() {
		return this.variableStore;
	}

	// ---------- Functions:

	/**
	 * Starts a dialogue with the given {@code dialogueId} and preferred
	 * language, setting the {@link ConversationalAgent}'s active dialogue and
	 * returning the first step of the dialogue. If you specify a
	 * {@code nodeId}, it will start at that node. Otherwise it starts at the
	 * Start node.
	 *
	 * <p>You can specify an ISO language tag such as "en-US".</p>
	 *
	 * @param dialogueId the dialogue ID
	 * @param nodeId a node ID or null
	 * @param language an ISO language tag
	 * @return a {@link WoolNode} representing the initial step of the initiated
	 * dialogue
	 */
	public WoolNode startDialogue(String dialogueId, String nodeId,
			String language) throws DatabaseException, IOException,
			WoolException {
		logger.info("User '" + getUserId() + "' is starting dialogue '" +
				dialogueId + "'");
		WoolDialogueDescription dialogueDescription =
				conversationalAgent.getDialogueDescriptionFromId(
				dialogueId, language);
		if (dialogueDescription == null) {
			throw new WoolException(WoolException.Type.DIALOGUE_NOT_FOUND,
					"Dialogue not found: " + dialogueId);
		}
		WoolDialogue dialogue = getDialogueDefinition(dialogueDescription);
		return conversationalAgent.startDialogue(dialogueDescription,
				dialogue, nodeId);
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
	 * @param replyId the reply ID
	 * @return the next node or null
	 * @throws DatabaseException if a database error occurs
	 * @throws IOException if a communication error occurs
	 * @throws WoolException if the request is invalid
	 */
	public WoolNode progressDialogue(int replyId)
			throws DatabaseException, IOException, WoolException {
		logger.info("User '{}' is progressing dialogue through reply '{}'",
				getUserId(), replyId);
		return conversationalAgent.progressDialogue(replyId);
	}

	/**
	 * Cancels the current dialogue.
	 * 
	 * @throws DatabaseException if a database error occurs
	 * @throws IOException if a communication error occurs
	 */
	public void cancelDialogue() throws DatabaseException, IOException {
		logger.info("User '" + this.getUserId() + "' cancels dialogue");
		conversationalAgent.cancelDialogue();
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
		return this.serviceManager.getDialogueDefinition(dialogueDescription);
	}
	
	/**
	 * Stores the specified variables in the variable store.
	 *
	 * @param variables the variables
	 */
	public void storeReplyInput(Map<String,?> variables)
			throws WoolException {
		conversationalAgent.storeReplyInput(variables);
	}
}
