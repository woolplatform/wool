package nl.rrd.wool.execution;

import java.util.Observable;

import nl.rrd.wool.exception.WoolUnknownVariableException;


/**
 * Interface class for {@link WoolVariableStore}'s. A {@link WoolVariableStore} implementation acts
 * as an interface between Wool scripts (and the variables used in e.g. "if" and "set" statements therein) and
 * the dialogue execution engine. 
 * 
 * @author Harm op den Akker
 * @see {@link DefaultWoolVariableStore},
 */
public abstract class WoolVariableStore extends Observable {
	
	public enum VariableSource {
		CORE, EXTERNAL;
	}
	
	public class StringVariable {
		public String variableName;
		public String variableValue;
		
		public StringVariable(String variableName, String variableValue) {
			this.variableName = variableName;
			this.variableValue = variableValue;
		}
		
		public String getVariableName() {
			return this.variableName;
		}
		
		public String getVariableValue() { 
			return this.variableValue;
		}
	}
	
	/**
	 * Stores the given {@code value} under the given {@name} in this {@link WoolVariableStore}.
	 * @param name the name of the variable to store.
	 * @param value the value of the variable to store.
	 */
	public abstract void setValue(String name, String value, VariableSource source);

	/**
	 * Retrieves the variable identified by the given {@code name}, or throws an {@link WoolUnknownVariableException} if no
	 * such variable is known.
	 * @param name the name of the variable to retrieve.
	 * @return the associated value of the variable as a {@link String}.
	 * @throws WoolUnknownVariableException in case no value is available for the given variable {@code name}.
	 */
	public abstract String getValue(String name) throws WoolUnknownVariableException;
	
}