package nl.rrd.wool.model.reply;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import nl.rrd.wool.model.nodepointer.WoolNodePointer;

public class WoolReplyBasic extends WoolReply {

	private String statement;
	private Set<String> variables;
	
	public WoolReplyBasic(int replyId, WoolNodePointer nextNodePointer, String statement) {
		super(replyId, nextNodePointer);
		this.statement = statement;
		this.variables = new HashSet<String>();
	}
	
	public WoolReplyBasic(WoolReplyBasic woolReply) {
		super(woolReply.getReplyId(), woolReply.getNodePointer());
		this.statement = new String(woolReply.getStatement());
		this.variables = new HashSet<String>();
		for(String s : woolReply.getVariablesInStatement()) {
			this.variables.add(new String(s));
		}
		this.variablesToSet = new HashMap<String, String>();
		for (String s : woolReply.getVariablesToSet().keySet()) {
			this.variablesToSet.put(s, woolReply.getVariablesToSet().get(s));
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
	
	public String getStatement() {
		return statement;
	}
	
	public Set<String> getVariablesInStatement() {
		return variables;
	}
	
	// ---------- Setters:
	
	public void setStatement(String statement) {
		this.statement = statement;
	}

	public void setVariablesInStatement(Set<String> variables) {
		this.variables = variables;
	}
	
	public void addVariableInStatement(String variable) {
		this.variables.add(variable);
	}
	
	public void removeVariableInStatement(String variable) {
		this.variables.remove(variable);
	}
	
	// ---------- Functions:

	@Override
	public String toString() {
		String result = "WoolReplyBasic ("+this.getReplyId()+") [[\""+this.getStatement()+"\" -> "+this.getNodePointer().toFriendlyString()+"]]";
		return result;
	}
	
	@Override
	public String toFriendlyString() {
		String result = "[[\""+this.getStatement()+"\"]]";
		return result;
	}

}
