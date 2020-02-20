package nl.rrd.wool.model.command;

import nl.rrd.wool.exception.LineNumberParseException;
import nl.rrd.wool.execution.WoolVariableStore;
import nl.rrd.wool.expressions.EvaluationException;
import nl.rrd.wool.expressions.Value;
import nl.rrd.wool.json.JsonMapper;
import nl.rrd.wool.model.WoolNodeBody;
import nl.rrd.wool.model.WoolVariableString;
import nl.rrd.wool.parser.WoolBodyToken;

import java.util.*;

public class WoolInputSetCommand extends WoolInputCommand {
	private List<Option> options = new ArrayList<>();

	public WoolInputSetCommand() {
		super(TYPE_SET);
	}

	public WoolInputSetCommand(WoolInputSetCommand other) {
		super(other);
		for (Option option : other.options) {
			this.options.add(new Option(option));
		}
	}

	public List<Option> getOptions() {
		return options;
	}

	public void setOptions(List<Option> options) {
		this.options = options;
	}

	@Override
	public Map<String, ?> getParameters() {
		Map<String,Object> result = new LinkedHashMap<>();
		List<Map<String,String>> processedOptions = new ArrayList<>();
		result.put("options", processedOptions);
		for (Option option : options) {
			Map<String,String> processedOption = new LinkedHashMap<>();
			processedOption.put("variableName", option.getVariableName());
			processedOption.put("text", option.getText().evaluate(null));
			processedOptions.add(processedOption);
		}
		return result;
	}

	@Override
	public String getStatementLog(WoolVariableStore varStore) {
		List<String> optionTexts = new ArrayList<>();
		for (Option option : options) {
			Value value = new Value(varStore.getValue(
					option.getVariableName()));
			if (value.asBoolean())
				optionTexts.add(option.getText().evaluate(null));
		}
		return JsonMapper.generate(optionTexts);
	}

	@Override
	public void getReadVariableNames(Set<String> varNames) {
		for (Option option : options) {
			option.getText().getReadVariableNames(varNames);
		}
	}

	@Override
	public void getWriteVariableNames(Set<String> varNames) {
		for (Option option : options) {
			varNames.add(option.getVariableName());
		}
	}

	@Override
	public void executeBodyCommand(Map<String, Object> variables,
			WoolNodeBody processedBody) throws EvaluationException {
		WoolInputSetCommand processedCmd = new WoolInputSetCommand();
		for (Option option : options) {
			Option processedOption = new Option();
			processedOption.setVariableName(option.getVariableName());
			processedOption.setText(option.getText().execute(variables));
			processedCmd.options.add(processedOption);
		}
		processedBody.addSegment(new WoolNodeBody.CommandSegment(processedCmd));
	}

	@Override
	public String toString() {
		char[] escapes = new char[] { '"' };
		StringBuilder builder = new StringBuilder("<<input type=\"" +
				TYPE_SET + "\"");
		for (int i = 0; i < options.size(); i++) {
			Option option = options.get(i);
			builder.append(" value" + (i+1) + "=\"$" +
					option.getVariableName() + "\"");
			builder.append(" option" + (i+1) + "=\"" +
					option.getText().toString(escapes) + "\"");
		}
		builder.append(">>");
		return builder.toString();
	}

	@Override
	public WoolInputSetCommand clone() {
		return new WoolInputSetCommand(this);
	}

	public static WoolInputSetCommand parse(WoolBodyToken cmdStartToken,
			Map<String,WoolBodyToken> attrs) throws LineNumberParseException {
		WoolInputSetCommand result = new WoolInputSetCommand();
		int index = 1;
		while (true) {
			WoolBodyToken valueToken = attrs.get("value" + index);
			WoolBodyToken optionToken = attrs.get("option" + index);
			if (valueToken == null && optionToken == null) {
				return result;
			} else if (valueToken != null && optionToken == null) {
				throw new LineNumberParseException(String.format(
						"Found attribute \"%s\" without attribute \"%s\"",
						"value" + index, "option" + index),
						cmdStartToken.getLineNum(), cmdStartToken.getColNum());
			} else if (valueToken == null && optionToken != null) {
				throw new LineNumberParseException(String.format(
						"Found attribute \"%s\" without attribute \"%s\"",
						"option" + index, "value" + index),
						cmdStartToken.getLineNum(), cmdStartToken.getColNum());
			}
			Option option = new Option();
			option.setVariableName(readVariableAttr("value" + index, attrs,
					cmdStartToken, true));
			option.setText(readAttr("option" + index, attrs, cmdStartToken,
					true));
			result.options.add(option);
			index++;
		}
	}

	public static class Option {
		private String variableName = null;
		private WoolVariableString text = null;

		public Option() {
		}

		public Option(Option other) {
			this.variableName = other.variableName;
			if (other.text != null)
				this.text = new WoolVariableString(other.text);
		}

		public String getVariableName() {
			return variableName;
		}

		public void setVariableName(String variableName) {
			this.variableName = variableName;
		}

		public WoolVariableString getText() {
			return text;
		}

		public void setText(WoolVariableString text) {
			this.text = text;
		}
	}
}
