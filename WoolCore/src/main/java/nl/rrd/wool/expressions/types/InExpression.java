package nl.rrd.wool.expressions.types;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
	public List<Expression> getChildren() {
		List<Expression> result = new ArrayList<>();
		result.add(operand1);
		result.add(operand2);
		return result;
	}

	@Override
	public List<Expression> getDescendants() {
		List<Expression> result = new ArrayList<>();
		for (Expression child : getChildren()) {
			result.add(child);
			result.addAll(child.getDescendants());
		}
		return result;
	}

	@Override
	public Set<String> getVariableNames() {
		Set<String> result = new HashSet<>();
		for (Expression child : getChildren()) {
			result.addAll(child.getVariableNames());
		}
		return result;
	}

	@Override
	public String toString() {
		return operand1 + " in " + operand2;
	}
}
