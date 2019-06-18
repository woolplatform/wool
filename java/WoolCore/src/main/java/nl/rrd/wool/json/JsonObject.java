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

package nl.rrd.wool.json;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This class has a toMap() method that uses Jackson to convert the object to a
 * map and a toString() method that returns the simple class name and the map
 * string. It also implements hashCode() and equals() using the map. Extending
 * this class is an easy way to get a meaningful toString(). If extending is not
 * possible, you may use the static toString() method in this class.
 * 
 * @author Dennis Hofs (RRD)
 */
public class JsonObject {
	@Override
	public int hashCode() {
		return toMap().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj.getClass() != getClass())
			return false;
		JsonObject other = (JsonObject)obj;
		return toMap().equals(other.toMap());
	}

	@Override
	public String toString() {
		return toString(this);
	}

	/**
	 * Returns this object as a map.
	 *
	 * @return the map
	 */
	public Map<?,?> toMap() {
		return toMap(this);
	}

	/**
	 * Returns the string representation for the specified object.
	 * 
	 * @param obj the object
	 * @return the string representation
	 */
	public static String toString(Object obj) {
		ObjectMapper mapper = new ObjectMapper();
		LinkedHashMap<?,?> map = mapper.convertValue(obj, LinkedHashMap.class);
		return obj.getClass().getSimpleName() + " " + map;
	}

	/**
	 * Returns the specified object as a map.
	 *
	 * @param obj the object
	 * @return the map
	 */
	public static Map<?,?> toMap(Object obj) {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.convertValue(obj, LinkedHashMap.class);
	}
}
