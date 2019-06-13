package nl.rrd.wool.model.command;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import nl.rrd.wool.exception.LineNumberParseException;
import nl.rrd.wool.model.WoolNodeBody;
import nl.rrd.wool.parser.WoolBodyToken;
import nl.rrd.wool.utils.CurrentIterator;

/**
 * This class models the &lt;&lt;input ...&gt;&gt; command in Wool. It can
 * be part of a {@link WoolNodeBody WoolNodeBody} inside a reply.
 * 
 * @author Dennis Hofs (RRD)
 */
public class WoolInputCommand extends WoolAttributesCommand {
	public static final String TYPE_TEXT = "text";
	public static final String TYPE_NUMERIC = "numeric";
	
	private static final List<String> VALID_TYPES = Arrays.asList(
			TYPE_TEXT, TYPE_NUMERIC);
	
	private String type;
	private String variableName;
	private Integer min = null;
	private Integer max = null;
	
	public WoolInputCommand(String type, String variableName) {
		this.type = type;
		this.variableName = variableName;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
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
	public String toString() {
		String result = "<<input type=\"" + type +
				"\" value=\"$" + variableName + "\"";
		if (min != null)
			result += " min=\"" + min + "\"";
		if (max != null)
			result += " max=\"" + max + "\"";
		result += ">>";
		return result;
	}

	public static WoolInputCommand parse(WoolBodyToken cmdStartToken,
			CurrentIterator<WoolBodyToken> tokens)
			throws LineNumberParseException {
		Map<String,WoolBodyToken> attrs = parseAttributesCommand(cmdStartToken,
				tokens);
		String type = readPlainTextAttr("type", attrs, cmdStartToken, true);
		WoolBodyToken token = attrs.get("type");
		if (!VALID_TYPES.contains(type)) {
			throw new LineNumberParseException(
					"Invalid value for attribute \"type\": " + type,
					token.getLineNum(), token.getColNum());
		}
		String variableName = readVariableAttr("value", attrs, cmdStartToken,
				true);
		WoolInputCommand command = new WoolInputCommand(type, variableName);
		Integer min = readIntAttr("min", attrs, cmdStartToken, false, null,
				null);
		command.setMin(min);
		Integer max = readIntAttr("max", attrs, cmdStartToken, false, null,
				null);
		command.setMax(max);
		return command;
	}
}
