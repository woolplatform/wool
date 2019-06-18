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

package nl.rrd.wool.expressions.types;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.rrd.wool.exception.LineNumberParseException;
import nl.rrd.wool.expressions.EvaluationException;
import nl.rrd.wool.expressions.Expression;
import nl.rrd.wool.expressions.Token;
import nl.rrd.wool.expressions.Value;

public class AssignExpression implements Expression {
	private Expression variableOperand;
	private String variableName;
	private Expression valueOperand;
	
	public AssignExpression(Expression variableOperand, Token operator,
			Expression valueOperand) throws LineNumberParseException {
		if (!(variableOperand instanceof ValueExpression)) {
			throw new LineNumberParseException(
					"First operand of assign expression must be a variable",
					operator.getLineNum(), operator.getColNum());
		}
		ValueExpression variableExpr = (ValueExpression)variableOperand;
		Token variableToken = variableExpr.getToken();
		if (variableToken.getType() != Token.Type.NAME &&
				variableToken.getType() != Token.Type.DOLLAR_VARIABLE) {
			throw new LineNumberParseException(
					"First operand of assign expression must be a variable",
					operator.getLineNum(), operator.getColNum());
		}
		this.variableOperand = variableOperand;
		this.variableName = variableToken.getValue().toString();
		this.valueOperand = valueOperand;
	}

	public Expression getVariableOperand() {
		return variableOperand;
	}

	public String getVariableName() {
		return variableName;
	}

	public Expression getValueOperand() {
		return valueOperand;
	}

	@Override
	public Value evaluate(Map<String, Object> variables)
			throws EvaluationException {
		Value result = valueOperand.evaluate(variables);
		if (variables != null)
			variables.put(variableName, result.getValue());
		return result;
	}

	@Override
	public List<Expression> getChildren() {
		List<Expression> result = new ArrayList<>();
		result.add(variableOperand);
		result.add(valueOperand);
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
		result.add(variableName);
		result.addAll(valueOperand.getVariableNames());
		return result;
	}
	
	@Override
	public String toString() {
		return variableOperand + " = " + valueOperand;
	}
}
