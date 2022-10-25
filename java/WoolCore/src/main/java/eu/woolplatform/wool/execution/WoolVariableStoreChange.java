/*
 * Copyright 2019-2022 WOOL Foundation - Licensed under the MIT License:
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package eu.woolplatform.wool.execution;

import java.time.ZonedDateTime;
import java.util.*;

public abstract class WoolVariableStoreChange {

	/**
	 * Defines a set of possible source of changes to the WOOL Variable Store.
	 */
	public enum Source {
		UNKNOWN,
		WOOL_SCRIPT,
		INPUT_REPLY,
		WEB_SERVICE,
		EXTERNAL_VARIABLE_SERVICE
	}

	private final ZonedDateTime time;
	private final Source source;

	/**
	 * Creates an instance of a {@link WoolVariableStoreChange} with a given {@code time} and
	 * {@code source}.
	 * @param time the time that this change took place (in the time zone of the user).
	 * @param source the source of the WOOL Variable Store change, as one of {@link Source}.
	 */
	public WoolVariableStoreChange(ZonedDateTime time, Source source) {
		this.time = time;
		this.source = source;
	}

	/**
	 * Returns the {@link Source} of this change to the WOOL Variable Store.
	 * @return the {@link Source} of this change to the WOOL Variable Store.
	 */
	public Source getSource() {
		return source;
	}

	/**
	 * Returns the time that this change took place (in the time zone of the user).
	 * @return the time that this change took place (in the time zone of the user).
	 */
	public ZonedDateTime getTime() {
		return time;
	}

	/**
	 * An implementation of {@link WoolVariableStoreChange} representing a set of added
	 * {@link WoolVariable}s.
	 *
	 * @author Dennis Hofs
	 * @author Harm op den Akker
	 */
	public static class Put extends WoolVariableStoreChange {
		private final Map<String,Object> variables;

		/**
		 * Creates an instance of a {@link Put} {@link WoolVariableStoreChange} with a given map of
		 * {@link WoolVariable}s.
		 * @param woolVariablesMap the mapping from variable name to {@link WoolVariable} that was
		 *                         added in this {@link WoolVariableStoreChange}.
		 * @param time the time that this change took place (in the time zone of the user).
		 */
		public Put(Map<String,WoolVariable> woolVariablesMap, ZonedDateTime time) {
			super(time,Source.UNKNOWN);
			variables = new LinkedHashMap<>();
			for(WoolVariable woolVariable : woolVariablesMap.values()) {
				variables.put(woolVariable.getName(),woolVariable.getValue());
			}
		}

		/**
		 * Creates an instance of a {@link Put} {@link WoolVariableStoreChange} with a given map of
		 * {@link WoolVariable}s.
		 * @param woolVariablesMap the mapping from variable name to {@link WoolVariable} that was
		 *                         added in this {@link WoolVariableStoreChange}.
		 * @param time the time that this change took place (in the time zone of the user).
		 * @param source the source of the change to the variable store.
		 */
		public Put(Map<String,WoolVariable> woolVariablesMap, ZonedDateTime time, Source source) {
			super(time,source);
			variables = new LinkedHashMap<>();
			for(WoolVariable woolVariable : woolVariablesMap.values()) {
				variables.put(woolVariable.getName(),woolVariable.getValue());
			}
		}

		/**
		 * Creates an instance of a {@link Put} {@link WoolVariableStoreChange} with a given
		 * {@code variableName}, {@code variableValues}, and {@code lastUpdated} timestamp in the
		 * timezone of the WOOL user.
		 * @param variableName the name of the {@link WoolVariable} representing this Put change.
		 * @param variableValue the value of the {@link WoolVariable} representing this Put change.
		 * @param time the time that this change took place (in the time zone of the user).
		 */
		public Put(String variableName, Object variableValue, ZonedDateTime time) {
			super(time, Source.UNKNOWN);
			variables = new LinkedHashMap<>();
			variables.put(variableName, variableValue);
		}

		/**
		 * Creates an instance of a {@link Put} {@link WoolVariableStoreChange} with a given
		 * {@code variableName}, {@code variableValues}, and {@code lastUpdated} timestamp in the
		 * timezone of the WOOL user.
		 * @param variableName the name of the {@link WoolVariable} representing this Put change.
		 * @param variableValue the value of the {@link WoolVariable} representing this Put change.
		 * @param time the time that this change took place (in the time zone of the user).
		 * @param source the source of the change to the variable store.
		 */
		public Put(String variableName, Object variableValue, ZonedDateTime time, Source source) {
			super(time, source);
			variables = new LinkedHashMap<>();
			variables.put(variableName, variableValue);
		}

		/**
		 * Creates an instance of a {@link Put} {@link WoolVariableStoreChange} with a single given
		 * {@link WoolVariable}.
		 * @param woolVariable the one and only {@link WoolVariable} that was added in this
		 *                     {@link WoolVariableStoreChange}.
		 * @param time the time that this change took place (in the time zone of the user).
		 */
		public Put(WoolVariable woolVariable, ZonedDateTime time) {
			super(time, Source.UNKNOWN);
			variables = new LinkedHashMap<>();
			variables.put(woolVariable.getName(), woolVariable.getValue());
		}

		/**
		 * Creates an instance of a {@link Put} {@link WoolVariableStoreChange} with a single given
		 * {@link WoolVariable}.
		 * @param woolVariable the one and only {@link WoolVariable} that was added in this
		 *                     {@link WoolVariableStoreChange}.
		 * @param time the time that this change took place (in the time zone of the user).
		 * @param source the source of the change to the variable store.
		 */
		public Put(WoolVariable woolVariable, ZonedDateTime time, Source source) {
			super(time, source);
			variables = new LinkedHashMap<>();
			variables.put(woolVariable.getName(), woolVariable.getValue());
		}

		/**
		 * Creates an instance of a {@link Put} {@link WoolVariableStoreChange} with a list of given
		 * {@link WoolVariable}s.
		 * @param woolVariablesList the list of {@link WoolVariable}s that were added in this
		 *                          {@link WoolVariableStoreChange}.
		 * @param time the time that this change took place (in the time zone of the user).
		 */
		public Put(List<WoolVariable> woolVariablesList, ZonedDateTime time) {
			super(time, Source.UNKNOWN);
			variables = new LinkedHashMap<>();
			for(WoolVariable woolVariable : woolVariablesList) {
				variables.put(woolVariable.getName(), woolVariable.getValue());
			}
		}

		/**
		 * Creates an instance of a {@link Put} {@link WoolVariableStoreChange} with a list of given
		 * {@link WoolVariable}s.
		 * @param woolVariablesList the list of {@link WoolVariable}s that were added in this
		 *                          {@link WoolVariableStoreChange}.
		 * @param time the time that this change took place (in the time zone of the user).
		 * @param source the source of the change to the variable store.
		 */
		public Put(List<WoolVariable> woolVariablesList, ZonedDateTime time, Source source) {
			super(time, source);
			variables = new LinkedHashMap<>();
			for(WoolVariable woolVariable : woolVariablesList) {
				variables.put(woolVariable.getName(), woolVariable.getValue());
			}
		}

		/**
		 * Returns the mapping of variable name to value ({@link Object}) representing all the
		 * variables that have been added in this {@link WoolVariableStoreChange}.
		 * @return the added WOOL Variables.
		 */
		public Map<String,Object> getVariables() {
			return variables;
		}

	}

	/**
	 * An implementation of {@link WoolVariableStoreChange} representing a set of removed WOOL
	 * Variables, identified by their variable names.
	 *
	 * @author Dennis Hofs
	 * @author Harm op den Akker
	 */
	public static class Remove extends WoolVariableStoreChange {
		private final Collection<String> removedVariableNames;

		/**
		 * Creates an instance of a {@link Remove} {@link WoolVariableStoreChange} with a given
		 * collection of variableNames.
		 * @param variableNames the names of the variables that have been removed in this change.
		 * @param time the time that this change took place (in the time zone of the user).
		 */
		public Remove(Collection<String> variableNames, ZonedDateTime time) {
			super(time, Source.UNKNOWN);
			this.removedVariableNames = variableNames;
		}

		/**
		 * Creates an instance of a {@link Remove} {@link WoolVariableStoreChange} with a given
		 * collection of variableNames.
		 * @param variableNames the names of the variables that have been removed in this change.
		 * @param time the time that this change took place (in the time zone of the user).
		 * @param source the source of the change to the variable store.
		 */
		public Remove(Collection<String> variableNames, ZonedDateTime time, Source source) {
			super(time, source);
			this.removedVariableNames = variableNames;
		}

		/**
		 * Creates an instance of a {@link Remove} {@link WoolVariableStoreChange} with a given
		 * single variable name, representing the variable that was removed with this change.
		 * @param variableName the name of the variable that was removed with this change.
		 * @param time the time that this change took place (in the time zone of the user).
		 */
		public Remove(String variableName, ZonedDateTime time) {
			super(time, Source.UNKNOWN);
			removedVariableNames = Collections.singletonList(variableName);
		}

		/**
		 * Creates an instance of a {@link Remove} {@link WoolVariableStoreChange} with a given
		 * single variable name, representing the variable that was removed with this change.
		 * @param variableName the name of the variable that was removed with this change.
		 * @param time the time that this change took place (in the time zone of the user).
		 * @param source the source of the change to the variable store.
		 */
		public Remove(String variableName, ZonedDateTime time, Source source) {
			super(time, source);
			removedVariableNames = Collections.singletonList(variableName);
		}

		/**
		 * Returns the collection of variable names that are associated with this {@link Remove}
		 * {@link WoolVariableStoreChange}.
		 * @return the collection of variable names of variables that have been removed.
		 */
		public Collection<String> getVariableNames() {
			return removedVariableNames;
		}

	}

	/**
	 * An implementation of {@link WoolVariableStoreChange} representing a full clear of the
	 * {@link WoolVariableStore}.
	 *
	 * @author Dennis Hofs
	 * @author Harm op den Akker
	 */
	public static class Clear extends WoolVariableStoreChange {

		/**
		 * Creates an instance of a {@link Clear} {@link WoolVariableStoreChange} indicating a full
		 * clear (removed all variables) of the WOOL Variable Store.
		 * @param time the time that this change took place (in the time zone of the user).
		 */
		public Clear(ZonedDateTime time) {
			super(time,Source.UNKNOWN);
		}

		/**
		 * Creates an instance of a {@link Clear} {@link WoolVariableStoreChange} indicating a full
		 * clear (removed all variables) of the WOOL Variable Store.
		 * @param time the time that this change took place (in the time zone of the user).
		 * @param source the source of the change to the variable store.
		 */
		public Clear(ZonedDateTime time, Source source) {
			super(time,source);
		}

	}
}
