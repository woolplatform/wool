package nl.rrd.wool.expressions.types;

import java.util.Map;

import nl.rrd.wool.expressions.EvaluationException;
import nl.rrd.wool.expressions.Expression;
import nl.rrd.wool.expressions.Value;

public class GroupExpression implements Expression {
	private Expression expression;
	
	public GroupExpression(Expression expression) {
		this.expression = expression;
	}

	public Expression getExpression() {
		return expression;
	}

	@Override
	public Value evaluate(Map<String,Object> variables)
			throws EvaluationException {
		return expression.evaluate(variables);
	}
	
	@Override
	public String toString() {
		return "(" + expression + ")";
	}
}
