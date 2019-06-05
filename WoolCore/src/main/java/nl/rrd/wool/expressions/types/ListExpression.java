package nl.rrd.wool.expressions.types;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
