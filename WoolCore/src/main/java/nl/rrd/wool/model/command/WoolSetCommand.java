package nl.rrd.wool.model.command;

import nl.rrd.wool.exception.LineNumberParseException;
import nl.rrd.wool.expressions.Expression;
import nl.rrd.wool.model.WoolNodeBody;
import nl.rrd.wool.model.WoolReply;
import nl.rrd.wool.parser.WoolBodyToken;
import nl.rrd.wool.utils.CurrentIterator;

/**
 * This class models a &lt;&lt;set ...&gt;&gt; command. It can be part of a
 * {@link WoolNodeBody WoolNodeBody} (along with an agent statement) or a {@link
 * WoolReply WoolReply} (to be performed when the user chooses the reply). It
 * contains an assign statement.
 * 
 * @author Dennis Hofs (RRD)
 */
public class WoolSetCommand extends WoolCommand {
	private Expression expression;
	
	public WoolSetCommand(Expression expression) {
		this.expression = expression;
	}

	public Expression getExpression() {
		return expression;
	}

	public void setExpression(Expression expression) {
		this.expression = expression;
	}
	
	@Override
	public String toString() {
		return "<<set " + expression + ">>";
	}

	public static WoolActionCommand parse(WoolBodyToken cmdStartToken,
			CurrentIterator<WoolBodyToken> tokens)
			throws LineNumberParseException {
		// TODO
		return null;
	}
}
