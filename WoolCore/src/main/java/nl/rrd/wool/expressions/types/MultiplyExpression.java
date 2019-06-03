package nl.rrd.wool.expressions.types;

import java.util.Map;

import nl.rrd.wool.expressions.EvaluationException;
import nl.rrd.wool.expressions.Expression;
import nl.rrd.wool.expressions.Value;

public class MultiplyExpression implements Expression {
	private Expression operand1;
	private Expression operand2;
	
	public MultiplyExpression(Expression operand1, Expression operand2) {
		this.operand1 = operand1;
		this.operand2 = operand2;
	}
	
	public Expression getOperand1() {
		return operand1;
	}

	public Expression getOperand2() {
		return operand2;
	}

	@Override
	public Value evaluate(Map<String,?> variables) throws EvaluationException {
		Number val1 = operand1.evaluate(variables).asNumber();
		Number val2 = operand2.evaluate(variables).asNumber();
		if (Value.isIntNumber(val1) && Value.isIntNumber(val2)) {
			return new Value(Value.normalizeNumber(
					val1.longValue() * val2.longValue()));
		} else {
			return new Value(Value.normalizeNumber(
					val1.doubleValue() * val2.doubleValue()));
		}
	}
	
	@Override
	public String toString() {
		return operand1 + " * " + operand2;
	}
}
