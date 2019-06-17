package nl.rrd.wool.model.command;

import java.util.Set;

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
}
