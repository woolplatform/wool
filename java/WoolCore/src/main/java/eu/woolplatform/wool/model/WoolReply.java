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

package eu.woolplatform.wool.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.woolplatform.utils.expressions.EvaluationException;
import eu.woolplatform.wool.model.command.WoolActionCommand;
import eu.woolplatform.wool.model.command.WoolCommand;
import eu.woolplatform.wool.model.command.WoolInputCommand;
import eu.woolplatform.wool.model.command.WoolSetCommand;
import eu.woolplatform.wool.model.nodepointer.WoolNodePointer;

/**
 * A reply option within a {@link WoolNodeBody WoolNodeBody}. A reply always has
 * a pointer to the next node when the reply is chosen. This might be a pointer
 * to the end node. The reply usually has a statement that is shown in the UI,
 * but a node may have at most one reply without a statement, which is known as
 * an auto-forward reply.
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
	private int replyId;
	private WoolNodeBody statement = null;
	private WoolNodePointer nodePointer;
	private List<WoolCommand> commands = new ArrayList<>();

	/**
	 * Constructs a new reply.
	 *
	 * @param replyId the reply ID
	 * @param statement the statement or null (auto-forward reply)
	 * @param nodePointer the next node when the reply is chosen
	 */
	public WoolReply(int replyId, WoolNodeBody statement,
			WoolNodePointer nodePointer) {
		this.replyId = replyId;
		this.statement = statement;
		this.nodePointer = nodePointer;
	}

	/**
	 * Constructs an auto-forward reply without a statement.
	 *
	 * @param replyId the reply ID
	 * @param nodePointer the next node when the reply is chosen
	 */
	public WoolReply(int replyId, WoolNodePointer nodePointer) {
		this.replyId = replyId;
		this.nodePointer = nodePointer;
	}

	public WoolReply(WoolReply other) {
		this.replyId = other.replyId;
		if (other.statement != null)
			this.statement = new WoolNodeBody(other.statement);
		this.nodePointer = other.nodePointer.clone();
		for (WoolCommand cmd : other.commands) {
			this.commands.add(cmd.clone());
		}
	}

	/**
	 * Returns the reply ID. The ID is unique within a node.
	 * 
	 * @return the reply ID
	 */
	public int getReplyId() {
		return replyId;
	}

	/**
	 * Sets the reply ID. The ID is unique within a node.
	 * 
	 * @param replyId the reply ID
	 */
	public void setReplyId(int replyId) {
		this.replyId = replyId;
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
	 * Returns the next node when this reply is chosen. This might be the end
	 * node.
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
	
	/**
	 * Adds a command that should be executed when this reply is chosen.
	 * 
	 * @param command the command that should be executed when this reply is
	 * chosen
	 */
	public void addCommand(WoolCommand command) {
		commands.add(command);
	}
	
	/**
	 * Retrieves all variable names that are read in this reply and adds them to
	 * the specified set.
	 * 
	 * @param varNames the set to which the variable names are added
	 */
	public void getReadVariableNames(Set<String> varNames) {
		if (statement != null)
			statement.getReadVariableNames(varNames);
		for (WoolCommand command : commands) {
			command.getReadVariableNames(varNames);
		}
	}
	
	/**
	 * Retrieves all variable names that are written in this reply and adds them
	 * to the specified set.
	 * 
	 * @param varNames the set to which the variable names are added
	 */
	public void getWriteVariableNames(Set<String> varNames) {
		if (statement != null)
			statement.getWriteVariableNames(varNames);
		for (WoolCommand command : commands) {
			command.getWriteVariableNames(varNames);
		}
	}
	
	/**
	 * Executes the statement in this reply with respect to the specified
	 * variable map. It executes commands and resolves variables, so that only
	 * content that should be sent to the client, remains in the resulting
	 * reply statement. This content can be text or client commands, with all
	 * variables resolved.
	 * 
	 * @param variables the variable map
	 * @return the processed reply
	 * @throws EvaluationException if an expression cannot be evaluated
	 */
	public WoolReply execute(Map<String,Object> variables)
			throws EvaluationException {
		if (statement == null)
			return this;
		WoolNodeBody processedStatement = new WoolNodeBody();
		statement.execute(variables, false, processedStatement);
		WoolReply result = new WoolReply(replyId, processedStatement,
				nodePointer);
		result.commands = commands;
		return result;
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
