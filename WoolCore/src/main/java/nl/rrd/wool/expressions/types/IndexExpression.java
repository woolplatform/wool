package nl.rrd.wool.expressions.types;

import java.util.List;
import java.util.Map;

import nl.rrd.wool.expressions.EvaluationException;
import nl.rrd.wool.expressions.Expression;
import nl.rrd.wool.expressions.Value;

public class IndexExpression implements Expression {
	private Expression parentOperand;
	private Expression indexOperand;
	
	public IndexExpression(Expression parentOperand, Expression indexOperand) {
		this.parentOperand = parentOperand;
		this.indexOperand = indexOperand;
	}

	public Expression getParentOperand() {
		return parentOperand;
	}

	public Expression getIndexOperand() {
		return indexOperand;
	}

	@Override
	public Value evaluate(Map<String,Object> variables)
			throws EvaluationException {
		Value parentVal = parentOperand.evaluate(variables);
		if (!parentVal.isString() && !parentVal.isList() &&
				!parentVal.isMap()) {
			throw new EvaluationException(
					"Index parent must be a string, list or map, found: " +
					parentVal.getTypeString());
		}
		Value indexVal = indexOperand.evaluate(variables);
		if (parentVal.isString()) {
			if (!indexVal.isNumericString() && !indexVal.isNumber()) {
				throw new EvaluationException(
						"String index must be a number or numeric string, found: " +
						indexVal.getTypeString());
			}
			Number num = indexVal.asNumber();
			if (!(num instanceof Integer)) {
				throw new EvaluationException(
						"String index must be an integer, found: " +
						num.getClass().getSimpleName());
			}
			return new Value(Character.toString(parentVal.toString().charAt(
					num.intValue())));
		} else if (parentVal.isList()) {
			if (!indexVal.isNumericString() && !indexVal.isNumber()) {
				throw new EvaluationException(
						"List index must be a number or numeric string, found: " +
						indexVal.getTypeString());
			}
			Number num = indexVal.asNumber();
			if (!(num instanceof Integer)) {
				throw new EvaluationException(
						"List index must be an integer, found: " +
						num.getClass().getSimpleName());
			}
			List<?> list = (List<?>)parentVal.getValue();
			return new Value(list.get(num.intValue()));
		} else {
			if (!indexVal.isString() && !indexVal.isNumber()) {
				throw new EvaluationException(
						"Map index must be a string or number, found: " +
						indexVal.getTypeString());
			}
			Map<?,?> map = (Map<?,?>)parentVal.getValue();
			return new Value(map.get(indexVal.toString()));
		}
	}
	
	@Override
	public String toString() {
		return parentOperand + "[" + indexOperand + "]";
	}
}
