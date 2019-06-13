package nl.rrd.wool.expressions.types;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.rrd.wool.expressions.EvaluationException;
import nl.rrd.wool.expressions.Expression;
import nl.rrd.wool.expressions.Token;
import nl.rrd.wool.expressions.Value;

public class DotExpression implements Expression {
	private Expression parentOperand;
	private Expression dotOperand;
	
	public DotExpression(Expression parentOperand, Expression dotOperand) {
		this.parentOperand = parentOperand;
		this.dotOperand = dotOperand;
	}

	public Expression getParentOperand() {
		return parentOperand;
	}

	public Expression getDotOperand() {
		return dotOperand;
	}

	@Override
	public Value evaluate(Map<String,Object> variables)
			throws EvaluationException {
		Value parent = parentOperand.evaluate(variables);
		if (!parent.isMap()) {
			throw new EvaluationException(
					"Dot parent must be a map, found: " +
					parent.getTypeString());
		}
		Map<?,?> map = (Map<?,?>)parent.getValue();
		String name = null;
		if (dotOperand instanceof ValueExpression) {
			ValueExpression valueExpr = (ValueExpression)dotOperand;
			if (valueExpr.getToken().getType() == Token.Type.NAME) {
				name = valueExpr.getToken().getValue().toString();
			}
		}
		if (name == null) {
			Value nameVal = dotOperand.evaluate(variables);
			if (!nameVal.isString() && !nameVal.isNumber()) {
				throw new EvaluationException(
						"Dot name must be a string or number, found: " +
						nameVal.getTypeString());
			}
			name = nameVal.toString();
		}
		return new Value(map.get(name));
	}

	@Override
	public List<Expression> getChildren() {
		List<Expression> result = new ArrayList<>();
		result.add(parentOperand);
		result.add(dotOperand);
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
		result.addAll(parentOperand.getVariableNames());
		boolean dotOperandIsName = false;
		if (dotOperand instanceof ValueExpression) {
			ValueExpression valueExpr = (ValueExpression)dotOperand;
			if (valueExpr.getToken().getType() == Token.Type.NAME) {
				dotOperandIsName = true;
			}
		}
		if (!dotOperandIsName)
			result.addAll(dotOperand.getVariableNames());
		return result;
	}
	
	@Override
	public String toString() {
		return parentOperand + "." + dotOperand;
	}
}
