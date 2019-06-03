package nl.rrd.wool.model.reply;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import nl.rrd.wool.model.nodepointer.WoolNodePointer;

public class WoolReplyInput extends WoolReply {
	
	public enum InputType {
		TEXT, NUMERIC;
	};
	
	private InputType inputType; 
	private String beforeInputStatement;
	private String inputVariable;
	private String afterInputStatement;
	private int min;
	private int max;
	private Set<String> variablesInStatement;
	
	public WoolReplyInput(int replyId, WoolNodePointer nextNodePointer, InputType inputType, String beforeInputStatement, String inputVariable, String afterInputStatement, int min, int max) {
		super(replyId, nextNodePointer);
		this.inputType = inputType;
		this.beforeInputStatement = beforeInputStatement;
		this.inputVariable = inputVariable;
		this.afterInputStatement = afterInputStatement;
		this.min = min;
		this.max = max;
		this.variablesInStatement = new HashSet<String>();
	}
	
	
	public WoolReplyInput(WoolReplyInput woolReply) {
		super(woolReply.getReplyId(), woolReply.getNodePointer());
		this.beforeInputStatement = new String(woolReply.getBeforeInputStatement());
		this.inputVariable = new String(woolReply.getInputVariable());
		this.afterInputStatement = new String(woolReply.getAfterInputStatement());
		this.min = woolReply.getMin();
		this.max = woolReply.getMax();
		this.variablesInStatement = new HashSet<String>();
		for(String s : woolReply.getVariablesInStatement()) {
			this.variablesInStatement.add(new String(s));
		}
		this.variablesToSet = new HashMap<String, String>();
		for(String s : woolReply.getVariablesToSet().keySet()) {
			this.variablesToSet.put(new String(s), woolReply.getVariablesToSet().get(s));
		}
	}

	// ---------- Getters:

	/**
	 * Returns if this reply goes to a 'end dialogue' node.
	 * @return if this reply goes to a 'end dialogue' node.
	 */
	public boolean getEndsDialogue() {
		if (this.getNodePointer().getNodeId().equalsIgnoreCase("end")) {
			return true;
		}
		else return false;
	}
	
	/**
	 * Returns the type of input for the reply.
	 * @return the type of input for the reply.
	 */
	public InputType getInputType() {
		return this.inputType;
	}
	
	/**
	 * Returns the statement that should precede the input field.
	 * @return the statement that should precede the input field.
	 */
	public String getBeforeInputStatement() {
		return this.beforeInputStatement;
	}
	
	/**
	 * Returns the name of the variable that should store the value from the input field.
	 * @return the name of the variable that should store the value from the input field.
	 */
	public String getInputVariable() {
		return this.inputVariable;
	}
	
	/**
	 * Returns the statement that should follow the input field.
	 * @return the statement that should follow the input field.
	 */
	public String getAfterInputStatement() {
		return this.afterInputStatement;
	}
	
	public int getMin() {
		return this.min;
	}
	
	public int getMax() {
		return this.max;
	}

	public Set<String> getVariablesInStatement() {
		return this.variablesInStatement;
	}
	
	// ---------- Setters:
	
	/**
	 * Sets the input type for the reply.
	 * @param inputType the input type for the reply.
	 */
	public void setInputType(InputType inputType) {
		this.inputType = inputType;
	}
	
	/**
	 * Sets the statement that should precede the input field.
	 * @param beforeInputStatement the statement that should precede the input field.
	 */
	public void setBeforeInputStatement(String beforeInputStatement) {
		this.beforeInputStatement = beforeInputStatement;
	}
	
	/**
	 * Sets the name of the variable that should store the value from the input field.
	 * @param the name of the variable that should store the value from the input field.
	 */
	public void setInputVariable(String inputVariable) {
		this.inputVariable = inputVariable;
	}
	
	/**
	 * Sets the statement that should follow the input field.
	 * @param beforeInputStatement the statement that should follow the input field.
	 */
	public void setAfterInputStatement(String afterInputStatement) {
		this.afterInputStatement = afterInputStatement;
	}
	
	public void setMin(int min) {
		this.min = min;
	}
	
	public void setMax(int max) {
		this.max = max;
	}
	
	public void setVariablesInStatement(Set<String> variables) {
		this.variablesInStatement = variables;
	}
	
	public void addVariableInStatement(String variable) {
		this.variablesInStatement.add(variable);
	}
	
	public void removeVariableInStatement(String variable) {
		this.variablesInStatement.remove(variable);
	}
	
	// ---------- Functions:

	@Override
	public String toString() {
		String result = "WoolReplyInput of type " + this.inputType.toString() + " ("+this.getReplyId()+") [[\""+this.getBeforeInputStatement() + "<<TextInput->" + this.getInputVariable() + " min=" + this.min + " max=" + this.max + ">>" + this.getAfterInputStatement() +"\" -> "+this.getNodePointer().toFriendlyString()+"]]";
		return result;
	}

	@Override
	public String toFriendlyString() {
		String result = "[[\"" + this.getBeforeInputStatement() + this.inputType.toString() + "->" + this.getInputVariable() + this.getAfterInputStatement() + "\"]]";
		return result;
	}
	
}
