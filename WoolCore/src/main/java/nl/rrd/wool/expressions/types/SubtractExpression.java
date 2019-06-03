package nl.rrd.wool.expressions.types;

import java.util.List;
import java.util.Map;

import nl.rrd.wool.expressions.EvaluationException;
import nl.rrd.wool.expressions.Expression;
import nl.rrd.wool.expressions.Value;

public class SubtractExpression implements Expression {
	private Expression operand1;
	private Expression operand2;
	
	public SubtractExpression(Expression operand1, Expression operand2) {
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
	public Value evaluate(Map<String,?> variables) throws EvaluationException {
		Value val1 = operand1.evaluate(variables);
		Value val2 = operand2.evaluate(variables);
		if (val1.isMap()) {
			Map<?,?> map = (Map<?,?>)val1.getValue();
			if (val2.isList())
				subtractMap(map, (List<?>)val2.getValue());
			else
				removeFromMap(map, val2);
			return new Value(map);
		} else if (val1.isList()) {
			List<?> list = (List<?>)val1.getValue();
			if (val2.isList())
				subtractList(list, (List<?>)val2.getValue());
			else
				removeFromList(list, val2);
			return new Value(list);
		} else {
			Number num1 = val1.asNumber();
			Number num2 = val2.asNumber();
			if (Value.isIntNumber(num1) && Value.isIntNumber(num2)) {
				return new Value(Value.normalizeNumber(num1.longValue() -
						num2.longValue()));
			} else {
				return new Value(num1.doubleValue() - num2.doubleValue());
			}
		}
	}
	
	private void subtractMap(Map<?,?> map, List<?> removeList)
			throws EvaluationException {
		for (Object item : removeList) {
			Value itemVal = new Value(item);
			removeFromMap(map, itemVal);
		}
	}
	
	private void removeFromMap(Map<?,?> map, Value val)
			throws EvaluationException {
		if (!val.isString() && !val.isNumber()) {
			throw new EvaluationException(
					"Remove key from map must be a string or number, found: " +
					val.getTypeString());
		}
		String key = val.toString();
		map.remove(key);
	}
	
	private void subtractList(List<?> list, List<?> removeList) {
		for (Object item : removeList) {
			Value itemVal = new Value(item);
			removeFromList(list, itemVal);
		}
	}
	
	private void removeFromList(List<?> list, Value val) {
		int i = 0;
		while (i < list.size()) {
			Value item = new Value(list.get(i));
			if (item.isEqual(val)) {
				list.remove(i);
			} else {
				i++;
			}
		}
	}
	
	@Override
	public String toString() {
		return operand1 + " - " + operand2;
	}
}
