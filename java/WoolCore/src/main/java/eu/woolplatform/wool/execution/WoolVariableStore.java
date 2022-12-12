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

/**
 * A {@link WoolVariableStore} is an object that stores all WOOL variable values for a given
 * user.
 * 
 * @author Harm op den Akker
 */
public class WoolVariableStore {

	// Contains the list of all WoolVariables in this store
	private final Map<String,WoolVariable> woolVariables = new HashMap<>();

	// The WOOL user associated with this WoolVariableStore
	private WoolUser woolUser;

	// Contains the list of all WoolVariableChangeListeners that need to be notified for updates
	private final List<WoolVariableStoreOnChangeListener> onChangeListeners = new ArrayList<>();

	// --------------------------------------------------------
	// -------------------- Constructor(s) --------------------
	// --------------------------------------------------------

	/**
	 * Creates an instance of a new {@link WoolVariableStore} for a user in the given
	 * {@code timeZone}.
	 * @param woolUser the {@link WoolUser} associated with this {@link WoolVariableStore}.
	 */
	public WoolVariableStore(WoolUser woolUser) {
		this.woolUser = woolUser;
	}

	public WoolVariableStore(WoolUser woolUser, WoolVariable[] woolVariableArray) {
		this.woolUser = woolUser;
		synchronized(woolVariables) {
			for (WoolVariable variable : woolVariableArray) {
				woolVariables.put(variable.getName(),variable);
			}
		}
	}

	// ----------------------------------------------------------
	// -------------------- Listener Methods --------------------
	// ----------------------------------------------------------

	/**
	 * Adds the given {@link WoolVariableStoreOnChangeListener} to the list of listeners
	 * for this {@link WoolVariableStore}.
	 *
	 * @param listener a {@link WoolVariableStoreOnChangeListener} that should be notified whenever
	 *                 the WoolVariableStore is changed
	 */
	public void addOnChangeListener(WoolVariableStoreOnChangeListener listener) {
		synchronized (onChangeListeners) {
			onChangeListeners.add(listener);
		}
	}

	/**
	 * Removes the given {@link WoolVariableStoreOnChangeListener} from the list of listeners
	 * for this {@link WoolVariableStore}.
	 *
	 * @param listener a {@link WoolVariableStoreOnChangeListener} that was previously registered
	 *                 to listen for changes.
	 * @return {@code true} if the given {@link WoolVariableStoreOnChangeListener} was removed, or
	 *         {@code false} otherwise.
	 * if it was not registered as a listener to begin with.
	 */
	public boolean removeOnChangeListener(WoolVariableStoreOnChangeListener listener) {
		synchronized (onChangeListeners) {
			return onChangeListeners.remove(listener);
		}
	}

	/**
	 * Notifies all {@link WoolVariableStoreOnChangeListener} that are listening for changes to this
	 * {@link WoolVariableStore} of one or more changes as represented by the list of
	 * {@link WoolVariableStoreChange} {@code changes}.
	 *
	 * @param changes one or multiple {@link WoolVariableStoreChange}s representing a modification
	 *                to this {@link WoolVariableStore}.
	 */
	private void notifyOnChange(WoolVariableStoreChange... changes) {
		List<WoolVariableStoreOnChangeListener> listeners;
		synchronized (onChangeListeners) {
			listeners = new ArrayList<>(onChangeListeners);
		}
		for (WoolVariableStoreOnChangeListener listener : listeners) {
			listener.onChange(this, Arrays.asList(changes));
		}
	}

	// -----------------------------------------------------------
	// -------------------- Retrieval Methods --------------------
	// -----------------------------------------------------------

	/**
	 * Retrieves the variable identified by the given {@code name}, or returns
	 * {@code null} if no such variable is known in this {@link WoolVariableStore}.
	 *
	 * @param name the name of the variable to retrieve.
	 * @return the {@link WoolVariable} with the given {@code name}, nor {@code null}.
	 */
	public WoolVariable getWoolVariable(String name) {
		synchronized (woolVariables) {
			return woolVariables.get(name);
		}
	}

	/**
	 * Returns the contents of this {@link WoolVariableStore} as an array of {@link WoolVariable}s.
	 * @return the contents of this {@link WoolVariableStore} as an array of {@link WoolVariable}s.
	 */
	public WoolVariable[] getWoolVariables() {
		synchronized (woolVariables) {
			return woolVariables.values().toArray(new WoolVariable[0]);
		}
	}

