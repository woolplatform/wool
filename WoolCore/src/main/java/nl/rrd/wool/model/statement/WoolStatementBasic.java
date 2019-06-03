package nl.rrd.wool.model.statement;

import java.util.HashSet;
import java.util.Set;

public class WoolStatementBasic implements WoolStatement {
	
	private String statement;
	private Set<String> variables;
	
	// ---------- Constructors:
	
	public WoolStatementBasic(String statement) {
		super();
		this.statement = statement;
		this.variables = new HashSet<String>();
	}
	
	public WoolStatementBasic(WoolStatementBasic woolStatement) {
		super();
		this.statement = new String(woolStatement.getStatement());
		this.variables = new HashSet<String>();
		for(String s : woolStatement.getVariables()) {
			this.variables.add(new String(s));
		}
	}
	
	// ---------- Getters:
	
	public String getStatement() {
		return statement;
	}
	
	public Set<String> getVariables() {
		return variables;
	}
	
	// ---------- Setters:

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
		result += "WoolStatementBasic: \""+this.getStatement()+"\".";
		return result;
	}
	
	public String toFriendlyString() {
		return this.getStatement();
	}
	
}
