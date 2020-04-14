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

public class ValueExpression implements Expression {
	private Token token;
	
	public ValueExpression(Token token) {
		this.token = token;
	}

	public Token getToken() {
		return token;
	}

	@Override
	public Value evaluate(Map<String,Object> variables)
			throws EvaluationException {
		if (token.getType() == Token.Type.NAME ||
				token.getType() == Token.Type.DOLLAR_VARIABLE) {
			if (variables == null)
				return new Value(null);
			else
				return new Value(variables.get(token.getValue().toString()));
		} else {
			return token.getValue();
		}
	}

	@Override
	public List<Expression> getChildren() {
		return new ArrayList<>();
	}

	@Override
	public List<Expression> getDescendants() {
		return new ArrayList<>();
	}

	@Override
	public Set<String> getVariableNames() {
		Set<String> result = new HashSet<>();
		if (token.getType() == Token.Type.NAME ||
				token.getType() == Token.Type.DOLLAR_VARIABLE) {
			result.add(token.getValue().toString());
		}
		return result;
	}
	
	@Override
	public String toString() {
		return token.getText();
	}
}
