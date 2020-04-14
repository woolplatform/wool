package eu.woolplatform.wool.model.command;

import eu.woolplatform.utils.exception.LineNumberParseException;
import eu.woolplatform.wool.execution.WoolVariableStore;
import eu.woolplatform.utils.expressions.EvaluationException;
import eu.woolplatform.utils.expressions.Value;
import eu.woolplatform.wool.model.WoolNodeBody;
import eu.woolplatform.wool.model.WoolVariableString;
import eu.woolplatform.wool.parser.WoolBodyToken;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class WoolInputTimeCommand extends WoolInputCommand {
	public static final String TIME_NOW = "now";

	private String variableName;
	private int granularityMinutes = 1;
	private WoolVariableString startTime = null;
	private WoolVariableString minTime = null;
	private WoolVariableString maxTime = null;

	public WoolInputTimeCommand(String variableName) {
		super(TYPE_TIME);
		this.variableName = variableName;
	}

	public WoolInputTimeCommand(WoolInputTimeCommand other) {
		super(other);
		this.variableName = other.variableName;
		this.granularityMinutes = other.granularityMinutes;
		if (other.startTime != null)
			this.startTime = new WoolVariableString(other.startTime);
		if (other.minTime != null)
			this.minTime = new WoolVariableString(other.minTime);
		if (other.maxTime != null)
			this.maxTime = new WoolVariableString(other.maxTime);
	}

	public String getVariableName() {
		return variableName;
	}

	public void setVariableName(String variableName) {
		this.variableName = variableName;
	}

	public int getGranularityMinutes() {
		return granularityMinutes;
	}

	public void setGranularityMinutes(int granularityMinutes) {
		this.granularityMinutes = granularityMinutes;
	}

	public WoolVariableString getStartTime() {
		return startTime;
	}

	public void setStartTime(WoolVariableString startTime) {
		this.startTime = startTime;
	}

	public WoolVariableString getMinTime() {
		return minTime;
	}

	public void setMinTime(WoolVariableString minTime) {
		this.minTime = minTime;
	}

	public WoolVariableString getMaxTime() {
		return maxTime;
	}

	public void setMaxTime(WoolVariableString maxTime) {
		this.maxTime = maxTime;
	}

	@Override
	public Map<String, ?> getParameters() {
		Map<String,Object> result = new LinkedHashMap<>();
		result.put("variableName", variableName);
		result.put("granularityMinutes", granularityMinutes);
		if (startTime != null)
			result.put("startTime", startTime.evaluate(null));
		if (minTime != null)
			result.put("minTime", minTime.evaluate(null));
		if (maxTime != null)
			result.put("maxTime", maxTime.evaluate(null));
		return result;
	}

	@Override
	public String getStatementLog(WoolVariableStore varStore) {
		Value value = new Value(varStore.getValue(variableName));
		return value.toString();
	}

	@Override
	public void getReadVariableNames(Set<String> varNames) {
		if (startTime != null)
			startTime.getReadVariableNames(varNames);
		if (minTime != null)
			minTime.getReadVariableNames(varNames);
		if (maxTime != null)
			maxTime.getReadVariableNames(varNames);
	}

	@Override
	public void getWriteVariableNames(Set<String> varNames) {
		varNames.add(variableName);
	}

	@Override
	public void executeBodyCommand(Map<String, Object> variables,
			WoolNodeBody processedBody) throws EvaluationException {
		WoolInputTimeCommand processedCmd = new WoolInputTimeCommand(
				variableName);
		processedCmd.granularityMinutes = granularityMinutes;
		if (startTime != null) {
			processedCmd.startTime = evaluateTime(startTime.evaluate(
					variables));
		}
		if (minTime != null) {
			processedCmd.minTime = evaluateTime(minTime.evaluate(variables));
		}
		if (maxTime != null) {
			processedCmd.maxTime = evaluateTime(maxTime.evaluate(variables));
		}
		processedBody.addSegment(new WoolNodeBody.CommandSegment(processedCmd));
	}

	@Override
	public WoolInputTimeCommand clone() {
		return new WoolInputTimeCommand(this);
	}

	private static WoolVariableString evaluateTime(String text)
			throws EvaluationException {
		if (text.toLowerCase().equals(TIME_NOW))
			return new WoolVariableString(TIME_NOW);
		DateTimeFormatter parser = ISODateTimeFormat.localTimeParser();
		LocalTime time;
		try {
			time = parser.parseLocalTime(text);
		} catch (IllegalArgumentException ex) {
			throw new EvaluationException("Invalid local time value: " + text);
		}
		return new WoolVariableString(time.toString("HH:mm"));
	}

	@Override
	public String toString() {
		char[] escapes = new char[] { '"' };
		StringBuilder builder = new StringBuilder("<<input type=\"" +
				TYPE_TIME + "\"");
		builder.append(" value=\"$" + variableName + "\"");
		builder.append(" granularityMinutes=\"" + granularityMinutes + "\"");
		if (startTime != null) {
			builder.append(" startTime=\"" + startTime.toString(escapes) +
					"\"");
		}
		if (minTime != null)
			builder.append(" minTime=\"" + minTime.toString(escapes) + "\"");
		if (maxTime != null)
			builder.append(" maxTime=\"" + maxTime.toString(escapes) + "\"");
		builder.append(">>");
		return builder.toString();
	}

	public static WoolInputTimeCommand parse(WoolBodyToken cmdStartToken,
			Map<String,WoolBodyToken> attrs) throws LineNumberParseException {
		String variableName = readVariableAttr("value", attrs, cmdStartToken,
				true);
		WoolInputTimeCommand command = new WoolInputTimeCommand(variableName);
		Integer granularityMinutes = readIntAttr("granularityMinutes", attrs,
				cmdStartToken, false, 1, null);
		if (granularityMinutes != null)
			command.granularityMinutes = granularityMinutes;
		command.startTime = readTimeAttribute("startTime", attrs,
				cmdStartToken);
		command.minTime = readTimeAttribute("minTime", attrs, cmdStartToken);
		command.maxTime = readTimeAttribute("maxTime", attrs, cmdStartToken);
		return command;
	}

	private static WoolVariableString readTimeAttribute(String attrName,
			Map<String,WoolBodyToken> attrs, WoolBodyToken cmdStartToken)
			throws LineNumberParseException {
		WoolVariableString result = readAttr(attrName, attrs, cmdStartToken,
				false);
		if (result == null || !result.isPlainText())
			return result;
		WoolBodyToken token = attrs.get(attrName);
		String value = result.evaluate(null);
		try {
			return evaluateTime(value);
		} catch (EvaluationException ex) {
			throw new LineNumberParseException(String.format(
					"Invalid value for attribute \"%s\"", attrName) + ": " +
					ex.getMessage(), token.getLineNum(), token.getColNum(), ex);
		}
	}
}
