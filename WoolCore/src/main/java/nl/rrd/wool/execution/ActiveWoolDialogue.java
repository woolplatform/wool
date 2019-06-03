package nl.rrd.wool.execution;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import nl.rrd.wool.exception.WoolUnknownVariableException;
import nl.rrd.wool.execution.WoolVariableStore.VariableSource;
import nl.rrd.wool.expressions.EvaluationException;
import nl.rrd.wool.model.WoolDialogue;
import nl.rrd.wool.model.WoolNode;
import nl.rrd.wool.model.WoolNodeBody;
import nl.rrd.wool.model.nodepointer.WoolNodePointer;
import nl.rrd.wool.model.nodepointer.WoolNodePointerInternal;
import nl.rrd.wool.model.reply.WoolReply;
import nl.rrd.wool.model.reply.WoolReplyAutoForward;
import nl.rrd.wool.model.reply.WoolReplyBasic;
import nl.rrd.wool.model.reply.WoolReplyInput;
import nl.rrd.wool.model.statement.WoolStatement;
import nl.rrd.wool.model.statement.WoolStatementBasic;
import nl.rrd.wool.model.statement.WoolStatementBasicIdentified;
import nl.rrd.wool.model.statement.WoolStatementCommandBody;
import nl.rrd.wool.model.statement.WoolStatementCommandIf;
import nl.rrd.wool.model.statement.WoolStatementCommandSet;
import nl.rrd.wool.model.statement.WoolStatementMultimedia;

/**
 * An {@link ActiveWoolDialogue} is a wrapper around a {@link WoolDialogue}, which contains
 * a static definition of a dialogue (referred to as the {@code dialogueDefinition}). 
 * The {@link ActiveWoolDialogue} also contains utility functions to keep track of the state during 
 * "execution" of the dialogue.
 * 
 * @author Harm op den Akker
 * @author Tessa Beinema
 */
public class ActiveWoolDialogue {
	
	private WoolDialogue dialogueDefinition;
	private WoolNode currentNode;
	private DialogueState dialogueState;
	private WoolVariableStore woolVariableStore;
		
	// ----------- Constructors:
	
	public ActiveWoolDialogue(WoolDialogue dialogueDefinition) {
		this.dialogueDefinition = dialogueDefinition;
		this.dialogueState = DialogueState.INACTIVE;
	}
	
	// ---------- Getters:
	
	public WoolDialogue getDialogueDefinition() {
		return dialogueDefinition;
	}
	
	public WoolNode getCurrentNode() {
		return currentNode;
	}
	
	public DialogueState getDialogueState() {
		return dialogueState;
	}
	
	/**
	 * Returns the {@link WoolVariableStore} associated with this {@link ActiveWoolDialogue}.
	 * @return the {@link WoolVariableStore} associated with this {@link ActiveWoolDialogue}.
	 */
	public WoolVariableStore getWoolVariableStore() {
		return woolVariableStore;
	}
	
	// ---------- Setters:
	
	public void setCurrentNode(WoolNode currentNode) {
		this.currentNode = currentNode;
	}
	
	public void setDialogueState(DialogueState dialogueState) {
		this.dialogueState = dialogueState;
	}
	
	/**
	 * Sets the {@link WoolVariableStore} used to store/retrieve parameters for this {@link ActiveWoolDialogue}.
	 * @param woolVariableStore the {@link WoolVariableStore} used to store/retrieve parameters for this {@link ActiveWoolDialogue}.
	 */
	public void setWoolVariableStore(WoolVariableStore woolVariableStore) {
		this.woolVariableStore = woolVariableStore;
	}
	
	// ---------- Convenience:
	
	/**
	 * Returns the name of this {@link ActiveWoolDialogue} as defined in the associated {@link WoolDialogue}.
	 * @return the name of this {@link ActiveWoolDialogue} as defined in the associated {@link WoolDialogue}.
	 */
	public String getDialogueName() {
		return dialogueDefinition.getDialogueName();
	}
	
	// ---------- Functions:
	
	/**
	 * "Starts" this {@link ActiveWoolDialogue}, returning the start node and updating
	 * its internal state.
	 * @return the initial {@link WoolNode}.
	 * @throws WoolUnknownVariableException 
	 */
	public WoolNode startDialogue() throws WoolUnknownVariableException {
		this.dialogueState = DialogueState.ACTIVE;
		WoolNode nextNode = dialogueDefinition.getStartNode();
		this.currentNode = nextNode;
		if(this.currentNode.getBody().getReplies().size() == 0) {
			this.dialogueState = DialogueState.FINISHED;
		}
		return executeWoolNode(nextNode);
	}
	
	/**
	 * "Starts" this {@link ActiveWoolDialogue} at the provided {@WoolNode}, returning that first node
	 * and updating the dialogue's internal state.
	 * @return the first {@link WoolNode}.
	 * @throws WoolUnknownVariableException
	 */
	public WoolNode startDialogue(String nodeId) throws WoolUnknownVariableException {
		this.dialogueState = DialogueState.ACTIVE;
		WoolNode nextNode = dialogueDefinition.getNodeById(nodeId);
		this.currentNode = nextNode;
		if(this.currentNode.getBody().getReplies().size() == 0) {
			this.dialogueState = DialogueState.FINISHED;
		}
		return executeWoolNode(nextNode);
	}
	
