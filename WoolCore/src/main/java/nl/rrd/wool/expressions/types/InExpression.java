package nl.rrd.wool.expressions.types;

import java.util.List;
import java.util.Map;

import nl.rrd.wool.expressions.EvaluationException;
import nl.rrd.wool.expressions.Expression;
import nl.rrd.wool.expressions.Value;

public class InExpression implements Expression {
	private Expression operand1;
	private Expression operand2;
	
	public InExpression(Expression operand1, Expression operand2) {
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
		Value needle = operand1.evaluate(variables);
		Value collection = operand2.evaluate(variables);
		if (!collection.isString() && !collection.isList()) {
			throw new EvaluationException(
					"Operand after \"in\" must be a string or list, found: " +
					collection.getTypeString());
		}
		if (collection.isString()) {
			if (!needle.isString() && !needle.isNumber()) {
				throw new EvaluationException(
						"Operand before \"in\" string must be a string or number, found: " +
						needle.getTypeString());
			}
			return new Value(collection.toString().contains(needle.toString()));
		} else {
			List<?> list = (List<?>)collection.getValue();
			for (Object item : list) {
				if (new Value(item).isEqual(needle))
					return new Value(true);
			}
			return new Value(false);
		}
	}
	
	@Override
	public String toString() {
		return operand1 + " in " + operand2;
	}
}
