package nl.rrd.wool.model.command;

import nl.rrd.wool.exception.LineNumberParseException;
import nl.rrd.wool.execution.WoolVariableStore;
import nl.rrd.wool.expressions.EvaluationException;
import nl.rrd.wool.expressions.Value;
import nl.rrd.wool.model.WoolNodeBody;
import nl.rrd.wool.parser.WoolBodyToken;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class WoolInputTextCommand extends WoolInputCommand {
	private String variableName;
	private Integer min = null;
	private Integer max = null;

	public WoolInputTextCommand(String variableName) {
		super(TYPE_TEXT);
		this.variableName = variableName;
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
		Value value = new Value(varStore.getValue(variableName));
		return value.toString();
	}

	@Override
	public String toString() {
		String result = "<<input type=\"" + TYPE_TEXT + "\"" +
				" value=\"$" + variableName + "\"";
		if (min != null)
			result += " min=\"" + min + "\"";
		if (max != null)
			result += " max=\"" + max + "\"";
		result += ">>";
		return result;
	}

	public static WoolInputCommand parse(WoolBodyToken cmdStartToken,
			Map<String,WoolBodyToken> attrs) throws LineNumberParseException {
		String variableName = readVariableAttr("value", attrs, cmdStartToken,
				true);
		WoolInputTextCommand command = new WoolInputTextCommand(variableName);
		Integer min = readIntAttr("min", attrs, cmdStartToken, false, null,
				null);
		command.setMin(min);
		Integer max = readIntAttr("max", attrs, cmdStartToken, false, null,
				null);
		command.setMax(max);
		return command;
	}
}
