package eu.woolplatform.wool.model.command;

import eu.woolplatform.utils.exception.LineNumberParseException;
import eu.woolplatform.wool.parser.WoolBodyToken;

import java.util.Map;

public class WoolInputLongtextCommand extends WoolInputAbstractTextCommand {
	public WoolInputLongtextCommand(String variableName) {
		super(TYPE_LONGTEXT, variableName);
	}

	public WoolInputLongtextCommand(WoolInputLongtextCommand other) {
		super(other);
	}

	@Override
	public WoolInputLongtextCommand clone() {
		return new WoolInputLongtextCommand(this);
	}

	public static WoolInputCommand parse(WoolBodyToken cmdStartToken,
			Map<String,WoolBodyToken> attrs) throws LineNumberParseException {
		String variableName = readVariableAttr("value", attrs, cmdStartToken,
				true);
		WoolInputLongtextCommand command = new WoolInputLongtextCommand(variableName);
		WoolInputAbstractTextCommand.parseAttributes(command, cmdStartToken,
				attrs);
		return command;
	}
}
