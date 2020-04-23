package eu.woolplatform.webservice.dialogue;

import eu.woolplatform.utils.AppComponents;
import eu.woolplatform.utils.exception.DatabaseException;
import eu.woolplatform.utils.exception.ParseException;
import eu.woolplatform.utils.expressions.EvaluationException;
import eu.woolplatform.utils.i18n.I18nLanguageFinder;
import eu.woolplatform.utils.i18n.I18nUtils;
import eu.woolplatform.wool.exception.WoolException;
import eu.woolplatform.wool.execution.ActiveWoolDialogue;
import eu.woolplatform.wool.execution.WoolVariableStore;
import eu.woolplatform.wool.model.WoolDialogue;
import eu.woolplatform.wool.model.WoolDialogueDescription;
import eu.woolplatform.wool.model.WoolNode;
import eu.woolplatform.wool.model.nodepointer.WoolNodePointer;
import eu.woolplatform.wool.model.nodepointer.WoolNodePointerExternal;
import eu.woolplatform.wool.model.nodepointer.WoolNodePointerInternal;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.*;

/**
 * An {@link ConversationalAgent} class models an abstract instance of a {@link ConversationalAgent}.
 * 
 * @author Tessa Beinema
 *
 */
public class ConversationalAgent {
	
	protected UserService userService;
	protected WoolVariableStore variableStore;
	protected ActiveWoolDialogue activeWoolDialogue;

	// availableDialogues: map from file name -> language -> dialogue description
	protected Map<String,Map<String,WoolDialogueDescription>> availableDialogues =
			new LinkedHashMap<>();
	
	public ConversationalAgent(UserService userService,
			WoolVariableStore variableStore) {
		this.userService = userService;
		this.variableStore = variableStore;
		List<WoolDialogueDescription> agentDialogues =
				userService.getServiceManager().getAvailableDialogues();
		for (WoolDialogueDescription agentDialogue : agentDialogues) {
			String name = agentDialogue.getDialogueName();
			Map<String,WoolDialogueDescription> langMap =
					availableDialogues.get(name);
			if (langMap == null) {
				langMap = new LinkedHashMap<>();
				availableDialogues.put(name, langMap);
			}
			langMap.put(agentDialogue.getLanguage(), agentDialogue);
		}
	}

	/**
	 * Returns the available dialogues for the conversational agent filtered for
	 * language. You can specify an ISO language tag such as "en-US".
	 *
	 * @param language an ISO language tag
	 * @return a list of available dialogues in the user's current language.
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
				availableDialogues.values()) {
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
	 * Starts the dialogue for the specified dialogue definition. It starts at
	 * the start node.
	 *
	 * @param dialogueDescription the dialogue description
	 * @param dialogueDefinition the dialogue definition
	 * @return the start node
	 * @throws DatabaseException if a database error occurs
	 * @throws IOException if a communication error occurs
	 * @throws WoolException if the request is invalid
	 */
	public WoolNode startDialogue(WoolDialogueDescription dialogueDescription,
			WoolDialogue dialogueDefinition) throws DatabaseException,
			IOException, WoolException {
		return startDialogue(dialogueDescription, dialogueDefinition, null);
	}

	/**
	 * Starts the dialogue for the specified dialogue definition. If you specify
	 * a node ID, it will start at that node. Otherwise it starts at the start
	 * node.
	 *
	 * @param dialogueDescription the dialogue description
	 * @param dialogueDefinition the dialogue definition
	 * @param nodeId the node ID or null
	 * @return the start node or specified node
	 * @throws DatabaseException if a database error occurs
	 * @throws IOException if a communication error occurs
	 * @throws WoolException if the request is invalid
	 */
	public WoolNode startDialogue(WoolDialogueDescription dialogueDescription,
			WoolDialogue dialogueDefinition, String nodeId)
			throws DatabaseException, IOException, WoolException {
		boolean success = false;
		try {
			activeWoolDialogue = new ActiveWoolDialogue(dialogueDescription,
					dialogueDefinition);
			activeWoolDialogue.setWoolVariableStore(this.variableStore);
			WoolNode startNode;
			try {
				startNode = activeWoolDialogue.startDialogue(nodeId, null);
			} catch (EvaluationException e) {
				throw new RuntimeException("Expression evaluation error: " +
						e.getMessage(), e);
			}
			executeNextNode(startNode);
			success = true;
			return startNode;
		} finally {
			if (!success)
				activeWoolDialogue = null;
		}
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
		if (activeWoolDialogue == null) {
			throw new WoolException(WoolException.Type.NO_ACTIVE_DIALOGUE,
					"No active dialogue");
		}
		// Find next WoolNode:
		WoolNodePointer nodePointer;
		try {
			nodePointer = activeWoolDialogue.processReplyAndGetNodePointer(
					replyId, null);
		} catch (EvaluationException ex) {
			throw new RuntimeException("Expression evaluation error: " +
					ex.getMessage(), ex);
		}
		WoolNode nextWoolNode;
		if (nodePointer instanceof WoolNodePointerInternal) {
			try {
				nextWoolNode = activeWoolDialogue.progressDialogue(
						(WoolNodePointerInternal)nodePointer, null);
			} catch (EvaluationException e) {
				throw new RuntimeException("Expression evaluation error: " +
						e.getMessage(), e);
			}
			executeNextNode(nextWoolNode);
			return nextWoolNode;
		}
		else {
			setActiveDialogueCompleted();
			WoolNodePointerExternal externalNodePointer =
					(WoolNodePointerExternal)nodePointer;
			String dialogueId = externalNodePointer.getDialogueId();
			String nodeId = externalNodePointer.getNodeId();
			return userService.startDialogue(dialogueId, nodeId, null);
		}
	}

	/**
	 * Cancels the current dialogue.
	 * 
	 * @throws DatabaseException if a database error occurs
	 * @throws IOException if a communication error occurs
	 */
	public void cancelDialogue() throws DatabaseException, IOException {
		activeWoolDialogue = null;
	}
	
	/**
	 * This method is called before the current node is returned from
	 * startDialogue() or progressDialogue(). The node can be null as a result
	 * of progressDialogue() with an end reply.
	 * 
	 * <p>If the node is not null, this method adds a logged agent interaction
	 * for it.</p>
	 * 
	 * <p>If the node is null or it has no replies, the dialogue is
	 * completed.</p>
	 * 
	 * @param node the current node or null
	 * @throws DatabaseException if a database error occurs
	 * @throws IOException if a communication error occurs
	 */
	private void executeNextNode(WoolNode node)
			throws DatabaseException, IOException {
		if (node == null || node.getBody().getReplies().isEmpty()) {
			activeWoolDialogue = null;
		}
	}
	
	private void setActiveDialogueCompleted() throws DatabaseException,
			IOException {
		activeWoolDialogue = null;
	}

	/**
	 * Returns the dialogue description for the specified dialogue ID and
	 * preferred language. You can specify an ISO language tag such as "en-US".
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
	 * Stores the specified variables in the variable store.
	 *
	 * @param variables the variables
	 */
	public void storeReplyInput(Map<String,?> variables)
			throws WoolException {
		if (activeWoolDialogue == null) {
			throw new WoolException(WoolException.Type.NO_ACTIVE_DIALOGUE,
					"No active dialogue");
		}
		this.activeWoolDialogue.storeReplyInput(variables, null);
	}
}
