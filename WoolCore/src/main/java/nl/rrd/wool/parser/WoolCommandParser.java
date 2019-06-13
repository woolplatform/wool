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
		WoolBodyToken.skipWhitespace(tokens);
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
		String name = token.getText().trim();
		String[] split = name.split("\\s+", 2);
		name = split[0];
		if (!validCommands.contains(name)) {
			throw new LineNumberParseException("Unexpected command: " + name,
					token.getLineNum(), token.getColNum());
		}
		switch (name) {
		case "action":
			return WoolActionCommand.parse(startToken, tokens);
		case "if":
			return WoolIfCommand.parse(startToken, tokens);
		case "input":
			return WoolInputCommand.parse(startToken, tokens);
		case "set":
			return WoolSetCommand.parse(startToken, tokens);
		default:
			throw new LineNumberParseException("Unknown command: " + name,
					token.getLineNum(), token.getColNum());
		}
	}
}
