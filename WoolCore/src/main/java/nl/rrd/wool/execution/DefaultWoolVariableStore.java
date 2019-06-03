package nl.rrd.wool.execution;

import java.util.HashMap;
import java.util.Map;

import nl.rrd.wool.exception.WoolUnknownVariableException;

public class DefaultWoolVariableStore extends WoolVariableStore {
		
	private Map<String,String> variableMap;
	
	/**
	 * Creates an instance of an empty {@link DefaultWoolVariableStore}.
	 */
	public DefaultWoolVariableStore() {
		this.variableMap = new HashMap<String,String>();
	}
	
	@Override
	public void setValue(String name, String value, VariableSource variableSource) {
		variableMap.put(name,value);
		if (variableSource.equals(VariableSource.CORE)) {
			setChanged();
			notifyObservers(new StringVariable(name, value));
		}
	}

	@Override
	public String getValue(String name) throws WoolUnknownVariableException {
		if(variableMap.keySet().contains(name)) {
			return variableMap.get(name);
		} else {
			throw new WoolUnknownVariableException("No value known for variable '"+name+"'.");
		}
	}
	
	public Map<String,String> getVariableMap() {
		return this.variableMap;
	}
}
