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

package eu.woolplatform.wool.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.rrd.utils.expressions.Value;

/**
 * This class represents a text with possible variables. It is modelled as a
 * list of segments, where each segment is plain text or a variable.
 * 
 * <p>The segments are always normalized so that subsequent plain text segments
 * are automatically merged into one.</p>
 * 
 * @author Dennis Hofs (RRD)
 */
public class WoolVariableString {
	private List<Segment> segments = new ArrayList<>();
	
	public WoolVariableString() {
	}

	public WoolVariableString(String text) {
		segments.add(new TextSegment(text));
	}

	public WoolVariableString(WoolVariableString other) {
		for (Segment segment : other.segments) {
			this.segments.add(segment.clone());
		}
	}

	/**
	 * Returns the segments as an unmodifiable list.
	 * 
	 * @return the segments as an unmodifiable list
	 */
	public List<Segment> getSegments() {
		return Collections.unmodifiableList(segments);
	}
	
	public void addSegment(Segment segment) {
		Segment lastSegment = null;
		if (!segments.isEmpty())
			lastSegment = segments.get(segments.size() - 1);
		if (lastSegment instanceof TextSegment &&
				segment instanceof TextSegment) {
			TextSegment lastTextSegment = (TextSegment)lastSegment;
			TextSegment textSegment = (TextSegment)segment;
			TextSegment mergedSegment = new TextSegment(
					lastTextSegment.text + textSegment.text);
			segments.remove(segments.size() - 1);
			segments.add(mergedSegment);
		} else {
			segments.add(segment);
		}
	}
	
	public void addSegments(Iterable<Segment> segments) {
		for (Segment segment : segments) {
			addSegment(segment);
		}
	}

	/**
	 * Executes this variable string with respect to the specified variables.
	 * The result will be a string with 0 or 1 text segments. Undefined
	 * variables will be evaluated as string "null".
	 * 
	 * @param variables the variable map (can be null)
	 * @return the processed variable string
	 */
	public WoolVariableString execute(Map<String,Object> variables) {
		WoolVariableString result = new WoolVariableString();
		for (Segment segment : segments) {
			if (segment instanceof TextSegment) {
				result.addSegment(segment);
			} else {
				VariableSegment varSegment = (VariableSegment)segment;
				Object valueObj = null;
				if (variables != null)
					valueObj = variables.get(varSegment.variableName);
				Value value = new Value(valueObj);
				result.addSegment(new TextSegment(value.toString()));
			}
		}
		return result;
	}

	/**
	 * Evaluates this variable string with respect to the specified variables.
	 * Undefined variables will be evaluated as string "null".
	 * 
	 * @param variables the variable map (can be null)
	 * @return the evaluated string
	 */
	public String evaluate(Map<String,Object> variables) {
		WoolVariableString varStr = execute(variables);
		if (varStr.segments.isEmpty())
			return "";
		TextSegment segment = (TextSegment)varStr.segments.get(0);
		return segment.text;
	}
	
	/**
	 * Retrieves all variable names that are read in this variable string and
	 * adds them to the specified set.
	 * 
	 * @param varNames the set to which the variable names are added
	 */
	public void getReadVariableNames(Set<String> varNames) {
		for (Segment segment : segments) {
			if (!(segment instanceof VariableSegment))
				continue;
			VariableSegment varSegment = (VariableSegment)segment;
			varNames.add(varSegment.variableName);
		}
	}
	
	public boolean isWhitespace() {
		for (Segment segment : segments) {
			if (!(segment instanceof TextSegment))
				return false;
			TextSegment textSegment = (TextSegment)segment;
			if (!textSegment.text.trim().isEmpty())
				return false;
		}
		return true;
	}

	public boolean isPlainText() {
		for (Segment segment : segments) {
			if (!(segment instanceof TextSegment))
				return false;
		}
		return true;
	}
	
	public void trimWhitespace() {
		removeLeadingWhitespace();
		removeTrailingWhitespace();
	}

	public void removeLeadingWhitespace() {
		while (!segments.isEmpty()) {
			Segment segment = segments.get(0);
			if (!(segment instanceof TextSegment))	
				return;
			TextSegment textSegment = (TextSegment)segment;
			String content = textSegment.getText().replaceAll("^\\s+", "");
			textSegment.setText(content);
			if (content.length() > 0)
				return;
			segments.remove(0);
		}
	}
	
	public void removeTrailingWhitespace() {
		while (!segments.isEmpty()) {
			Segment segment = segments.get(segments.size() - 1);
			if (!(segment instanceof TextSegment))	
				return;
			TextSegment textSegment = (TextSegment)segment;
			String content = textSegment.getText().replaceAll("\\s+$", "");
			textSegment.setText(content);
			if (content.length() > 0)
				return;
			segments.remove(segments.size() - 1);
		}
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		for (Segment segment : segments) {
			result.append(segment.toString());
		}
		return result.toString();
	}
	
	/**
	 * Returns the code string for this instance. It will escape \ and $ with a
	 * backslash. You may specify additional characters to escape.
	 * 
	 * @param escapes the characters to escape
	 * @return the code string
	 */
	public String toString(char[] escapes) {
		StringBuilder result = new StringBuilder();
		for (Segment segment : segments) {
			if (segment instanceof TextSegment) {
				result.append(((TextSegment)segment).toString(escapes));
			} else {
				result.append(segment.toString());
			}
		}
		return result.toString();
	}

	public static abstract class Segment implements Cloneable {
		@Override
		public abstract Segment clone();
	}
	
	public static class TextSegment extends Segment {
		private String text;
		
		public TextSegment(String text) {
			this.text = text;
		}

		public TextSegment(TextSegment other) {
			this.text = other.text;
		}

		public String getText() {
			return text;
		}

		public void setText(String text) {
			this.text = text;
		}
		
		@Override
		public String toString() {
			return text.replaceAll("\\\\", "\\\\\\\\")
					.replaceAll("\\$", "\\\\\\$");
		}
		
		/**
		 * Returns the code string for this instance. It will escape \ and $
		 * with a backslash. You may specify additional characters to escape.
		 * 
		 * @param escapes the characters to escape
		 * @return the code string
		 */
		public String toString(char[] escapes) {
			String input = toString();
			StringBuilder builder = new StringBuilder();
			int start = 0;
			for (int i = 0; i < input.length(); i++) {
				char c = input.charAt(i);
				for (char escape : escapes) {
					if (c == escape) {
						builder.append(input, start, i);
						builder.append('\\');
						builder.append(c);
						start = i + 1;
						break;
					}
				}
			}
			if (start < input.length())
				builder.append(input.substring(start));
			return builder.toString();
		}

		@Override
		public TextSegment clone() {
			return new TextSegment(this);
		}
	}
	
	public static class VariableSegment extends Segment {
		private String variableName;

		public VariableSegment(String variableName) {
			this.variableName = variableName;
		}

		public VariableSegment(VariableSegment other) {
			this.variableName = other.variableName;
		}

		public String getVariableName() {
			return variableName;
		}

		public void setVariableName(String variableName) {
			this.variableName = variableName;
		}

		@Override
		public String toString() {
			return "$" + variableName;
		}

		@Override
		public VariableSegment clone() {
			return new VariableSegment(this);
		}
	}
}
