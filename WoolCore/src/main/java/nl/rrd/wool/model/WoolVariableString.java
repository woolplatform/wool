package nl.rrd.wool.model;

import java.util.ArrayList;
import java.util.List;

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
	
	public List<Segment> getSegments() {
		return segments;
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

	public static abstract class Segment {
	}
	
	public static class TextSegment extends Segment {
		private String text;
		
		public TextSegment(String text) {
			this.text = text;
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
						builder.append(input.substring(start, i));
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
	}
	
	public static class VariableSegment extends Segment {
		private String variableName;
		
		public VariableSegment(String variableName) {
			this.variableName = variableName;
		}
		
		@Override
		public String toString() {
			return "$" + variableName;
		}
	}
}
