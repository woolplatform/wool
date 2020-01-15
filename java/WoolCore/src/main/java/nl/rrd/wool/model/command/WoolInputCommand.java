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

package nl.rrd.wool.model.command;

import nl.rrd.wool.exception.LineNumberParseException;
import nl.rrd.wool.execution.WoolVariableStore;
import nl.rrd.wool.model.WoolNodeBody;
import nl.rrd.wool.model.WoolReply;
import nl.rrd.wool.model.nodepointer.WoolNodePointer;
import nl.rrd.wool.parser.WoolBodyToken;
import nl.rrd.wool.parser.WoolNodeState;
import nl.rrd.wool.utils.CurrentIterator;

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
	public static final String TYPE_NUMERIC = "numeric";

	private static final List<String> VALID_TYPES = Arrays.asList(
			TYPE_TEXT, TYPE_NUMERIC);
	
	private String type;

	public WoolInputCommand(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public abstract Map<String,?> getParameters();

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
			case TYPE_NUMERIC:
				return WoolInputNumericCommand.parse(cmdStartToken, attrs);
		}
		throw new RuntimeException("Unsupported value for input type: " + type);
	}
}
