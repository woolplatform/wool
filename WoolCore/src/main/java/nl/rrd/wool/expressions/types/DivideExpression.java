package nl.rrd.wool.expressions.types;

import java.util.Map;

import nl.rrd.wool.expressions.EvaluationException;
import nl.rrd.wool.expressions.Expression;
import nl.rrd.wool.expressions.Value;

public class DivideExpression implements Expression {
	private Expression operand1;
	private Expression operand2;
	
	public DivideExpression(Expression operand1, Expression operand2) {
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
	public Value evaluate(Map<String,Object> variables)
			throws EvaluationException {
		Number num1 = operand1.evaluate(variables).asNumber();
		Number num2 = operand2.evaluate(variables).asNumber();
		if (Value.isIntNumber(num1) && Value.isIntNumber(num2)) {
			long l1 = num1.longValue();
			long l2 = num2.longValue();
			if (l1 % l2 == 0) {
				return new Value(Value.normalizeNumber(num1.longValue() /
						num2.longValue()));
			}
		}
		return new Value(num1.doubleValue() / num2.doubleValue());
	}
	
	@Override
	public String toString() {
		return operand1 + " / " + operand2;
	}
}
