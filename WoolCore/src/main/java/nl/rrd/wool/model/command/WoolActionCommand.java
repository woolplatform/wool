package nl.rrd.wool.model.command;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import nl.rrd.wool.exception.LineNumberParseException;
import nl.rrd.wool.model.WoolNodeBody;
import nl.rrd.wool.model.WoolReply;
import nl.rrd.wool.model.WoolVariableString;
import nl.rrd.wool.parser.WoolBodyToken;
import nl.rrd.wool.utils.CurrentIterator;

/**
 * This command models the &lt;&lt;action ...&gt;&gt; command in Wool. It
 * specifies an action that should be performed along with a statement. It can
 * be part of a {@link WoolNodeBody WoolNodeBody} (along with an agent
 * statement) or a {@link WoolReply WoolReply} (to be performed when the user
 * chooses the reply).
 * 
 * @author Dennis Hofs (RRD)
 */
public class WoolActionCommand extends WoolAttributesCommand {
	public static final String TYPE_IMAGE = "image";
	public static final String TYPE_VIDEO = "video";
	public static final String TYPE_GENERIC = "generic";
	
	private static final List<String> VALID_TYPES = Arrays.asList(
			TYPE_IMAGE, TYPE_VIDEO, TYPE_GENERIC);
	
	private String type;
	private WoolVariableString value;
	private Map<String,WoolVariableString> parameters = new LinkedHashMap<>();
	
	public WoolActionCommand(String type, WoolVariableString value) {
		this.type = type;
		this.value = value;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public WoolVariableString getValue() {
		return value;
	}

	public void setValue(WoolVariableString value) {
		this.value = value;
	}

	public Map<String, WoolVariableString> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, WoolVariableString> parameters) {
		this.parameters = parameters;
	}
	
	public void addParameter(String name, WoolVariableString value) {
		parameters.put(name, value);
	}
	
	@Override
	public String toString() {
		char[] escapes = new char[] { '"' };
		StringBuilder result = new StringBuilder(
				"<<action type=\"" + type +
				"\" value=\"" + value.toString(escapes) + "\"");
		for (String key : parameters.keySet()) {
			result.append(" " + key + "=\"" +
					parameters.get(key).toString(escapes) + "\"");
		}
		result.append(">>");
		return result.toString();
	}
	
	public static WoolActionCommand parse(WoolBodyToken cmdStartToken,
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
		attrs.remove("type");
		WoolVariableString value = readAttr("value", attrs, cmdStartToken,
				true);
		attrs.remove("value");
		WoolActionCommand command = new WoolActionCommand(type, value);
		for (String attr : attrs.keySet()) {
			token = attrs.get(attr);
			command.addParameter(attr, (WoolVariableString)token.getValue());
		}
		return command;
	}
}
