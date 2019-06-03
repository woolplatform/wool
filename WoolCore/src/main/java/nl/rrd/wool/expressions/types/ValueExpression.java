package nl.rrd.wool.expressions.types;

import java.util.Map;

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
	public Value evaluate(Map<String,?> variables) throws EvaluationException {
		if (token.getType() == Token.Type.NAME) {
			if (variables == null)
				return new Value(null);
			else
				return new Value(variables.get(token.getValue().toString()));
		} else {
			return token.getValue();
		}
	}
	
	@Override
	public String toString() {
		return token.getText();
	}
}