	/**
	 * Returns the value of the variable identified by the given {@code name}.
	 * If no such variable is known in this {@link WoolVariableStore}, then this
	 * method returns null.
	 *
	 * <p>Note: if this method returns null, it can mean that the variable does
	 * not exist, or that the variable has value {@code null}. If you need to
	 * distinguish these two cases, you should call {@link
	 * #getWoolVariable(String) getWoolVariable()} </p>
	 *
	 * @param variableName the name of the variable to retrieve.
	 * @return the value of the variable, null if the variable does not exist
	 * or the variable value is null
	 */
	public Object getValue(String variableName) {
		WoolVariable variable;
		synchronized (woolVariables) {
			variable = woolVariables.get(variableName);
		}
		if (variable == null)
			return null;
		return variable.getValue();
	}

	/**
	 * Returns the {@link WoolUser} associated with this {@link WoolVariableStore}.
	 * @return the {@link WoolUser} associated with this {@link WoolVariableStore}.
	 */
	public WoolUser getWoolUser() {
		return woolUser;
	}

	/**
	 * Returns a set of all the names of {@link WoolVariable}s contained in this
	 * {@link WoolVariableStore}.
	 *
	 * @return a set of all the names of {@link WoolVariable}s contained in this
	 * {@link WoolVariableStore}.
	 */
	public Set<String> getWoolVariableNames() {
		return woolVariables.keySet();
	}

	public List<String> getSortedWoolVariableNames() {
		List<String> nameList = new ArrayList<>(woolVariables.keySet());
		Collections.sort(nameList);
		return nameList;
	}

	// --------------------------------------------------------------
	// -------------------- Modification Methods --------------------
	// --------------------------------------------------------------

	/**
	 * Stores the given {@code value} under the given variable-{@code name} in this
	 * {@link WoolVariableStore} and sets the updatedTime to {@code updatedTime}.
	 *
	 * @param name the name of the variable to store.
	 * @param value the value of the variable to store.
	 * @param notifyObservers true if observers of this {@link WoolVariableStore} should be
	 *                        notified about this update.
	 * @param eventTime the time (in the time zone of the user) of the event that triggered the
	 *                  update of this variable.
	 */
	public void setValue(String name, Object value, boolean notifyObservers,
						 ZonedDateTime eventTime) {
		setValue(name,value,notifyObservers,eventTime,WoolVariableStoreChange.Source.UNKNOWN);
	}

	/**
	 * Stores the given {@code value} under the given variable-{@code name} in this
	 * {@link WoolVariableStore} and sets the updatedTime to {@code updatedTime}.
	 *
	 * @param name the name of the variable to store.
	 * @param value the value of the variable to store.
	 * @param notifyObservers true if observers of this {@link WoolVariableStore} should be
	 *                        notified about this update.
	 * @param eventTime the time (in the time zone of the user) of the event that triggered the
	 *                  update of this variable.
	 * @param source the source of the update to this {@link WoolVariableStore}.
	 */
	public void setValue(String name, Object value, boolean notifyObservers,
						 ZonedDateTime eventTime, WoolVariableStoreChange.Source source) {
		synchronized (woolVariables) {
			WoolVariable woolVariable = new WoolVariable(name, value, eventTime);
			woolVariables.put(name,woolVariable);
			if (notifyObservers) {
				notifyOnChange(new WoolVariableStoreChange.Put(woolVariable, eventTime, source));
			}
		}
	}

	/**
	 * Remove the {@link WoolVariable} with the given {@code name} from this
	 * {@link WoolVariableStore}. This method returns the {@link WoolVariable} object that has been
	 * deleted, or {@code null} if the element to be deleted was not found.
	 *
	 * @param name the name of the {@link WoolVariable} to remove.
	 * @param notifyObservers true if observers of this {@link WoolVariableStore} should be
	 *                        notified about this update.
	 * @param eventTime the time (in the time zone of the user) of the event that triggered the
	 *                  removal of this variable
	 * @return the {@link WoolVariable} that was removed, or {@code null}.
	 */
	public WoolVariable removeByName(String name, boolean notifyObservers,
									 ZonedDateTime eventTime) {
		return removeByName(name,notifyObservers,eventTime,WoolVariableStoreChange.Source.UNKNOWN);
	}

	/**
	 * Remove the {@link WoolVariable} with the given {@code name} from this
	 * {@link WoolVariableStore}. This method returns the {@link WoolVariable} object that has been
	 * deleted, or {@code null} if the element to be deleted was not found.
	 *
	 * @param name the name of the {@link WoolVariable} to remove.
	 * @param notifyObservers true if observers of this {@link WoolVariableStore} should be
	 *                        notified about this update.
	 * @param eventTime the time (in the time zone of the user) of the event that triggered the
	 *                  removal of this variable
	 * @param source the source of the update to this {@link WoolVariableStore}.
	 * @return the {@link WoolVariable} that was removed, or {@code null}.
	 */
	public WoolVariable removeByName(String name, boolean notifyObservers,
									 ZonedDateTime eventTime,
									 WoolVariableStoreChange.Source source) {
		WoolVariable result;
		synchronized (woolVariables) {
			result = woolVariables.remove(name);
		}
		if(result == null) {
			return null;
		} else {
			if(notifyObservers) {
				notifyOnChange(new WoolVariableStoreChange.Remove(name, eventTime, source));
			}
			return result;
		}
	}

