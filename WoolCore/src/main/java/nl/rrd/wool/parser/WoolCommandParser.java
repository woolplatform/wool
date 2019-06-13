package nl.rrd.wool.parser;

import java.util.List;

import nl.rrd.wool.exception.LineNumberParseException;
import nl.rrd.wool.model.command.WoolActionCommand;
import nl.rrd.wool.model.command.WoolCommand;
import nl.rrd.wool.model.command.WoolIfCommand;
import nl.rrd.wool.model.command.WoolInputCommand;
import nl.rrd.wool.model.command.WoolSetCommand;
import nl.rrd.wool.utils.CurrentIterator;

public class WoolCommandParser {
	private List<String> validCommands;
	
	public WoolCommandParser(List<String> validCommands) {
		this.validCommands = validCommands;
	}
	
	public WoolCommand parse(CurrentIterator<WoolBodyToken> tokens)
			throws LineNumberParseException {
		WoolBodyToken startToken = tokens.getCurrent();
		tokens.moveNext();
		WoolBodyToken token = tokens.getCurrent();
		if (token == null) {
			throw new LineNumberParseException("Command not terminated",
					startToken.getLineNum(), startToken.getColNum());
		}
		if (token.getType() != WoolBodyToken.Type.TEXT) {
			throw new LineNumberParseException(
					"Expected command name, found token: " + token.getType(),
					token.getLineNum(), token.getColNum());
		}
		String name = (String)token.getValue();
		for (int i = 0; i < name.length(); i++) {
			if (Character.isWhitespace(name.charAt(i))) {
				name = name.substring(0, i);
				break;
			}
		}
		if (!validCommands.contains(name.toLowerCase())) {
			throw new LineNumberParseException("Unexpected command: " + name,
					token.getLineNum(), token.getColNum());
		}
		switch (name.toLowerCase()) {
		case "action":
			return WoolActionCommand.parse(tokens);
		case "if":
			return WoolIfCommand.parse(tokens);
		case "input":
			return WoolInputCommand.parse(tokens);
		case "set":
			return WoolSetCommand.parse(tokens);
		default:
			throw new LineNumberParseException("Unknown command: " + name,
					token.getLineNum(), token.getColNum());
		}
	}
}
