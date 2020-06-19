/*
 * Copyright 2019-2020 Roessingh Research and Development.
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

package eu.woolplatform.wool.model;

import eu.woolplatform.utils.expressions.EvaluationException;
import eu.woolplatform.wool.model.command.*;
import eu.woolplatform.wool.model.nodepointer.WoolNodePointer;
import eu.woolplatform.wool.model.nodepointer.WoolNodePointerExternal;
import eu.woolplatform.wool.model.nodepointer.WoolNodePointerInternal;

import java.util.*;

/**
 * A node body can occur in three different contexts inside a {@link WoolNode
 * WoolNode}.
 * 
 * <p><ul>
 * <li>Directly in the node. In this case it specifies the agent statement with
 * possible commands and user replies.</li>
 * <li>As part of a clause in a {@link WoolIfCommand WoolIfCommand} or {@link
 * WoolRandomCommand WoolRandomCommand}. The content is the same as directly in
 * the node. The only difference is that it is performed conditionally.</li>
 * <li>As part of a {@link WoolReply WoolReply}. In this case it specifies the
 * user statement with possible commands, but no replies. Note that the UI shows
 * these statements as options immediately along with the agent statement. This
 * {@link WoolNodeBody WoolNodeBody} does not contain commands that are to be
 * performed when the reply is chosen. Such commands are specified separately in
 * a {@link WoolReply WoolReply}.</li>
 * </ul></p>
 * 
 * <p>The body contains a statement as a list of segments where each segment is
 * one of:</p>
 * 
 * <p><ul>
 * <li>{@link WoolNodeBody.TextSegment TextSegment}: a {@link WoolVariableString
 * WoolVariableString} with text and variables</li>
 * <li>{@link WoolNodeBody.CommandSegment CommandSegment}: a command (see
 * below)</li>
 * </ul></p>
 * 
 * <p>The segments are always normalized so that subsequent text segments are
 * automatically merged into one.</p>
 * 
 * <p>The type of commands depend on the context. Directly in the node or in a
 * {@link WoolIfCommand WoolIfCommand} or {@link WoolRandomCommand
 * WoolRandomCommand}, it can be:</p>
 * 
 * <p><ul>
 * <li>{@link WoolActionCommand WoolActionCommand}: Actions to perform along
 * with the agent's text statement.</li>
 * <li>{@link WoolIfCommand WoolIfCommand}: Contains clauses, each with a {@link
 * WoolNodeBody WoolNodeBody} specifying conditional statements, replies and
 * commands.</li>
 * <li>{@link WoolRandomCommand WoolRandomCommand}: Contains clauses, each with
 * a {@link WoolNodeBody WoolNodeBody} specifying statements, replies and
 * commands.</li>
 * <li>{@link WoolSetCommand WoolSetCommand}: Sets a variable value.</li>
 * </ul></p>
 * 
 * <p>As part of a reply (remember the earlier remarks about commands in a
 * reply), it can be:</p>
 * 
 * <p><ul>
 * <li>{@link WoolInputCommand WoolInputCommand}: Allow user to provide input
 * other than just clicking the reply option.</li>
 * </ul></p>
 * 
 * @author Dennis Hofs (RRD)
 */
public class WoolNodeBody {
	private List<Segment> segments = new ArrayList<>();
	private List<WoolReply> replies = new ArrayList<>();

	public WoolNodeBody() {
	}

	public WoolNodeBody(WoolNodeBody other) {
		for (Segment segment : other.segments) {
			this.segments.add(segment.clone());
		}
		for (WoolReply reply : other.replies) {
			this.replies.add(new WoolReply(reply));
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
			WoolVariableString text = new WoolVariableString();
			text.addSegments(lastTextSegment.text.getSegments());
			text.addSegments(textSegment.text.getSegments());
			TextSegment mergedSegment = new TextSegment(text);
			segments.remove(segments.size() - 1);
			segments.add(mergedSegment);
		} else {
			segments.add(segment);
		}
	}