	/**
	 * Adds all the entries in the {@code variablesToAdd}-map as {@link WoolVariable}s to this
	 * {@link WoolVariableStore}. The {@code variablesToAdd}-map is treated as a mapping from
	 * variable names ({@link String}s) to variable values ({@link Object}s).
	 * @param variablesToAdd the {@link Map} of name-value pairs to add as {@link WoolVariable}s.
	 * @param notifyObservers true if observers of this {@link WoolVariableStore} should be
	 *                        notified about this update.
	 * @param eventTime the time of the event that triggered the addition of these variables
	 *                  (in the time zone of the user)
	 */
	public void addAll(Map<? extends String, ?> variablesToAdd, boolean notifyObservers,
					   ZonedDateTime eventTime) {
		addAll(variablesToAdd,notifyObservers,eventTime,WoolVariableStoreChange.Source.UNKNOWN);
	}

	/**
	 * Adds all the entries in the {@code variablesToAdd}-map as {@link WoolVariable}s to this
	 * {@link WoolVariableStore}. The {@code variablesToAdd}-map is treated as a mapping from
	 * variable names ({@link String}s) to variable values ({@link Object}s).
	 * @param variablesToAdd the {@link Map} of name-value pairs to add as {@link WoolVariable}s.
	 * @param notifyObservers true if observers of this {@link WoolVariableStore} should be
	 *                        notified about this update.
	 * @param eventTime the time of the event that triggered the addition of these variables
	 *                  (in the time zone of the user)
	 * @param source the source of the update to this {@link WoolVariableStore}.
	 */
	public void addAll(Map<? extends String, ?> variablesToAdd, boolean notifyObservers,
					   ZonedDateTime eventTime, WoolVariableStoreChange.Source source) {
		List<WoolVariable> woolVariablesToAdd = new ArrayList<>();

		for (Map.Entry<? extends String, ?> entry : variablesToAdd.entrySet()) {
			String name = entry.getKey();
			Object value = entry.getValue();
			WoolVariable woolVariable = new WoolVariable(name,value,eventTime);
			woolVariablesToAdd.add(woolVariable);
		}

		synchronized (woolVariables) {
			for(WoolVariable woolVariable : woolVariablesToAdd) {
				woolVariables.put(woolVariable.getName(),woolVariable);
			}
		}

		if (notifyObservers) {
			notifyOnChange(new WoolVariableStoreChange.Put(woolVariablesToAdd, eventTime, source));
		}
	}

	/**
	 * Sets the {@link WoolUser} for this {@link WoolVariableStore}.
	 * @param woolUser the {@link WoolUser} for this {@link WoolVariableStore}.
	 */
	public void setWoolUser(WoolUser woolUser) {
		this.woolUser = woolUser;
	}

	/**
	 * Returns a modifiable mapping of {@link String}s to {@link Object}s that is linked to the
	 * contents of this {@link WoolVariableStore}. The {@link Object} values in this map are the
	 * values of the stored {@link WoolVariable}s, so not the {@link WoolVariable}s themselves!
	 * This {@code Map<String,Object>} can be used as a regular map, but is actually a specific
	 * implementation for this variable store. All basic map operations on the resulting map are
	 * observable by the {@link WoolVariableStoreOnChangeListener}s that are registered to listen to
	 * this {@link WoolVariableStore}.
	 *
	 * <p>This "modifiable map" is used in the execution of WOOL Dialogues containing WOOL
	 * Variables, as the implementation relies on the
	 * {@link nl.rrd.utils.expressions.Expression} interface.</p>
	 *
	 * <p>In other words, if you are thinking "Man, I wish WoolVariableStore was just a simple
	 * mapping of variable names to values", use this method, and you can pretend that that is the
	 * case.</p>
	 *
	 * If {@code notifyObservers} is {@code true}, then any action that modifies the content of this
	 * {@link Map} will result in all listeners being notified.
	 *
	 * @param notifyObservers true if observers of this {@link WoolVariableStore} should be notified
	 *                        about updates to the Map.
	 * @param eventTime the time of the event that is causing the changes to this
	 *                  {@link WoolVariableStore} in the time zone of the user.
	 * @return the modifiable map
	 */
	public Map<String, Object> getModifiableMap(boolean notifyObservers, ZonedDateTime eventTime) {
		return new WoolVariableMap(notifyObservers, eventTime,
				WoolVariableStoreChange.Source.UNKNOWN);
	}

