package eu.woolplatform.wool.model.command;

import eu.woolplatform.utils.exception.LineNumberParseException;
import eu.woolplatform.utils.expressions.EvaluationException;
import eu.woolplatform.utils.expressions.Value;
import eu.woolplatform.wool.execution.WoolVariable;
import eu.woolplatform.wool.execution.WoolVariableStore;
import eu.woolplatform.wool.model.WoolNodeBody;
import eu.woolplatform.wool.parser.WoolBodyToken;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class WoolInputEmailCommand extends WoolInputCommand {
	private String variableName;

	public WoolInputEmailCommand(String variableName) {
		super(TYPE_EMAIL);
		this.variableName = variableName;
	}

	public WoolInputEmailCommand(WoolInputEmailCommand other) {
		super(other);
		this.variableName = other.variableName;
	}

	public String getVariableName() {
		return variableName;
	}

	public void setVariableName(String variableName) {
		this.variableName = variableName;
	}

	@Override
	public Map<String, ?> getParameters() {
		Map<String,Object> result = new LinkedHashMap<>();
		result.put("variableName", variableName);
		return result;
	}

	@Override
	public String getStatementLog(WoolVariableStore varStore) {
		WoolVariable woolVariable = varStore.getWoolVariable(variableName);
		Value value = new Value(woolVariable.getValue());
		return value.toString();
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
	public WoolInputEmailCommand clone() {
		return new WoolInputEmailCommand(this);
	}

	@Override
	public String toString() {
		String result = toStringStart();
		result += " value=\"$" + variableName + "\">>";
		return result;
	}

	public static WoolInputCommand parse(WoolBodyToken cmdStartToken,
			Map<String,WoolBodyToken> attrs) throws LineNumberParseException {
		String variableName = readVariableAttr("value", attrs, cmdStartToken,
				true);
		return new WoolInputEmailCommand(variableName);
	}
}
