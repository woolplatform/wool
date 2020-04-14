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
	public List<Expression> getChildren() {
		List<Expression> result = new ArrayList<>();
		result.add(parentOperand);
		result.add(indexOperand);
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
		return parentOperand + "[" + indexOperand + "]";
	}
}