	public void clearSegments() {
		segments.clear();
	}

	/**
	 * Normalizes whitespace in the text segments. It removes empty lines and
	 * makes sure that lines end with "\n". Within each line, it trims
	 * whitespace from the start and end, and it replaces any sequence of
	 * spaces and tabs with one space.
	 * 
	 * <p>This method should only be called if all variables in the text
	 * segments have been resolved.</p>
	 * 
	 * @param trimText true if trailing new lines should be trimmed, false if
	 * they should be preserved
	 */
	private void normalizeWhitespace(boolean trimText) {
		TextSegment lastText = null;
		String currLine = null;
		Iterator<Segment> it = segments.iterator();
		while (it.hasNext()) {
			Segment segment = it.next();
			if (!(segment instanceof TextSegment))
				continue;
			TextSegment textSegment = (TextSegment)segment;
			lastText = textSegment;
			String text = textSegment.text.evaluate(null);
			text = text.replaceAll("[\r\n]+", "\n")
					.replaceAll("[\t ]+", " ");
			StringBuilder normText = new StringBuilder();
			int start = 0;
			int index;
			while ((index = text.indexOf('\n', start)) != -1) {
				String line = text.substring(start, index).trim();
				if (currLine != null && line.isEmpty()) {
					normText.append("\n");
				} else if (currLine != null) {
					normText.append(" " + line + "\n");
				} else if (!line.isEmpty()) {
					normText.append(line + "\n");
				}
				currLine = null;
				start = index + 1;
			}
			String line = text.substring(start).trim();
			if (!line.isEmpty()) {
				if (currLine != null) {
					currLine += " " + line;
					normText.append(" " + line);
				} else {
					currLine = line;
					normText.append(line);
				}
			}
			if (normText.length() == 0) {
				it.remove();
			} else {
				textSegment.text = new WoolVariableString(normText.toString());
			}
		}
		if (trimText && lastText != null) {
			String text = lastText.text.evaluate(null).replaceAll("\\s+$", "");
			lastText.text = new WoolVariableString(text);
		}
	}

	public List<WoolReply> getReplies() {
		return replies;
	}
	
	public WoolReply findReplyById(int replyId) {
		for (WoolReply reply : replies) {
			if (reply.getReplyId() == replyId)
				return reply;
		}
		for (Segment segment : segments) {
			WoolReply reply = segment.findReplyById(replyId);
			if (reply != null)
				return reply;
		}
		return null;
	}

	public void addReply(WoolReply reply) {
		replies.add(reply);
	}

	/**
	 * Retrieves all variable names that are read in this body.
	 * 
	 * @return the variable names that are read in this body
	 */
	public List<String> getReadVariableNames() {
		Set<String> set = new HashSet<>();
		getReadVariableNames(set);
		List<String> result = new ArrayList<>(set);
		Collections.sort(result);
		return result;
	}
	
	/**
	 * Retrieves all variable names that are read in this body and adds them to
	 * the specified set.
	 * 
	 * @param varNames the set to which the variable names are added
	 */
	public void getReadVariableNames(Set<String> varNames) {
		for (Segment segment : segments) {
			segment.getReadVariableNames(varNames);
		}
		for (WoolReply reply : replies) {
			reply.getReadVariableNames(varNames);
		}
	}

	/**
	 * Retrieves all variable names that are written in this body.
	 * 
	 * @return the variable names that are written in this body
	 */
	public List<String> getWriteVariableNames() {
		Set<String> set = new HashSet<>();
		getWriteVariableNames(set);
		List<String> result = new ArrayList<>(set);
		Collections.sort(result);
		return result;
	}

	/**
	 * Retrieves all variable names that are written in this body and adds them
	 * to the specified set.
	 * 
	 * @param varNames the set to which the variable names are added
	 */
	public void getWriteVariableNames(Set<String> varNames) {
		for (Segment segment : segments) {
			segment.getWriteVariableNames(varNames);
		}
		for (WoolReply reply : replies) {
			reply.getWriteVariableNames(varNames);
		}
	}
	
