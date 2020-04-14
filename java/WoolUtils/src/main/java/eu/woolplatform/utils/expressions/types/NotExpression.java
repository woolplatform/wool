package eu.woolplatform.utils.expressions.types;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.woolplatform.utils.expressions.EvaluationException;
import eu.woolplatform.utils.expressions.Expression;
import eu.woolplatform.utils.expressions.Value;

public class NotExpression implements Expression {
	private Expression operand;
	
	public NotExpression(Expression operand) {
		this.operand = operand;
	}

	public Expression getOperand() {
		return operand;
	}

	@Override
	public Value evaluate(Map<String,Object> variables)
			throws EvaluationException {
		return new Value(!operand.evaluate(variables).asBoolean());
	}

	@Override
	public List<Expression> getChildren() {
		List<Expression> result = new ArrayList<>();
		result.add(operand);
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
		return "!" + operand;
	}
}
