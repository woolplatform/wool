/*
 * Copyright 2019 Roessingh Research and Development.
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

import org.joda.time.DateTime;

import java.util.*;


/**
 * Interface class for {@link WoolVariableStore}'s. A {@link WoolVariableStore} implementation acts
 * as an interface between Wool scripts (and the variables used in e.g. "if" and "set" statements therein) and
 * the dialogue execution engine. 
 * 
 * @author Harm op den Akker
 */
public class WoolVariableStore {
	private final Map<String,Object> variableMap = new HashMap<>();

	private final List<OnChangeListener> onChangeListeners = new ArrayList<>();

	public void addOnChangeListener(OnChangeListener listener) {
		synchronized (onChangeListeners) {
			onChangeListeners.add(listener);
		}
	}

	public void removeOnChangeListener(OnChangeListener listener) {
		synchronized (onChangeListeners) {
			onChangeListeners.remove(listener);
		}
	}

	private void notifyOnChange(WoolVariableStoreChange... changes) {
		List<OnChangeListener> ls;
		synchronized (onChangeListeners) {
			ls = new ArrayList<>(onChangeListeners);
		}
		for (OnChangeListener l : ls) {
			l.onChange(this, Arrays.asList(changes));
		}
	}

	/**
	 * Stores the given {@code value} under the given {@code name} in this
	 * {@link WoolVariableStore}.
	 *
	 * @param name the name of the variable to store.
	 * @param value the value of the variable to store.
	 * @param save true if the variable should be saved in the database, false
	 * if it can be kept just in memory
	 * @param time the time (in the time zone of the user) that should be stored
	 * with this value. This is ignored and can be null if "save" is false
	 */
	public void setValue(String name, Object value, boolean save,
			DateTime time) {
		synchronized (variableMap) {
			variableMap.put(name, value);
		}
		if (save)
			notifyOnChange(new WoolVariableStoreChange.Put(name, value, time));
	}

	/**
	 * Retrieves the variable identified by the given {@code name}, or returns
	 * null if no such variable is known.
	 * @param name the name of the variable to retrieve.
	 * @return the associated value of the variable ({@link String}, {@link Number}, {@link Boolean}, null)
	 */
	public Object getValue(String name) {
		synchronized (variableMap) {
			return variableMap.get(name);
		}
	}

	/**
	 * Returns a modifiable map for this variable store. If "save" is true, then
	 * any modification is saved to the database with the specified time.
	 *
	 * @param save true if the variable should be saved in the database, false
	 * if it can be kept just in memory
	 * @param time the time in the time zone of the user. This is ignored and
	 * can be null if "save" is false
	 * @return the modifiable map
	 */
	public Map<String,Object> getModifiableMap(boolean save, DateTime time) {
		return new SourceMap(save, time);
	}
	
	private class SourceMap implements Map<String,Object> {
		private boolean save;
		private DateTime time;

		public SourceMap(boolean save, DateTime time) {
			this.save = save;
			this.time = time;
		}

		@Override
		public int size() {
			synchronized (variableMap) {
				return variableMap.size();
			}
		}

		@Override
		public boolean isEmpty() {
			synchronized (variableMap) {
				return variableMap.isEmpty();
			}
		}

		@Override
		public boolean containsKey(Object key) {
			synchronized (variableMap) {
				return variableMap.containsKey(key);
			}
		}

		@Override
		public boolean containsValue(Object value) {
			synchronized (variableMap) {
				return variableMap.containsValue(value);
			}
		}

		@Override
		public Object get(Object key) {
			synchronized (variableMap) {
				return variableMap.get(key);
			}
		}

		@Override
		public Object put(String key, Object value) {
			Object result = get(key);
			setValue(key, value, save, time);
			return result;
		}

		@Override
		public Object remove(Object key) {
			Object result;
			synchronized (variableMap) {
				result = variableMap.remove(key);
			}
			if (save) {
				notifyOnChange(new WoolVariableStoreChange.Remove((String) key,
						time));
			}
			return result;
		}

		@Override
		public void putAll(Map<? extends String, ?> m) {
			synchronized (variableMap) {
				variableMap.putAll(m);
			}
			if (save) {
				Map<String, Object> notifyMap = new LinkedHashMap<>(m);
				notifyOnChange(new WoolVariableStoreChange.Put(notifyMap, time));
			}
		}

		@Override
		public void clear() {
			synchronized (variableMap) {
				variableMap.clear();
			}
			if (save)
				notifyOnChange(new WoolVariableStoreChange.Clear(time));
		}

		@Override
		public Set<String> keySet() {
			synchronized (variableMap) {
				return variableMap.keySet();
			}
		}

		@Override
		public Collection<Object> values() {
			synchronized (variableMap) {
				return variableMap.values();
			}
		}

		@Override
		public Set<Entry<String, Object>> entrySet() {
			synchronized (variableMap) {
				return variableMap.entrySet();
			}
		}
	}

	public interface OnChangeListener {
		void onChange(WoolVariableStore varStore,
				List<WoolVariableStoreChange> changes);
	}
}
