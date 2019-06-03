package nl.rrd.wool.model.statement;

import java.util.HashSet;
import java.util.Set;


public class WoolStatementBasicIdentified implements WoolStatement {
	
	private String speaker;
	private String statement;
	private Set<String> variables;

	
	// ---------- Constructors:
	
	public WoolStatementBasicIdentified(String speaker, String statement) {
		this.speaker = speaker;
		this.statement = statement;
		this.variables = new HashSet<String>();
	}
	
	public WoolStatementBasicIdentified(WoolStatementBasicIdentified woolStatement) {
		super();
		this.speaker = new String(woolStatement.getSpeaker());
		this.statement = new String(woolStatement.getStatement());
		this.variables = new HashSet<String>();
		for(String s : woolStatement.getVariables()) {
			this.variables.add(new String(s));
		}
	}
	
	// ---------- Getters:
	
	public String getSpeaker() {
		return speaker;
	}
	
	public String getStatement() {
		return statement;
	}
	
	public Set<String> getVariables() {
		return variables;
	}
	
	// ---------- Setters:
	
	public void setSpeaker(String speaker) {
		this.speaker = speaker;
	}
	
	public void setStatement(String statement) {
		this.statement = statement;
	}
	
	public void setVariables(Set<String> variables) {
		this.variables = variables;
	}
	
	public void addVariable(String variable) {
		this.variables.add(variable);
	}
	
	public void removeVariable(String variable) {
		this.variables.remove(variable);
	}
	
	// ---------- Utility:
	
	public String toString() {
		String result = "";
		result += "WoolStatementBasicIdentified: "+this.getSpeaker()+": \""+this.getStatement()+"\".";
		return result;
	}
	
	public String toFriendlyString() {
		return this.getSpeaker()+": "+this.getStatement();
	}

}
