/*
 * Copyright 2019 Roessingh Research and Development.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package eu.woolplatform.utils.expressions.types;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.woolplatform.utils.expressions.EvaluationException;
import eu.woolplatform.utils.expressions.Expression;
import eu.woolplatform.utils.expressions.Value;

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
	public Value evaluate(Map<String,Object> variables)
			throws EvaluationException {
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
		return operand1 + " - " + operand2;
	}
}
