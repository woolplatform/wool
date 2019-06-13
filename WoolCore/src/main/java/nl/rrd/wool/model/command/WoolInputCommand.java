package nl.rrd.wool.model.command;

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
public class WoolInputCommand extends WoolCommand {
	public static final String TYPE_TEXT = "text";
	public static final String TYPE_NUMERIC = "numeric";
	
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

	public static WoolActionCommand parse(CurrentIterator<WoolBodyToken> tokens)
			throws LineNumberParseException {
		// TODO
		return null;
	}
}
