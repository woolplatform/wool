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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Set;


/**
 * Interface class for {@link WoolVariableStore}'s. A {@link WoolVariableStore} implementation acts
 * as an interface between Wool scripts (and the variables used in e.g. "if" and "set" statements therein) and
 * the dialogue execution engine. 
 * 
 * @author Harm op den Akker
 */
public class WoolVariableStore extends Observable {
	private Map<String,Object> variableMap = new HashMap<>();
	
	public enum VariableSource {
		CORE, EXTERNAL;
	}
	
	public class Variable {
		private String name;
		private Object value;
		
		public Variable(String name, Object value) {
			this.name = name;
			this.value = value;
		}
		
		public String getVariableName() {
			return name;
		}
		
		public Object getVariableValue() {
			return value;
		}
	}
	
	/**
	 * Stores the given {@code value} under the given {@code name} in this
	 * {@link WoolVariableStore}.
	 *
	 * @param name the name of the variable to store.
	 * @param value the value of the variable to store.
	 * @param source the source (core or external)
	 */
	public void setValue(String name, Object value, VariableSource source) {
		variableMap.put(name,value);
		if (source == VariableSource.CORE) {
			setChanged();
			notifyObservers(new Variable(name, value));
		}
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
	
	public Map<String,Object> getModifiableMap(VariableSource source) {
		return new SourceMap(source);
	}
	
	private class SourceMap implements Map<String,Object> {
		private VariableSource source;
		
		public SourceMap(VariableSource source) {
			this.source = source;
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
			setValue(key, value, source);
			return result;
		}

		@Override
		public Object remove(Object key) {
			return variableMap.remove(key);
		}

		@Override
		public void putAll(Map<? extends String, ? extends Object> m) {
			for (String key : m.keySet()) {
				setValue(key, m.get(key), source);
			}
		}

		@Override
		public void clear() {
			variableMap.clear();
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
}