	/**
	 * Retrieves the next dialogue and node Ids based on the provided reply id.
	 * @param replyId
	 * @return WoolNodePointer nodePointer to next (dialogue and) node. 
	 */
	public WoolNodePointer processReplyAndGetNodePointer(int replyId) {
		WoolReply selectedWoolReply = currentNode.getBody().getReplyById(replyId);
		Map<String, String> variablesToSet = selectedWoolReply.getVariablesToSet();
		
		for(String variableName : variablesToSet.keySet()) {
			this.woolVariableStore.setValue(variableName, variablesToSet.get(variableName), VariableSource.CORE);
		}
		WoolNodePointer nodePointer = selectedWoolReply.getNodePointer();
		return nodePointer;
	}
	
	/**
	 * Takes the selected reply and selects the next {@link WoolNode} based on the replies in the current {@link WoolNode}. 
	 * If there is a next node, then returns the executed version of that next {@link WoolNode} which results from a call to the {@link #executeWoolNode(WoolNode)} function. 
	 * @param selectedWoolReply
	 * @return the next {@link WoolNode} that follows on the selected reply.  
	 * @throws WoolUnknownVariableException
	 */
	public WoolNode progressDialogue(WoolNodePointerInternal nodePointer) throws WoolUnknownVariableException {
		WoolNode nextNode = null;
		nextNode = dialogueDefinition.getNodeById(nodePointer.getNodeId());
		if(nextNode != null) {
			this.currentNode = nextNode;
			if(this.currentNode.getBody().getReplies().size() == 0) {
				this.dialogueState = DialogueState.FINISHED;
			}
			this.currentNode = executeWoolNode(nextNode);
			return currentNode;
		} else {
			return null;
		}
	}
	
	public void storeReplyInput(int replyId, String input) {
		WoolReply inputReply = this.currentNode.getBody().getReplyById(replyId);
		if(inputReply instanceof WoolReplyInput) {
			String variableName = ((WoolReplyInput) inputReply).getInputVariable();
			this.woolVariableStore.setValue(variableName, input, VariableSource.CORE);
		}
	}
	
	/**
	 * The user's client returned the given {@code replyId} - what was the statement that was
	 * uttered by the user?
	 * @param replyId
	 * @return 
	 * @throws WoolUnknownVariableException 
	 */
	public String getUserStatementFromReplyId(int replyId) throws WoolUnknownVariableException {
		WoolReply selectedWoolReply = currentNode.getBody().getReplyById(replyId);
		if(selectedWoolReply instanceof WoolReplyBasic) {
			return ((WoolReplyBasic)selectedWoolReply).getStatement();
		} 
		else if(selectedWoolReply instanceof WoolReplyAutoForward) {
			return "AUTOFORWARD";
		} 
		else if(selectedWoolReply instanceof WoolReplyInput) {
			return ((WoolReplyInput)selectedWoolReply).getBeforeInputStatement() + this.woolVariableStore.getValue(((WoolReplyInput)selectedWoolReply).getInputVariable())+ ((WoolReplyInput)selectedWoolReply).getAfterInputStatement(); 
		}
		else return null;
	}
	
	/**
	 * Executes the WoolNode (i.e. evaluates the command statements and returns a flattened 1-level of statements node).
	 * @param WoolNode a node to execute
	 * @return WoolNode an executed WoolNode (i.e. all ifs and sets etc. are set and evaluated and removed accordingly) 
	 * @throws WoolUnknownVariableException 
	 */
	private WoolNode executeWoolNode(WoolNode woolNode) throws WoolUnknownVariableException {
		WoolNode processedNode = new WoolNode();
		processedNode.setHeader(woolNode.getHeader());
		
		ArrayList<WoolStatement> processedStatements = new ArrayList<WoolStatement>();
		ArrayList<WoolReply> processedReplies = new ArrayList<WoolReply>();
		
		for (WoolStatement statement : woolNode.getBody().getStatements()) {
			if (statement instanceof WoolStatementBasic || statement instanceof WoolStatementBasicIdentified) {
				processedStatements.add(this.replaceVariablesByValuesInStatement(statement));
			} 
			else if (statement instanceof WoolStatementCommandSet) {
				WoolStatementCommandSet setStatement = (WoolStatementCommandSet)statement;
				woolVariableStore.setValue(setStatement.getVariableName(), setStatement.getVariableValue(), VariableSource.CORE);
			}
			else if (statement instanceof WoolStatementCommandIf) {
				WoolStatementCommandIf ifStatement = (WoolStatementCommandIf)statement;
				WoolStatementCommandBody ifBodyEvaluated = evaluateWoolStatementCommandIf(ifStatement);
				processedStatements.addAll(ifBodyEvaluated.getStatements());
				processedReplies.addAll(ifBodyEvaluated.getReplies());
			}
			else if (statement instanceof WoolStatementMultimedia) {
				processedStatements.add(statement);
			}
		}
		
		processedReplies.addAll(woolNode.getBody().getReplies());
		ArrayList<WoolReply> processedAndUpdatedReplies = new ArrayList<WoolReply>();
		for (WoolReply reply : processedReplies) {
			if (reply instanceof WoolReplyBasic) {
				reply = this.replaceVariablesByValuesInReply(reply); 
			}
			processedAndUpdatedReplies.add(reply);
		}
		
		processedNode.setBody(new WoolNodeBody(processedStatements, processedAndUpdatedReplies));
		return processedNode;
	}
	
