package nl.rrd.wool.expressions.types;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.rrd.wool.expressions.EvaluationException;
import nl.rrd.wool.expressions.Expression;
import nl.rrd.wool.expressions.Value;

public class ListExpression implements Expression {
	private List<Expression> elements;
	
	public ListExpression(List<Expression> elements) {
		this.elements = elements;
	}

	public List<Expression> getElements() {
		return elements;
	}

	@Override
	public Value evaluate(Map<String,Object> variables)
			throws EvaluationException {
		List<Object> result = new ArrayList<>();
		for (Expression expr : elements) {
			result.add(expr.evaluate(variables).getValue());
		}
		return new Value(result);
	}
	
	@Override
	public List<Expression> getChildren() {
		return new ArrayList<>(elements);
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
		StringBuilder builder = new StringBuilder();
		for (Expression elem : elements) {
			if (builder.length() > 0)
				builder.append(", ");
			builder.append(elem);
		}
		return "[" + builder + "]";
	}
}
