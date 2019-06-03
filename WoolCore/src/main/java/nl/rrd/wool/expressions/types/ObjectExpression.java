package nl.rrd.wool.expressions.types;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import nl.rrd.wool.expressions.EvaluationException;
import nl.rrd.wool.expressions.Expression;
import nl.rrd.wool.expressions.Value;

public class ObjectExpression implements Expression {
	private List<KeyValue> properties;
	
	public ObjectExpression(List<KeyValue> properties) {
		this.properties = properties;
	}
	
	public List<KeyValue> getProperties() {
		return properties;
	}

	@Override
	public Value evaluate(Map<String,?> variables) throws EvaluationException {
		Map<String,Object> result = new LinkedHashMap<>();
		for (KeyValue prop : properties) {
			Value key = prop.key.evaluate(variables);
			Value val = prop.value.evaluate(variables);
			if (!key.isString() && !key.isNumber()) {
				throw new EvaluationException(
						"Map key must be a string or number, found: " +
						key.getTypeString());
			}
			result.put(key.toString(), val.getValue());
		}
		return new Value(result);
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (KeyValue prop : properties) {
			if (builder.length() > 0)
				builder.append(", ");
			builder.append(prop.key);
			builder.append(": ");
			builder.append(prop.value);
		}
		return "{" + builder + "}";
	}
	
	public static class KeyValue {
		private Expression key;
		private Expression value;
		
		public KeyValue(Expression key, Expression value) {
			this.key = key;
			this.value = value;
		}
	}
}
