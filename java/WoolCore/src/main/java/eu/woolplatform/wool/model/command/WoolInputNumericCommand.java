package eu.woolplatform.wool.model.command;

import eu.woolplatform.utils.exception.LineNumberParseException;
import eu.woolplatform.wool.execution.WoolVariable;
import eu.woolplatform.wool.execution.WoolVariableStore;
import eu.woolplatform.utils.expressions.EvaluationException;
import eu.woolplatform.utils.expressions.Value;
import eu.woolplatform.wool.model.WoolNodeBody;
import eu.woolplatform.wool.parser.WoolBodyToken;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class WoolInputNumericCommand extends WoolInputCommand {
	private String variableName;
	private Integer min = null;
	private Integer max = null;

	public WoolInputNumericCommand(String variableName) {
		super(TYPE_NUMERIC);
		this.variableName = variableName;
	}

	public WoolInputNumericCommand(WoolInputNumericCommand other) {
		super(other);
		this.variableName = other.variableName;
		this.min = other.min;
		this.max = other.max;
	}

	public String getVariableName() {
		return variableName;
	}

	public void setVariableName(String variableName) {
		this.variableName = variableName;
	}

	public Integer getMin() {
		return min;
	}

	public void setMin(Integer min) {
		this.min = min;
	}

	public Integer getMax() {
		return max;
	}

	public void setMax(Integer max) {
		this.max = max;
	}

	@Override
	public Map<String, ?> getParameters() {
		Map<String,Object> result = new LinkedHashMap<>();
		result.put("variableName", variableName);
		result.put("min", min);
		result.put("max", max);
		return result;
	}

	@Override
	public void getReadVariableNames(Set<String> varNames) {
	}

	@Override
	public void getWriteVariableNames(Set<String> varNames) {
		varNames.add(variableName);
	}

	@Override
	public void executeBodyCommand(Map<String, Object> variables,
			WoolNodeBody processedBody) throws EvaluationException {
		processedBody.addSegment(new WoolNodeBody.CommandSegment(this));
	}

	@Override
	public String getStatementLog(WoolVariableStore varStore) {
		WoolVariable woolVariable = varStore.getWoolVariable(variableName);
		Value value = new Value(woolVariable.getValue());
		return value.toString();
	}

	@Override
	public String toString() {
		String result = toStringStart();
		result += " value=\"$" + variableName + "\"";
		if (min != null)
			result += " min=\"" + min + "\"";
		if (max != null)
			result += " max=\"" + max + "\"";
		result += ">>";
		return result;
	}

	@Override
	public WoolInputNumericCommand clone() {
		return new WoolInputNumericCommand(this);
	}

	public static WoolInputCommand parse(WoolBodyToken cmdStartToken,
			Map<String,WoolBodyToken> attrs) throws LineNumberParseException {
		String variableName = readVariableAttr("value", attrs, cmdStartToken,
				true);
		WoolInputNumericCommand command = new WoolInputNumericCommand(
				variableName);
		Integer min = readIntAttr("min", attrs, cmdStartToken, false, null,
				null);
		command.setMin(min);
		Integer max = readIntAttr("max", attrs, cmdStartToken, false, null,
				null);
		command.setMax(max);
		return command;
	}
}
