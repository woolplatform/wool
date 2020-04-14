package eu.woolplatform.utils;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.woolplatform.utils.json.JsonMapper;

/**
 * This class can format maps, lists and primitives (boolean, number, string).
 * The output can be use human-friendly formatting with new lines and
 * indentation. And the output can be JSON or it can use Java toString(). If
 * the output should be JSON, any object that is not a map, list or primitive,
 * will be converted to a map first.
 * 
 * @author Dennis Hofs (RRD)
 */
public class DataFormatter {
	private static final String INDENT = "    ";

	private ObjectMapper mapper = new ObjectMapper();
	
	/**
	 * Returns a string representation of the specified value (map, list or
	 * primitive). If "human" is true, the returned string will have a friendly
	 * formatting, possibly spanning multiple lines.
	 * 
	 * <p>The output of primitives will use Java toString(). See also {@link
	 * #format(Object, boolean, boolean) format(value, human, json)}.</p>
	 *
	 * @param value the value to format
	 * @param human true for friendly formatting, false for single-line
	 * formatting
	 * @return the string
	 */
	public String format(Object value, boolean human) {
		return valueToString(value, human, false, 0);
	}

	/**
	 * Returns a string representation of the specified value (map, list or
	 * primitive). If "human" is true, the returned string will have a friendly
	 * formatting, possibly spanning multiple lines.
	 *
	 * @param value the value to format
	 * @param human true for friendly formatting, false for single-line
	 * formatting
	 * @param json true if the output should be JSON, false if the output should
	 * use Java toString()
	 * @return the string
	 */
	public String format(Object value, boolean human, boolean json) {
		return valueToString(value, human, json, 0);
	}
	
	/**
	 * Returns a string representation of the specified value (map, list or
	 * primitive). If "human" is true, the returned string will have a friendly
	 * formatting, possibly spanning multiple lines.
	 * 
	 * @param value the value to format
	 * @param human true for friendly formatting, false for single-line
	 * formatting
	 * @param json true if the output should be JSON, false if the output should
	 * use Java toString()
	 * @param indent the number of times to indent each line except the first
	 * line
	 * @return the string
	 */
	private String valueToString(Object value, boolean human, boolean json,
			int indent) {
		if (value == null)
			return "null";
		boolean isPrimitive = value instanceof String ||
				value instanceof Number || value instanceof Boolean;
		if (isPrimitive) {
			if (json)
				return JsonMapper.generate(value);
			else
				return value.toString();
		} else if (value instanceof List) {
			return listToString((List<?>)value, human, json, indent);
		} else if (value instanceof Map) {
			return mapToString((Map<?,?>)value, human, json, indent);
		} else {
			if (json) {
				try {
					Map<?,?> map = mapper.convertValue(value, Map.class);
					return valueToString(map, human, json, indent);
				} catch (IllegalArgumentException ex) {
					return JsonMapper.generate(value);
				}
			} else {
				return value.toString();
			}
		}
	}

	/**
	 * Returns a string representation of the specified map. If "human" is
	 * true, the returned string will have a friendly formatting, possibly
	 * spanning multiple lines.
	 * 
	 * @param map the map to format
	 * @param human true for friendly formatting, false for single-line
	 * formatting
	 * @param json true if the output should be JSON, false if the output should
	 * use Java toString()
	 * @param indent the number of times to indent each line except the first
	 * line
	 * @return the string
	 */
	private String mapToString(Map<?,?> map, boolean human, boolean json,
			int indent) {
		Set<?> keys = map.keySet();
		if (keys.isEmpty())
			return "{}";
		StringBuffer buffer = new StringBuffer();
		String newline = System.getProperty("line.separator");
		buffer.append("{");
		if (human && keys.size() > 1) {
			buffer.append(newline);
			indent(buffer, indent + 1);
		} else if (human) {
			buffer.append(" ");
		}
		boolean first = true;
		for (Object key : keys) {
			if (!first) {
				buffer.append(",");
				if (human) {
					buffer.append(newline);
					indent(buffer, indent + 1);
				}
			}
			first = false;
			buffer.append(valueToString(key, false, json, 0) + ":");
			if (human)
				buffer.append(" ");
			Object val = map.get(key);
			String valStr = valueToString(val, human, json, indent + 1);
			buffer.append(valStr);
		}
		if (human && keys.size() > 1) {
			buffer.append(newline);
			indent(buffer, indent);
		} else if (human) {
			buffer.append(" ");
		}
		buffer.append("}");
		return buffer.toString();
	}
	
	/**
	 * Returns a string representation of the specified list. If "human" is
	 * true, the returned string will have a friendly formatting, possibly
	 * spanning multiple lines.
	 * 
	 * @param list the list to format
	 * @param human true for friendly formatting, false for single-line
	 * formatting
	 * @param json true if the output should be JSON, false if the output should
	 * use Java toString()
	 * @param indent the number of times to indent each line except the first
	 * line
	 * @return the string
	 */
	private String listToString(List<?> list, boolean human, boolean json,
			int indent) {
		if (list.isEmpty())
			return "[]";
		StringBuffer buffer = new StringBuffer();
		String newline = System.getProperty("line.separator");
		buffer.append("[");
		if (human && list.size() > 1) {
			buffer.append(newline);
			indent(buffer, indent + 1);
		} else if (human) {
			buffer.append(" ");
		}
		boolean first = true;
		for (Object item : list) {
			if (!first) {
				buffer.append(",");
				if (human) {
					buffer.append(newline);
					indent(buffer, indent + 1);
				}
			}
			first = false;
			String itemStr = valueToString(item, human, json, indent + 1);
			buffer.append(itemStr);
		}
		if (human && list.size() > 1) {
			buffer.append(newline);
			indent(buffer, indent);
		} else if (human) {
			buffer.append(" ");
		}
		buffer.append("]");
		return buffer.toString();
	}
	
	/**
	 * Appends a specified number of indentations to a buffer.
	 * 
	 * @param buffer the string buffer
	 * @param indent the number of indentations to append
	 */
	private void indent(StringBuffer buffer, int indent) {
		for (int i = 0; i < indent; i++) {
			buffer.append(INDENT);
		}
	}
}
