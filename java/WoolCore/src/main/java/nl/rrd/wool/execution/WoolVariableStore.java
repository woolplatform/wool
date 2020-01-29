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

package nl.rrd.wool.execution;

import java.util.*;


/**
 * Interface class for {@link WoolVariableStore}'s. A {@link WoolVariableStore} implementation acts
 * as an interface between Wool scripts (and the variables used in e.g. "if" and "set" statements therein) and
 * the dialogue execution engine. 
 * 
 * @author Harm op den Akker
 */
public class WoolVariableStore {
	private Map<String,Object> variableMap = new HashMap<>();

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
	 * @param notify true if listeners should be notified of the change, false
	 * otherwise
	 */
	public void setValue(String name, Object value, boolean notify) {
		variableMap.put(name,value);
		if (notify)
			notifyOnChange(new WoolVariableStoreChange.Put(name, value));
	}

	/**
	 * Retrieves the variable identified by the given {@code name}, or returns
	 * null if no such variable is known.
	 * @param name the name of the variable to retrieve.
	 * @return the associated value of the variable ({@link String}, {@link Number}, {@link Boolean}, null)
	 */
	public Object getValue(String name) {
		return variableMap.get(name);
	}
	
	public Map<String,Object> getModifiableMap(boolean notifyOnChange) {
		return new SourceMap(notifyOnChange);
	}
	
	private class SourceMap implements Map<String,Object> {
		private boolean notifyOnChange;
		
		public SourceMap(boolean notifyOnChange) {
			this.notifyOnChange = notifyOnChange;
		}

		@Override
		public int size() {
			return variableMap.size();
		}

		@Override
		public boolean isEmpty() {
			return variableMap.isEmpty();
		}

		@Override
		public boolean containsKey(Object key) {
			return variableMap.containsKey(key);
		}

		@Override
		public boolean containsValue(Object value) {
			return variableMap.containsValue(value);
		}

		@Override
		public Object get(Object key) {
			return variableMap.get(key);
		}

		@Override
		public Object put(String key, Object value) {
			Object result = get(key);
			setValue(key, value, notifyOnChange);
			return result;
		}

		@Override
		public Object remove(Object key) {
			Object result = variableMap.remove(key);
			if (notifyOnChange)
				notifyOnChange(new WoolVariableStoreChange.Remove((String)key));
			return result;
		}

		@Override
		public void putAll(Map<? extends String, ?> m) {
			variableMap.putAll(m);
			if (notifyOnChange) {
				Map<String,Object> notifyMap = new LinkedHashMap<>(m);
				notifyOnChange(new WoolVariableStoreChange.Put(notifyMap));
			}
		}

		@Override
		public void clear() {
			variableMap.clear();
			if (notifyOnChange)
				notifyOnChange(new WoolVariableStoreChange.Clear());
		}

		@Override
		public Set<String> keySet() {
			return variableMap.keySet();
		}

		@Override
		public Collection<Object> values() {
			return variableMap.values();
		}

		@Override
		public Set<Entry<String, Object>> entrySet() {
			return variableMap.entrySet();
		}
	}

	public interface OnChangeListener {
		void onChange(WoolVariableStore varStore,
				List<WoolVariableStoreChange> changes);
	}
}
