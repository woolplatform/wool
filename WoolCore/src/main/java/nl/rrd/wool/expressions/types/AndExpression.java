package nl.rrd.wool.expressions.types;

import java.util.Map;

import nl.rrd.wool.expressions.EvaluationException;
import nl.rrd.wool.expressions.Expression;
import nl.rrd.wool.expressions.Value;

public class AndExpression implements Expression {
	private Expression operand1;
	private Expression operand2;
	
	public AndExpression(Expression operand1, Expression operand2) {
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
		Value val1 = operand1.evaluate(variables);
		if (!val1.asBoolean())
			return new Value(false);
		Value val2 = operand2.evaluate(variables);
		return new Value(val2.asBoolean());
	}
	
	@Override
	public String toString() {
		return operand1 + " && " + operand2;
	}
}
