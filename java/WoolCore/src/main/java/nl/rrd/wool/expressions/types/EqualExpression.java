package nl.rrd.wool.expressions.types;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.rrd.wool.expressions.EvaluationException;
import nl.rrd.wool.expressions.Expression;
import nl.rrd.wool.expressions.Value;

public class EqualExpression implements Expression {
	private Expression operand1;
	private Expression operand2;
	
	public EqualExpression(Expression operand1, Expression operand2) {
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
		Value val2 = operand2.evaluate(variables);
		return new Value(val1.isEqual(val2));
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
		return operand1 + " == " + operand2;
	}
}
