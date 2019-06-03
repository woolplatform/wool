package nl.rrd.wool.model.statement;

public class WoolStatementCommandSet implements WoolStatement {

	private String variableName;
	private String variableValue;
	
	// ---------- Constructors:
	
	public WoolStatementCommandSet(String variableName, String variableValue) {
		this.variableName = variableName;
		this.variableValue = variableValue;
	}
	
	// ---------- Getters:
	
	public String getVariableName() {
		return this.variableName;
	}
	
	public String getVariableValue() {
		return this.variableValue;
	}
	

	// ---------- Setters:
	
	public void setVariableName(String variableName) {
		this.variableName = variableName;
	}

	public void setVariableValue(String variableValue) {
		this.variableValue = variableValue;
	}
	
	// ---------- Utility:
	
	public String toString() {
		String result = "";
		result += "WoolStatementCommandSet: \"Set "+variableName+" to "+variableValue+"\".";
		return result;
	}
	
	public String toFriendlyString() {
		String result = "";
		result += "Set "+variableName+" to "+variableValue+".";
		return result;
	}
}