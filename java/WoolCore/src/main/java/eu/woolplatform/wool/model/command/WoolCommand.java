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

package eu.woolplatform.wool.model.command;

import java.util.Map;
import java.util.Set;

import nl.rrd.utils.expressions.EvaluationException;
import eu.woolplatform.wool.model.WoolNodeBody;
import eu.woolplatform.wool.model.WoolReply;
import eu.woolplatform.wool.model.nodepointer.WoolNodePointer;

/**
 * Base class for commands that are specified with &lt;&lt;...&gt;&gt; in Wool
 * statements and replies.
 * 
 * @author Dennis Hofs (RRD)
 */
public abstract class WoolCommand implements Cloneable {

	/**
	 * Tries to find a reply with the specified ID within this command. If no
	 * such reply is found, this method returns null.
	 * 
	 * @param replyId the reply ID
	 * @return the reply or null
	 */
	public abstract WoolReply findReplyById(int replyId);
	
	/**
	 * Retrieves all variable names that are read in this command and adds them
	 * to the specified set.
	 * 
	 * @param varNames the set to which the variable names are added
	 */
	public abstract void getReadVariableNames(Set<String> varNames);
	
	/**
	 * Retrieves all variable names that are written in this command and adds
	 * them to the specified set.
	 * 
	 * @param varNames the set to which the variable names are added
	 */
	public abstract void getWriteVariableNames(Set<String> varNames);
	
	/**
	 * Retrieves all node pointers that occur in this command and adds them to
	 * the specified list.
	 * 
	 * @param pointers the list to which the node pointers are added
	 */
	public abstract void getNodePointers(Set<WoolNodePointer> pointers);

	/**
	 * This method is called if this command occurs in a statement body. It
	 * executes the command with respect to the specified variable map. Any
	 * body content that should be sent to the client, is added to
	 * "processedBody". This content can be text or client commands, with all
	 * variables resolved.
	 * 
	 * @param variables the variable map
	 * @param processedBody the processed body
	 * @throws EvaluationException if an expression cannot be evaluated
	 */
	public abstract void executeBodyCommand(Map<String,Object> variables,
			WoolNodeBody processedBody) throws EvaluationException;

	/**
	 * Returns a deep copy of this command.
	 *
	 * @return a deep copy of this command
	 */
	@Override
	public abstract WoolCommand clone();
}
