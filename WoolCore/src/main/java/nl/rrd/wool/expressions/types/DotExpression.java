package nl.rrd.wool.expressions.types;

import java.util.Map;

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
	public Value evaluate(Map<String,?> variables) throws EvaluationException {
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
	public String toString() {
		return parentOperand + "." + dotOperand;
	}
}
