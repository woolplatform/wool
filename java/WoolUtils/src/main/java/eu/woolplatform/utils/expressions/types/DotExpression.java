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
import eu.woolplatform.utils.expressions.Token;
import eu.woolplatform.utils.expressions.Value;

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
