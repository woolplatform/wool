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
