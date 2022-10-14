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

package eu.woolplatform.wool.model.protocol;

import eu.woolplatform.wool.model.WoolVariableString;
import eu.woolplatform.wool.model.command.WoolActionCommand;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This class is used for dialogue actions that are sent to the client in the
 * web service protocol. It mirrors {@link WoolActionCommand WoolActionCommand}
 * except that any variables in strings have been resolved.
 *
 * @author Dennis Hofs (RRD)
 */
public class DialogueAction {
	private String type;
	private String value;
	private Map<String,String> parameters = new LinkedHashMap<>();
	
	public DialogueAction() {
	}
	
	public DialogueAction(WoolActionCommand actionCommand) {
		type = actionCommand.getType();
		value = actionCommand.getValue().evaluate(null);
		Map<String,WoolVariableString> cmdParams =
				actionCommand.getParameters();
		for (String key : cmdParams.keySet()) {
			String paramVal = cmdParams.get(key).evaluate(null);
			parameters.put(key, paramVal);
		}
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Map<String,String> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String,String> parameters) {
		this.parameters = parameters;
	}
}