	/**
	 * See {@link #getModifiableMap(boolean, ZonedDateTime)}.
	 * @param notifyObservers true if observers of this {@link WoolVariableStore} should be notified
	 *                        about updates to the Map.
	 * @param eventTime the time of the event that is causing the changes to this
	 * 	                {@link WoolVariableStore} in the time zone of the user.
	 * @param source the source of the changes to this {@link WoolVariableStore}.
	 * @return the modifiable map
	 */
	public Map<String, Object> getModifiableMap(boolean notifyObservers, ZonedDateTime eventTime,
												WoolVariableStoreChange.Source source) {
		return new WoolVariableMap(notifyObservers, eventTime, source);
	}

	/**
	 * A {@link WoolVariableMap} is a Mapping from variable name to variable value and can be used
	 * as an observable and modifiable "view" of the {@link WoolVariableStore} whose changes are
	 * maintained within this {@link WoolVariableStore} object.
	 */
	private class WoolVariableMap implements Map<String, Object> {

		private final boolean notifyObservers;
		private final ZonedDateTime eventTime;
		private final WoolVariableStoreChange.Source source;

		// --------------------------------------------------------
		// -------------------- Constructor(s) --------------------
		// --------------------------------------------------------

		/**
		 * Creates an instance of a {@link WoolVariableMap} which is a mapping of Strings to Objects
		 * representing the contents of this {@link WoolVariableStore}.
		 * @param notifyObservers whether {@link WoolVariableStoreOnChangeListener}s should be
		 *                        notified of updates made to this {@link WoolVariableMap}.
		 * @param eventTime the timestamp that is passed along to all changes that are made in this
		 *                  {@link WoolVariableMap}.
		 */
		public WoolVariableMap(boolean notifyObservers, ZonedDateTime eventTime,
							   WoolVariableStoreChange.Source source) {
			this.notifyObservers = notifyObservers;
			this.eventTime = eventTime;
			this.source = source;
		}

		// --------------------------------------------------------------
		// -------------------- Modification Methods --------------------
		// --------------------------------------------------------------

		@Override
		public Object put(String key, Object value) {
			Object result = get(key);
			setValue(key, value, notifyObservers, eventTime, source);
			return result;
		}

		@Override
		public Object remove(Object key) {
			WoolVariable result = removeByName((String)key, notifyObservers, eventTime, source);
			if(result != null) return result.getValue();
			else return null;
		}

		@Override
		public void putAll(Map<? extends String, ?> variablesToAdd) {
			addAll(variablesToAdd,notifyObservers,eventTime, source);
		}

		@Override
		public void clear() {
			synchronized (woolVariables) {
				woolVariables.clear();
			}
			if (notifyObservers)
				notifyOnChange(new WoolVariableStoreChange.Clear(eventTime, source));
		}

		// -----------------------------------------------------------
		// -------------------- Retrieval Methods --------------------
		// -----------------------------------------------------------

		@Override
		public int size() {
			synchronized (woolVariables) {
				return woolVariables.size();
			}
		}

		@Override
		public boolean isEmpty() {
			synchronized (woolVariables) {
				return woolVariables.isEmpty();
			}
		}

		@Override
		public boolean containsKey(Object key) {
			synchronized (woolVariables) {
				return woolVariables.containsKey(key);
			}
		}

		@Override
		public Object get(Object key) {
			synchronized (woolVariables) {
				if(woolVariables.get(key) != null) {
					return woolVariables.get(key).getValue();
				} else return null;
			}
		}

		@Override
		public boolean containsValue(Object value) {
			synchronized (woolVariables) {
				for (Map.Entry<String,WoolVariable> entry : woolVariables.entrySet()) {
					if(entry.getValue().getValue().equals(value)) return true;
				}
				return false;
			}
		}

		@Override
		public Set<String> keySet() {
			synchronized (woolVariables) {
				return woolVariables.keySet();
			}
		}

		@Override
		public Collection<Object> values() {
			Collection<Object> objectCollection = new ArrayList<>();

			synchronized (woolVariables) {
				Collection<WoolVariable> woolVariableCollection = woolVariables.values();
				for(WoolVariable woolVariable : woolVariableCollection) {
					objectCollection.add(woolVariable.getValue());
				}
			}

			return objectCollection;
		}

		@Override
		public Set<Entry<String, Object>> entrySet() {
			Set<Entry<String,Object>> resultSet = new HashSet<>();

			synchronized (woolVariables) {
				Set<Entry<String,WoolVariable>> entrySet = woolVariables.entrySet();

				for(Entry<String,WoolVariable> entry : entrySet) {
					String key = entry.getKey();
					Object value = entry.getValue().getValue();
					Map.Entry<String,Object> newEntry = new AbstractMap.SimpleEntry<>(key, value);
					resultSet.add(newEntry);
				}
			}

			return resultSet;
		}
	}
}

