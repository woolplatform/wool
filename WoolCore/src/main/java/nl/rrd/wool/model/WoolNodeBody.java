package nl.rrd.wool.model;

import java.util.ArrayList;
import java.util.List;

import nl.rrd.wool.model.command.WoolActionCommand;
import nl.rrd.wool.model.command.WoolCommand;
import nl.rrd.wool.model.command.WoolIfCommand;
import nl.rrd.wool.model.command.WoolInputCommand;
import nl.rrd.wool.model.command.WoolSetCommand;

/**
 * A node body can occur in three different contexts inside a {@link WoolNode
 * WoolNode}.
 * 
 * <p><ul>
 * <li>Directly in the node. In this case it specifies the agent statement with
 * possible commands and user replies.</li>
 * <li>As part of a clause in a {@link WoolIfCommand WoolIfCommand}. The content
 * is the same as directly in the node. The only difference is that it is
 * performed conditionally.</li>
 * <li>As part of a {@link WoolReply WoolReply}. In this case it specifies the
 * user statement with possible commands, but no replies. Note that the UI shows
 * these statements as options immediately along with the agent statement. This
 * {@link WoolNodeBody WoolNodeBody} does not contain commands that are to be
 * performed when the reply is chosen. Such commands are specified separately in
 * a {@link WoolReply WoolReply}.</li>
 * </ul></p>
 * 
 * <p>The body contains a list of segments where each segment is one of:</p>
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
 * {@link WoolIfCommand WoolIfCommand}, it can be:</p>
 * 
 * <p><ul>
 * <li>{@link WoolActionCommand WoolActionCommand}: Actions to perform along
 * with the agent's text statement.</li>
 * <li>{@link WoolIfCommand WoolIfCommand}: Contains clauses, each with a {@link
 * WoolNodeBody WoolNodeBody} specifying conditional statements, replies and
 * commands.</li>
 * <li>{@link WoolSetCommand WoolSetCommand}: Sets a variable value. This is
 * in most cases more useful as a reply command.</li>
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

	public List<WoolReply> getReplies() {
		return replies;
	}

	public void addReply(WoolReply reply) {
		replies.add(reply);
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
			WoolVariableString text = textSegment.getText();
			text.removeLeadingWhitespace();
			if (!text.getSegments().isEmpty())
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
			WoolVariableString text = textSegment.getText();
			text.removeTrailingWhitespace();
			if (!text.getSegments().isEmpty())
				return;
			segments.remove(segments.size() - 1);
		}
	}

	public static abstract class Segment {
	}
	
	public static class TextSegment extends Segment {
		private WoolVariableString text;
		
		public TextSegment(WoolVariableString text) {
			this.text = text;
		}

		public WoolVariableString getText() {
			return text;
		}

		public void setText(WoolVariableString text) {
			this.text = text;
		}
		
		@Override
		public String toString() {
			return text.toString();
		}
	}
	
	public static class CommandSegment extends Segment {
		private WoolCommand command;
		
		public CommandSegment(WoolCommand command) {
			this.command = command;
		}

		public WoolCommand getCommand() {
			return command;
		}
		
		@Override
		public String toString() {
			return command.toString();
		}
	}
}
