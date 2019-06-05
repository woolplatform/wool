package nl.rrd.wool.expressions.types;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import nl.rrd.wool.expressions.EvaluationException;
import nl.rrd.wool.expressions.Expression;
import nl.rrd.wool.expressions.Value;

public class AddExpression implements Expression {
	private Expression operand1;
	private Expression operand2;
	
	public AddExpression(Expression operand1, Expression operand2) {
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
		if (val1.isMap() || val2.isMap()) {
			return mergeMaps(val1, val2);
		} else if (val1.isList() || val2.isList()) {
			return mergeLists(val1, val2);
		} else if (val1.isString() || val2.isString()) {
			return new Value(val1.toString() + val2.toString());
		} else {
			Number num1 = val1.asNumber();
			Number num2 = val2.asNumber();
			if (Value.isIntNumber(num1) && Value.isIntNumber(num2)) {
				return new Value(Value.normalizeNumber(num1.longValue() +
						num2.longValue()));
			} else {
				return new Value(num1.doubleValue() + num2.doubleValue());
			}
		}
	}
	
	private Value mergeMaps(Value val1, Value val2) throws EvaluationException {
		Value nonMap = null;
		if (!val1.isMap())
			nonMap = val1;
		else if (!val2.isMap())
			nonMap = val2;
		if (nonMap != null) {
			throw new EvaluationException("Can't add map and " +
					nonMap.getTypeString());
		}
		Map<String,Object> result = new LinkedHashMap<>();
		Map<?,?> map1 = (Map<?,?>)val1.getValue();
		Map<?,?> map2 = (Map<?,?>)val2.getValue();
		for (Object key : map1.keySet()) {
			result.put((String)key, map1.get(key));
		}
		for (Object key : map2.keySet()) {
			result.put((String)key, map2.get(key));
		}
		return new Value(result);
	}
	
	private Value mergeLists(Value val1, Value val2) {
		List<?> list1;
		if (val1.isList())
			list1 = (List<?>)val1.getValue();
		else
			list1 = Collections.singletonList(val1.getValue());
		List<?> list2;
		if (val2.isList())
			list2 = (List<?>)val2.getValue();
		else
			list2 = Collections.singletonList(val2.getValue());
		List<Object> result = new ArrayList<>(list1);
		result.addAll(list2);
		return new Value(result);
	}

	@Override
	public String toString() {
		return operand1 + " + " + operand2;
	}
}
