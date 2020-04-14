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

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.woolplatform.utils.CurrentIterator;
import eu.woolplatform.utils.exception.LineNumberParseException;
import eu.woolplatform.utils.expressions.EvaluationException;
import eu.woolplatform.wool.model.WoolNodeBody;
import eu.woolplatform.wool.model.WoolReply;
import eu.woolplatform.wool.model.WoolVariableString;
import eu.woolplatform.wool.model.nodepointer.WoolNodePointer;
import eu.woolplatform.wool.parser.WoolBodyToken;
import eu.woolplatform.wool.parser.WoolNodeState;

/**
 * This command models the &lt;&lt;action ...&gt;&gt; command in WOOL. It
 * specifies an action that should be performed along with a statement. It can
 * be part of a {@link WoolNodeBody WoolNodeBody} (along with an agent
 * statement) or a {@link WoolReply WoolReply} (to be performed when the user
 * chooses the reply).
 *
 * Three different action commands are supported:
 * <ul>
 *     <li>image</li>
 *     <li>video</li>
 *     <li>generic</li>
 * </ul>
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

	/**
	 * Creates an instance of a {@link WoolActionCommand} with given {@code type} and
	 * {@code value}.
	 * @param type the type of this {@link WoolActionCommand} as a String, which should be
	 *                one of "image", "video", or "generic".
	 * @param value the value of this command
	 */
	public WoolActionCommand(String type, WoolVariableString value) {
		this.type = type;
		this.value = value;
	}

	public WoolActionCommand(WoolActionCommand other) {
		this.type = other.type;
		this.value = new WoolVariableString(other.value);
		for (String key : other.parameters.keySet()) {
			this.parameters.put(key, new WoolVariableString(
					other.parameters.get(key)));
		}
	}

	/**
	 * Returns the type of this {@link WoolActionCommand} as a String.
	 * @return the type of this {@link WoolActionCommand} as a String.
	 */
	public String getType() {
		return type;
	}

	/**
	 * Sets the type of this {@link WoolActionCommand}, which should be one of "image",
	 * "video", or "generic".
	 * @param type the type of this {@link WoolActionCommand}.
	 */
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
	public WoolReply findReplyById(int replyId) {
		return null;
	}

	@Override
	public void getReadVariableNames(Set<String> varNames) {
		value.getReadVariableNames(varNames);
		for (WoolVariableString paramVals : parameters.values()) {
			paramVals.getReadVariableNames(varNames);
		}
	}

	@Override
	public void getWriteVariableNames(Set<String> varNames) {
	}

	@Override
	public void getNodePointers(Set<WoolNodePointer> pointers) {
	}

	@Override
	public void executeBodyCommand(Map<String, Object> variables,
			WoolNodeBody processedBody) throws EvaluationException {
		WoolActionCommand processedCommand = new WoolActionCommand(type,
				value.execute(variables));
		for (String param : parameters.keySet()) {
			WoolVariableString value = parameters.get(param);
			processedCommand.addParameter(param, value.execute(variables));
		}
		processedBody.addSegment(new WoolNodeBody.CommandSegment(
				processedCommand));
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

	@Override
	public WoolActionCommand clone() {
		return new WoolActionCommand(this);
	}
}
