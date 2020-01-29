package nl.rrd.wool.execution;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class WoolVariableStoreChange {
	public static class Put extends WoolVariableStoreChange {
		private Map<String,?> variables;

		public Put(Map<String,?> variables) {
			this.variables = variables;
		}

		public Put(String name, Object value) {
			Map<String,Object> variables = new LinkedHashMap<>();
			variables.put(name, value);
			this.variables = variables;
		}

		public Map<String,?> getVariables() {
			return variables;
		}
	}

	public static class Remove extends WoolVariableStoreChange {
		private Collection<String> variableNames;

		public Remove(Collection<String> variableNames) {
			this.variableNames = variableNames;
		}

		public Remove(String variableName) {
			variableNames = Collections.singletonList(variableName);
		}

		public Collection<String> getVariableNames() {
			return variableNames;
		}
	}

	public static class Clear extends WoolVariableStoreChange {
	}
}
