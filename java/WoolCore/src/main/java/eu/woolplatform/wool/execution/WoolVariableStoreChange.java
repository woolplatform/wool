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

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class WoolVariableStoreChange {
	public static class Put extends WoolVariableStoreChange {
		private Map<String,?> variables;
		private DateTime time;

		public Put(Map<String,?> variables, DateTime time) {
			this.variables = variables;
			this.time = time;
		}

		public Put(String name, Object value, DateTime time) {
			Map<String,Object> variables = new LinkedHashMap<>();
			variables.put(name, value);
			this.variables = variables;
			this.time = time;
		}

		public Map<String,?> getVariables() {
			return variables;
		}

		public DateTime getTime() {
			return time;
		}
	}

	public static class Remove extends WoolVariableStoreChange {
		private Collection<String> variableNames;
		private DateTime time;

		public Remove(Collection<String> variableNames, DateTime time) {
			this.variableNames = variableNames;
			this.time = time;
		}

		public Remove(String variableName, DateTime time) {
			variableNames = Collections.singletonList(variableName);
			this.time = time;
		}

		public Collection<String> getVariableNames() {
			return variableNames;
		}

		public DateTime getTime() {
			return time;
		}
	}

	public static class Clear extends WoolVariableStoreChange {
		private DateTime time;

		public Clear(DateTime time) {
			this.time = time;
		}

		public DateTime getTime() {
			return time;
		}
	}
}
