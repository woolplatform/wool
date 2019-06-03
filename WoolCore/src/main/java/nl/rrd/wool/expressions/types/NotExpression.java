package nl.rrd.wool.expressions.types;

import java.util.Map;

import nl.rrd.wool.expressions.EvaluationException;
import nl.rrd.wool.expressions.Expression;
import nl.rrd.wool.expressions.Value;

public class NotExpression implements Expression {
	private Expression operand;
	
	public NotExpression(Expression operand) {
		this.operand = operand;
	}

	public Expression getOperand() {
		return operand;
	}

	@Override
	public Value evaluate(Map<String,?> variables) throws EvaluationException {
		return new Value(!operand.evaluate(variables).asBoolean());
	}
	
	@Override
	public String toString() {
		return "!" + operand;
	}
}
