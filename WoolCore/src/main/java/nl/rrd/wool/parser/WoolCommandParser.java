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

	/**
	 * Reads the command name from the start of a command. The specified
	 * iterator should be positioned at the command start token. When this
	 * method returns, it will be positioned at the token with the command name.
	 * This method does not validate the command name.
	 * 
	 * @param tokens the tokens
	 * @return the command name
	 * @throws LineNumberParseException if a parsing error occurs
	 */
	public String readCommandName(CurrentIterator<WoolBodyToken> tokens)
			throws LineNumberParseException {
		WoolBodyToken startToken = tokens.getCurrent();
		tokens.moveNext();
		WoolBodyToken.skipWhitespace(tokens);
		WoolBodyToken token = tokens.getCurrent();
		return getCommandName(startToken, token);
	}
	
	/**
	 * Parses a command from the command name. The specified iterator should be
	 * positioned at the command name token. When this method returns it will be
	 * positioned after the command end token. This method can be called after
	 * {@link #readCommandName(CurrentIterator) readCommandName()}. This method
	 * validates the command name.
	 * 
	 * @param startToken the command start token
	 * @param tokens the tokens
	 * @return the command
	 * @throws LineNumberParseException if a parsing error occurs
	 */
	public WoolCommand parseFromName(WoolBodyToken startToken,
			CurrentIterator<WoolBodyToken> tokens)
			throws LineNumberParseException {
		WoolBodyToken token = tokens.getCurrent();
		String name = getCommandName(startToken, token);
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
	
	/**
	 * Parses a command from the start token. The specified iterator should be
	 * positioned at the command start token. When this method returns it will
	 * be positioned after the command end token. This method cannot be called
	 * after {@link #readCommandName(CurrentIterator) readCommandName()}. This
	 * method validates the command name.
	 * 
	 * @param tokens the tokens
	 * @return the command
	 * @throws LineNumberParseException if a parsing error occurs
	 */
	public WoolCommand parseFromStart(CurrentIterator<WoolBodyToken> tokens)
			throws LineNumberParseException {
		WoolBodyToken startToken = tokens.getCurrent();
		tokens.moveNext();
		WoolBodyToken.skipWhitespace(tokens);
		return parseFromName(startToken, tokens);
	}
	
	/**
	 * Tries to read the command name from the specified name token. This should
	 * be the first non-whitespace token after the command start token. It may
	 * be null.
	 * 
	 * @param startToken the start token
	 * @param nameToken the name token or null
	 * @return the command name
	 * @throws LineNumberParseException if the command name can't be read
	 */
	private String getCommandName(WoolBodyToken startToken,
			WoolBodyToken nameToken) throws LineNumberParseException {
		if (nameToken == null) {
			throw new LineNumberParseException("Command not terminated",
					startToken.getLineNum(), startToken.getColNum());
		}
		if (nameToken.getType() != WoolBodyToken.Type.TEXT) {
			throw new LineNumberParseException(
					"Expected command name, found token: " +
					nameToken.getType(), nameToken.getLineNum(),
					nameToken.getColNum());
		}
		String name = nameToken.getText().trim();
		String[] split = name.split("\\s+", 2);
		return split[0];
	}
}
