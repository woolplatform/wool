/*
 * Copyright 2019 Roessingh Research and Development.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the "Software"), 
 * to deal in the Software without restriction, including without limitation 
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, 
 * and/or sell copies of the Software, and to permit persons to whom the 
 * Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER 
 * DEALINGS IN THE SOFTWARE.
 */

package eu.woolplatform.wool.model.command;

import eu.woolplatform.utils.CurrentIterator;
import eu.woolplatform.utils.exception.LineNumberParseException;
import eu.woolplatform.wool.execution.WoolVariableStore;
import eu.woolplatform.wool.model.WoolNodeBody;
import eu.woolplatform.wool.model.WoolReply;
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
	public static final String TYPE_TEXT = "text";
	public static final String TYPE_LONGTEXT = "longtext";
	public static final String TYPE_NUMERIC = "numeric";
	public static final String TYPE_SET = "set";
	public static final String TYPE_TIME = "time";

	private static final List<String> VALID_TYPES = Arrays.asList(
			TYPE_TEXT, TYPE_LONGTEXT, TYPE_NUMERIC, TYPE_SET, TYPE_TIME);
	
	private String type;

	public WoolInputCommand(String type) {
		this.type = type;
	}

	public WoolInputCommand(WoolInputCommand other) {
		this.type = other.type;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
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
		switch (type) {
			case TYPE_TEXT:
				return WoolInputTextCommand.parse(cmdStartToken, attrs);
			case TYPE_LONGTEXT:
				return WoolInputLongtextCommand.parse(cmdStartToken, attrs);
			case TYPE_NUMERIC:
				return WoolInputNumericCommand.parse(cmdStartToken, attrs);
			case TYPE_SET:
				return WoolInputSetCommand.parse(cmdStartToken, attrs);
			case TYPE_TIME:
				return WoolInputTimeCommand.parse(cmdStartToken, attrs);
		}
		throw new RuntimeException("Unsupported value for input type: " + type);
	}
}
