/*
 * Copyright 2019-2022 WOOL Foundation.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package eu.woolplatform.wool.execution;

import java.time.ZonedDateTime;
import java.util.*;

public abstract class WoolVariableStoreChange {

	/**
	 * An implementation of {@link WoolVariableStoreChange} representing a set of added {@link WoolVariable}s.
	 *
	 * @author Dennis Hofs
	 * @author Harm op den Akker
	 */
	public static class Put extends WoolVariableStoreChange {
		private Map<String,WoolVariable> variables;

		/**
		 * Creates an instance of a {@link Put} {@link WoolVariableStoreChange} with a given map of {@link WoolVariable}s.
		 * @param variables the mapping from variable name to {@link WoolVariable} that was added in this {@link WoolVariableStoreChange}.
		 */
		public Put(Map<String,WoolVariable> variables) {
			this.variables = variables;
		}

		/**
		 * Creates an instance of a {@link Put} {@link WoolVariableStoreChange} with a given {@code variableName}, {@code variableValues},
		 * and {@code lastUpdated} timestamp in the timezone of the WOOL user.
		 * @param variableName the name of the {@link WoolVariable} representing this Put change.
		 * @param variableValue the value of the {@link WoolVariable} representing this Put change.
		 * @param lastUpdated the timestamp (in the timezone of the WOOL user) representing this Put change.
		 */
		public Put(String variableName, Object variableValue, ZonedDateTime lastUpdated) {
			Map<String,WoolVariable> variables = new LinkedHashMap<>();
			variables.put(variableName, new WoolVariable(variableName,variableValue,lastUpdated));
			this.variables = variables;
		}

		/**
		 * Creates an instance of a {@link Put} {@link WoolVariableStoreChange} with a single given {@link WoolVariable}.
		 * @param woolVariable the one and only {@link WoolVariable} that was added in this {@link WoolVariableStoreChange}.
		 */
		public Put(WoolVariable woolVariable) {
			Map<String,WoolVariable> variables = new LinkedHashMap<>();
			variables.put(woolVariable.getName(), woolVariable);
			this.variables = variables;
		}

		/**
		 * Creates an instance of a {@link Put} {@link WoolVariableStoreChange} with a list of given {@link WoolVariable}s.
		 * @param woolVariables the list of {@link WoolVariable}s that were added in this {@link WoolVariableStoreChange}.
		 */
		public Put(List<WoolVariable> woolVariables) {
			Map<String,WoolVariable> variables = new LinkedHashMap<>();
			for(WoolVariable woolVariable : woolVariables) {
				variables.put(woolVariable.getName(), woolVariable);
			}
			this.variables = variables;
		}

		/**
		 * Returns the mapping of variable name to {@link WoolVariable} representing all the variables that
		 * have been added in this {@link WoolVariableStoreChange}.
		 * @return the added WOOL Variables.
		 */
		public Map<String,WoolVariable> getVariables() {
			return variables;
		}

	}

	/**
	 * An implementation of {@link WoolVariableStoreChange} representing a set of removed WOOL Variables, identified
	 * by their variable names.
	 *
	 * @author Dennis Hofs
	 * @author Harm op den Akker
	 */
	public static class Remove extends WoolVariableStoreChange {
		private Collection<String> variableNames;

		/**
		 * Creates an instance of a {@link Remove} {@link WoolVariableStoreChange} with a given collection of variableNames.
		 * @param variableNames the names of the variables that have been removed in this change.
		 */
		public Remove(Collection<String> variableNames) {
			this.variableNames = variableNames;
		}

		/**
		 * Creates an instance of a {@link Remove} {@link WoolVariableStoreChange} with a given single variable name, representing
		 * the variable that was removed with this change.
		 * @param variableName the name of the variable that was removed with this change.
		 */
		public Remove(String variableName) {
			variableNames = Collections.singletonList(variableName);
		}

		/**
		 * Returns the collection of variable names that are associated with this {@link Remove} {@link WoolVariableStoreChange}.
		 * @return the collection of variable names of variables that have been removed.
		 */
		public Collection<String> getVariableNames() {
			return variableNames;
		}
	}

	/**
	 * An implementation of {@link WoolVariableStoreChange} representing a full clear of the {@link WoolVariableStore}.
	 *
	 * @author Dennis Hofs
	 * @author Harm op den Akker
	 */
	public static class Clear extends WoolVariableStoreChange {

	}
}
