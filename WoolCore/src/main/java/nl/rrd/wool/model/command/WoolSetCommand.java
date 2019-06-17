package nl.rrd.wool.model.command;

import java.util.Set;

import nl.rrd.wool.exception.LineNumberParseException;
import nl.rrd.wool.expressions.types.AssignExpression;
import nl.rrd.wool.model.WoolNodeBody;
import nl.rrd.wool.model.WoolReply;
import nl.rrd.wool.parser.WoolBodyToken;
import nl.rrd.wool.parser.WoolNodeState;
import nl.rrd.wool.utils.CurrentIterator;

/**
 * This class models a &lt;&lt;set ...&gt;&gt; command. It can be part of a
 * {@link WoolNodeBody WoolNodeBody} (along with an agent statement) or a {@link
 * WoolReply WoolReply} (to be performed when the user chooses the reply). It
 * contains an assign statement.
 * 
 * @author Dennis Hofs (RRD)
 */
public class WoolSetCommand extends WoolExpressionCommand {
	private AssignExpression expression;
	
	public WoolSetCommand(AssignExpression expression) {
		this.expression = expression;
	}

	public AssignExpression getExpression() {
		return expression;
	}

	public void setExpression(AssignExpression expression) {
		this.expression = expression;
	}
	
	@Override
	public void getReadVariableNames(Set<String> varNames) {
		varNames.addAll(expression.getValueOperand().getVariableNames());
	}

	@Override
	public String toString() {
		return "<<set " + expression + ">>";
	}

	public static WoolSetCommand parse(WoolBodyToken cmdStartToken,
			CurrentIterator<WoolBodyToken> tokens, WoolNodeState nodeState)
			throws LineNumberParseException {
		ReadContentResult content = readCommandContent(cmdStartToken, tokens);
		ParseContentResult parsed = parseCommandContentExpression(cmdStartToken,
				content, "set");
		if (!(parsed.expression instanceof AssignExpression)) {
			throw new LineNumberParseException(
					"Expression in \"set\" command is not an assignment",
					cmdStartToken.getLineNum(), cmdStartToken.getColNum());
		}
		return new WoolSetCommand((AssignExpression)parsed.expression);
	}
}
