package nl.rrd.wool.model;

import java.util.ArrayList;
import java.util.List;

import nl.rrd.wool.model.command.WoolActionCommand;
import nl.rrd.wool.model.command.WoolCommand;
import nl.rrd.wool.model.command.WoolInputCommand;
import nl.rrd.wool.model.command.WoolSetCommand;
import nl.rrd.wool.model.nodepointer.WoolNodePointer;

/**
 * A reply option within a {@link WoolNodeBody WoolNodeBody}. A reply always has
 * a pointer to the next node when the reply is chosen. It usually has a
 * statement that is shown in the UI, but a node may have at most one reply
 * without a statement, which is known as an auto-forward reply.
 * 
 * <p>The statement may contain a {@link WoolInputCommand WoolInputCommand} (see
 * {@link WoolNodeBody WoolNodeBody}).</p>
 * 
 * <p>The reply may also have commands that should be performed when the reply
 * is chosen. This can be:</p>
 * 
 * <p><ul>
 * <li>{@link WoolActionCommand WoolActionCommand}</li>
 * <li>{@link WoolSetCommand WoolSetCommand}</li>
 * </ul></p>
 * 
 * @author Dennis Hofs (RRD)
 */
public class WoolReply {
	private WoolNodeBody statement = null;
	private WoolNodePointer nodePointer;
	private List<WoolCommand> commands = new ArrayList<>();

	/**
	 * Constructs a new reply.
	 * 
	 * @param statement the statement or null (auto-forward reply)
	 * @param nodePointer the next node when the reply is chosen
	 */
	public WoolReply(WoolNodeBody statement, WoolNodePointer nodePointer) {
		this.statement = statement;
		this.nodePointer = nodePointer;
	}

	/**
	 * Constructs an auto-forward reply without a statement.
	 * 
	 * @param nodePointer the next node when the reply is chosen
	 */
	public WoolReply(WoolNodePointer nodePointer) {
		this.nodePointer = nodePointer;
	}

	/**
	 * Returns the statement. If this reply is an auto-forward reply, then this
	 * method returns null.
	 * 
	 * @return the statement or null
	 */
	public WoolNodeBody getStatement() {
		return statement;
	}

	/**
	 * Sets the statement. If this reply is an auto-forward reply, then the
	 * statement can be null.
	 * 
	 * @param statement the statement or null
	 */
	public void setStatement(WoolNodeBody statement) {
		this.statement = statement;
	}

	/**
	 * Returns the next node when this reply is chosen.
	 * 
	 * @return the next node when this reply is chosen
	 */
	public WoolNodePointer getNodePointer() {
		return nodePointer;
	}

	/**
	 * Sets the next node when this reply is chosen.
	 * 
	 * @param nodePointer the next node when this reply is chosen
	 */
	public void setNodePointer(WoolNodePointer nodePointer) {
		this.nodePointer = nodePointer;
	}

	/**
	 * Returns the commands that should be executed when this reply is chosen.
	 * 
	 * @return the commands that should be executed when this reply is chosen
	 */
	public List<WoolCommand> getCommands() {
		return commands;
	}

	/**
	 * Sets the commands that should be executed when this reply is chosen.
	 * 
	 * @param commands the commands that should be executed when this reply is
	 * chosen
	 */
	public void setCommands(List<WoolCommand> commands) {
		this.commands = commands;
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder("[[");
		if (statement != null)
			result.append(statement + "|");
		result.append(nodePointer.toString());
		if (!commands.isEmpty()) {
			result.append("|");
			for (WoolCommand command : commands) {
				result.append(command.toString());
			}
		}
		result.append("]]");
		return result.toString();
	}
}