	private WoolReply replaceVariablesByValuesInReply(WoolReply reply) throws WoolUnknownVariableException {
		if (reply instanceof WoolReplyBasic) {
			WoolReplyBasic woolReplyBasic = new WoolReplyBasic((WoolReplyBasic) reply);
			if (woolReplyBasic.getVariablesInStatement().size() > 0) {
				Set<String> variables = woolReplyBasic.getVariablesInStatement();
				
				String instantiatedReply = woolReplyBasic.getStatement();
				for (String variable : variables) {
					String storedValue = this.woolVariableStore.getValue(variable);
					instantiatedReply = instantiatedReply.replaceAll("\\$" + variable, storedValue);
				}
				woolReplyBasic.setStatement(instantiatedReply);
			}
			return woolReplyBasic;
		}
		else return reply;
	}
	
	private WoolStatement replaceVariablesByValuesInStatement(WoolStatement statement) throws WoolUnknownVariableException{
		WoolStatement returnStatement = statement;
		if (statement instanceof WoolStatementBasic) {
			WoolStatementBasic woolStatementBasic = new WoolStatementBasic((WoolStatementBasic)statement);
			if (woolStatementBasic.getVariables().size() > 0) {
				Set<String> variables = woolStatementBasic.getVariables();
				String instantiatedStatement = woolStatementBasic.getStatement();
				for (String variable : variables) {
					String storedValue = this.woolVariableStore.getValue(variable);
					instantiatedStatement = instantiatedStatement.replaceAll("\\$" + variable, storedValue);
				}
				woolStatementBasic.setStatement(instantiatedStatement);
			}
			returnStatement = woolStatementBasic;
		}
		else if (statement instanceof WoolStatementBasicIdentified) {
			WoolStatementBasicIdentified woolStatementBasicIdentified = new WoolStatementBasicIdentified((WoolStatementBasicIdentified)statement);
			if (woolStatementBasicIdentified.getVariables().size() > 0) {
				Set<String> variables = woolStatementBasicIdentified.getVariables();
				String instantiatedStatement = woolStatementBasicIdentified.getStatement();
				for (String variable : variables) {
					String storedValue = woolVariableStore.getValue(variable);
					instantiatedStatement = instantiatedStatement.replaceAll("\\$" + variable, storedValue);
				}
				woolStatementBasicIdentified.setStatement(instantiatedStatement);
			}
			returnStatement = woolStatementBasicIdentified;
		} 
		return returnStatement;
	}
	
	/**
	 * Evaluates the if statements in a WoolNode.
	 * @param ifStatement a WoolStatementCommandIf
	 * @return WoolStatementCommmandBody 
	 * @throws WoolUnknownVariableException
	 */
	private WoolStatementCommandBody evaluateWoolStatementCommandIf(WoolStatementCommandIf ifStatement) throws WoolUnknownVariableException {
		WoolStatementCommandBody ifBodyFlattened = new WoolStatementCommandBody();
		
		try {
			if (ifStatement.getExpression().evaluate(((DefaultWoolVariableStore) woolVariableStore).getVariableMap()).asBoolean()) {
				if(ifStatement.getIfBody().getStatements().size() > 0) {
					for (WoolStatement statementInIf : ifStatement.getIfBody().getStatements()) {
						if (statementInIf instanceof WoolStatementBasic || statementInIf instanceof WoolStatementBasicIdentified) {
							ifBodyFlattened.addStatement(this.replaceVariablesByValuesInStatement(statementInIf));
						}
						else if (statementInIf instanceof WoolStatementCommandSet) {
							WoolStatementCommandSet setStatement = (WoolStatementCommandSet)statementInIf;
							woolVariableStore.setValue(setStatement.getVariableName(), setStatement.getVariableValue(), VariableSource.CORE);
						}
						else if (statementInIf instanceof WoolStatementCommandIf) {
							WoolStatementCommandBody ifBodyExtracted = this.evaluateWoolStatementCommandIf((WoolStatementCommandIf) statementInIf);;
							for (WoolStatement statement : ifBodyExtracted.getStatements()) {
								ifBodyFlattened.addStatement(statement);
							}
							for (WoolReply reply : ifBodyExtracted.getReplies()) {
								ifBodyFlattened.addReply(reply);
							}
						}
					}
				}
				if (ifStatement.getIfBody().getReplies().size() > 0) {
					for (WoolReply reply : ifStatement.getIfBody().getReplies()) {
						ifBodyFlattened.addReply(reply);
					}
				}
			}
		} catch (EvaluationException e) {
			throw new RuntimeException(e);
		}
		return ifBodyFlattened;
	}
	
}