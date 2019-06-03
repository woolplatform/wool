package nl.rrd.wool.expressions.types;

import java.util.Map;

import nl.rrd.wool.expressions.EvaluationException;
import nl.rrd.wool.expressions.Expression;
import nl.rrd.wool.expressions.Value;

public class LessThanExpression implements Expression {
	private Expression operand1;
	private Expression operand2;
	
	public LessThanExpression(Expression operand1, Expression operand2) {
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
		Value[] vals = new Value[2];
		vals[0] = operand1.evaluate(variables);
		vals[1] = operand2.evaluate(variables);
		for (Value val : vals) {
			if (!val.isString() && !val.isNumber()) {
				throw new EvaluationException(
						"Operand of < must be a string or number, found: " +
						val.getTypeString());
			}
		}
		if (vals[0].isString() || vals[1].isString()) {
			return new Value(vals[0].toString().compareTo(
					vals[1].toString()) < 0);
		} else {
			Number num1 = vals[0].asNumber();
			Number num2 = vals[1].asNumber();
			if (Value.isIntNumber(num1) && Value.isIntNumber(num2))
				return new Value(num1.longValue() < num2.longValue());
			else
				return new Value(num1.doubleValue() < num2.doubleValue());
		}
	}
	
	@Override
	public String toString() {
		return operand1 + " < " + operand2;
	}
}
