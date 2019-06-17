package nl.rrd.wool.model.command;

import java.util.Map;
import java.util.Set;

import nl.rrd.wool.expressions.EvaluationException;
import nl.rrd.wool.model.WoolNodeBody;

/**
 * Base class for commands that are specified with &lt;&lt;...&gt;&gt; in Wool
 * statements and replies.
 * 
 * @author Dennis Hofs (RRD)
 */
public abstract class WoolCommand {
	
	/**
	 * Retrieves all variable names that are read in this command and adds them
	 * to the specified set.
	 * 
	 * @param varNames the set to which the variable names are added
	 */
	public abstract void getReadVariableNames(Set<String> varNames);

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
}
