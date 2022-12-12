/*
 * Copyright 2019-2022 WOOL Foundation - Licensed under the MIT License:
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package eu.woolplatform.wool.model.command;

import nl.rrd.utils.CurrentIterator;
import nl.rrd.utils.exception.LineNumberParseException;
import eu.woolplatform.wool.execution.WoolVariableStore;
import eu.woolplatform.wool.model.WoolNodeBody;
import eu.woolplatform.wool.model.WoolReply;
import eu.woolplatform.wool.model.WoolVariableString;
import eu.woolplatform.wool.model.nodepointer.WoolNodePointer;
import eu.woolplatform.wool.parser.WoolBodyToken;
import eu.woolplatform.wool.parser.WoolNodeState;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class models the &lt;&lt;input ...&gt;&gt; command in Wool. It can
 * be part of a {@link WoolNodeBody WoolNodeBody} inside a reply.
 * 
 * @author Dennis Hofs (RRD)
 */
public abstract class WoolInputCommand extends WoolAttributesCommand {
	public static final String TYPE_EMAIL = "email";
	public static final String TYPE_TEXT = "text";
	public static final String TYPE_LONGTEXT = "longtext";
	public static final String TYPE_NUMERIC = "numeric";
	public static final String TYPE_SET = "set";
	public static final String TYPE_TIME = "time";

	private static final List<String> VALID_TYPES = Arrays.asList(TYPE_EMAIL,
			TYPE_TEXT, TYPE_LONGTEXT, TYPE_NUMERIC, TYPE_SET, TYPE_TIME);
	
	private String type;
	private String description = null;

	public WoolInputCommand(String type) {
		this.type = type;
	}

	public WoolInputCommand(WoolInputCommand other) {
		this.type = other.type;
		this.description = other.description;
	}

	/**
	 * Returns the type of input command. This should be one of the TYPE_*
	 * constants defined in this class.
	 *
	 * @return the type of input command
	 */
	public String getType() {
		return type;
	}

	/**
	 * Sets the type of input command. This should be one of the TYPE_*
	 * constants defined in this class.
	 *
	 * @param type the type of input command
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * Returns the description of this input command. For example a client can
	 * use this in input validation messages ("You did not fill in [your
	 * name]."). The description is optional and may be null.
	 *
	 * @return the description or null
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the description of this input command. For example a client can use
	 * this in input validation messages ("You did not fill in [your name].").
	 * The description is optional and may be null.
	 *
	 * @param description the description or null
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Returns the parameters for this input command to send to the client. This
	 * is a map from parameter names to values. A value can be any JSON type.
	 * This method should only be called on a command that has already been
	 * executed with {@link #executeBodyCommand(Map, WoolNodeBody)
	 * executeBodyCommand()}. This means that any variables in parameter values
	 * have already been resolved.
	 *
	 * @return the parameters for this input command to send to the client
	 */
	public abstract Map<String,?> getParameters();

	/**
	 * Returns the string to use in the user statement log in place of this
	 * input command. It can use variable values from the specified variable
	 * store. This method should only be called on a command that has already
	 * been executed with {@link #executeBodyCommand(Map, WoolNodeBody)
	 * executeBodyCommand()}. This means that any variables in parameter values
	 * have already been resolved.
	 *
	 * @param varStore the variable store
	 * @return the statement log
	 */
	public abstract String getStatementLog(WoolVariableStore varStore);

	@Override
	public WoolReply findReplyById(int replyId) {
		return null;
	}

	@Override
	public void getNodePointers(Set<WoolNodePointer> pointers) {
	}

	public static WoolInputCommand parse(WoolBodyToken cmdStartToken,
			CurrentIterator<WoolBodyToken> tokens, WoolNodeState nodeState)
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
		WoolInputCommand result;
		switch (type) {
			case TYPE_EMAIL:
				result = WoolInputEmailCommand.parse(cmdStartToken, attrs);
				break;
			case TYPE_TEXT:
				result = WoolInputTextCommand.parse(cmdStartToken, attrs);
				break;
			case TYPE_LONGTEXT:
				result = WoolInputLongtextCommand.parse(cmdStartToken, attrs);
				break;
			case TYPE_NUMERIC:
				result = WoolInputNumericCommand.parse(cmdStartToken, attrs);
				break;
			case TYPE_SET:
				result = WoolInputSetCommand.parse(cmdStartToken, attrs);
				break;
			case TYPE_TIME:
				result = WoolInputTimeCommand.parse(cmdStartToken, attrs);
				break;
			default:
				throw new RuntimeException("Unsupported value for input type: " + type);
		}
		String description = readPlainTextAttr("description", attrs,
				cmdStartToken, false);
		if (description != null && !description.isEmpty())
			result.setDescription(description);
		return result;
	}

	protected String toStringStart() {
		String result = "<<input type=\"" + type + "\"";
		if (description != null) {
			char[] escapes = new char[] { '"' };
			String escapedDescr = new WoolVariableString(description)
					.toString(escapes);
			result += " description=\"" + escapedDescr + "\"";
		}
		return result;
	}
}