	public List<WoolNodePointer> getNodePointers() {
		Set<WoolNodePointer> set = new HashSet<>();
		getNodePointers(set);
		List<WoolNodePointer> result = new ArrayList<>(set);
		Collections.sort(result, this::compareNodePointers);
		return result;
	}
	
	private int compareNodePointers(WoolNodePointer o1, WoolNodePointer o2) {
		if (o1 instanceof WoolNodePointerInternal) {
			if (o2 instanceof WoolNodePointerExternal)
				return -1;
			WoolNodePointerInternal p1 = (WoolNodePointerInternal)o1;
			WoolNodePointerInternal p2 = (WoolNodePointerInternal)o2;
			return p1.getNodeId().compareTo(p2.getNodeId());
		} else {
			if (o2 instanceof WoolNodePointerInternal)
				return -1;
			WoolNodePointerExternal p1 = (WoolNodePointerExternal)o1;
			WoolNodePointerExternal p2 = (WoolNodePointerExternal)o2;
			int result = p1.getDialogueId().compareTo(p2.getDialogueId());
			if (result != 0)
				return result;
			return p1.getNodeId().compareTo(p2.getNodeId());
		}
	}
	
	public void getNodePointers(Set<WoolNodePointer> pointers) {
		for (Segment segment : segments) {
			if (!(segment instanceof CommandSegment))
				continue;
			WoolCommand command = ((CommandSegment)segment).command;
			command.getNodePointers(pointers);
		}
		for (WoolReply reply : replies) {
			pointers.add(reply.getNodePointer());
		}
	}
	
	/**
	 * Executes the agent statement and reply statements in this body with
	 * respect to the specified variable map. It executes ("if" and "set")
	 * commands and resolves variables. Any resulting body content that should
	 * be sent to the client, is added to agent and reply statements in
	 * "processedBody". This content can be text or client commands, with all
	 * variables resolved.
	 * 
	 * <p>This method also normalizes whitespace in the text segments. It
	 * removes empty lines and makes sure that lines end with "\n". Within each
	 * line, it trims whitespace from the start and end, and it replaces any
	 * sequence of spaces and tabs with one space.</p>
	 * 
	 * <p>This method should only be called if all variables in the text
	 * segments have been resolved.</p>
	 *  
	 * @param variables the variable map
	 * @param trimText true if trailing new lines should be trimmed, false if
	 * they should be preserved. This should be set to true for the body that is
	 * directly in the node. If the body is in an "if" clause or in a reply, it
	 * should be set to false.
	 * @param processedBody the processed body
	 * @throws EvaluationException if an expression cannot be evaluated
	 */
	public void execute(Map<String,Object> variables, boolean trimText,
			WoolNodeBody processedBody) throws EvaluationException {
		for (Segment segment : segments) {
			if (segment instanceof TextSegment) {
				executeTextSegment((TextSegment)segment, variables,
						processedBody);
			} else {
				executeCommandSegment((CommandSegment)segment, variables,
						processedBody);
			}
		}
		for (WoolReply reply : replies) {
			processedBody.addReply(reply.execute(variables));
		}
		processedBody.normalizeWhitespace(trimText);
	}
	
	private void executeTextSegment(TextSegment segment,
			Map<String,Object> variables, WoolNodeBody processedBody) {
		TextSegment processedText = new TextSegment(
				segment.text.execute(variables));
		processedBody.addSegment(processedText);
	}
	
	private void executeCommandSegment(CommandSegment segment,
			Map<String,Object> variables, WoolNodeBody processedBody)
			throws EvaluationException {
		segment.command.executeBodyCommand(variables, processedBody);
	}

	public void trimWhitespace() {
		trimWhitespace(segments);
	}

	public static void trimWhitespace(List<WoolNodeBody.Segment> segments) {
		removeLeadingWhitespace(segments);
		removeTrailingWhitespace(segments);
	}

	public void removeLeadingWhitespace() {
		removeLeadingWhitespace(segments);
	}

	public static void removeLeadingWhitespace(List<WoolNodeBody.Segment> segments) {
		while (!segments.isEmpty()) {
			Segment segment = segments.get(0);
			if (!(segment instanceof TextSegment))
				return;
			TextSegment textSegment = (TextSegment)segment;
			WoolVariableString text = textSegment.getText();
			text.removeLeadingWhitespace();
			if (!text.getSegments().isEmpty())
				return;
			segments.remove(0);
		}
	}

	public void removeTrailingWhitespace() {
		removeTrailingWhitespace(segments);
	}

	public static void removeTrailingWhitespace(List<WoolNodeBody.Segment> segments) {
		while (!segments.isEmpty()) {
			Segment segment = segments.get(segments.size() - 1);
			if (!(segment instanceof TextSegment))
				return;
			TextSegment textSegment = (TextSegment)segment;
			WoolVariableString text = textSegment.getText();
			text.removeTrailingWhitespace();
			if (!text.getSegments().isEmpty())
				return;
			segments.remove(segments.size() - 1);
		}
	}
	
	@Override
	public String toString() {
		String newline = System.getProperty("line.separator");
		StringBuilder builder = new StringBuilder();
		for (Segment segment : segments) {
			builder.append(segment.toString());
		}
		for (WoolReply reply : replies) {
			builder.append(newline);
			builder.append(reply);
		}
		return builder.toString();
	}

	public static abstract class Segment implements Cloneable {
		/**
		 * Tries to find a reply with the specified ID within this segment. If
		 * no such reply is found, this method returns null.
		 * 
		 * @param replyId the reply ID
		 * @return the reply or null
		 */
		public abstract WoolReply findReplyById(int replyId);

		/**
		 * Retrieves all variable names that are read in this segment and adds
		 * them to the specified set.
		 * 
		 * @param varNames the set to which the variable names are added
		 */
		public abstract void getReadVariableNames(Set<String> varNames);
		
		/**
		 * Retrieves all variable names that are written in this segment and
		 * adds them to the specified set.
		 * 
		 * @param varNames the set to which the variable names are added
		 */
		public abstract void getWriteVariableNames(Set<String> varNames);

		/**
		 * Returns a deep copy of this segment.
		 *
		 * @return a deep copy of this segment
		 */
		@Override
		public abstract Segment clone();
	}
	
	public static class TextSegment extends Segment {
		private WoolVariableString text;
		
		public TextSegment(WoolVariableString text) {
			this.text = text;
		}

		public TextSegment(TextSegment other) {
			this.text = new WoolVariableString(other.text);
		}

		public WoolVariableString getText() {
			return text;
		}

		public void setText(WoolVariableString text) {
			this.text = text;
		}
		
		@Override
		public WoolReply findReplyById(int replyId) {
			return null;
		}

		@Override
		public void getReadVariableNames(Set<String> varNames) {
			text.getReadVariableNames(varNames);
		}

		@Override
		public void getWriteVariableNames(Set<String> varNames) {
		}

		@Override
		public String toString() {
			return text.toString();
		}

		@Override
		public TextSegment clone() {
			return new TextSegment(this);
		}
	}
	
	public static class CommandSegment extends Segment {
		private WoolCommand command;
		
		public CommandSegment(WoolCommand command) {
			this.command = command;
		}

		public CommandSegment(CommandSegment other) {
			this.command = other.command.clone();
		}

		public WoolCommand getCommand() {
			return command;
		}
		
		@Override
		public WoolReply findReplyById(int replyId) {
			return command.findReplyById(replyId);
		}

		@Override
		public void getReadVariableNames(Set<String> varNames) {
			command.getReadVariableNames(varNames);
		}

		@Override
		public void getWriteVariableNames(Set<String> varNames) {
			command.getWriteVariableNames(varNames);
		}
		
		@Override
		public String toString() {
			return command.toString();
		}

		@Override
		public CommandSegment clone() {
			return new CommandSegment(this);
		}
	}
}
