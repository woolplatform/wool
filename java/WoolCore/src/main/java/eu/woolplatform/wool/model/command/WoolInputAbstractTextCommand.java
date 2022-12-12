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

import nl.rrd.utils.exception.LineNumberParseException;
import nl.rrd.utils.expressions.EvaluationException;
import nl.rrd.utils.expressions.Value;
import eu.woolplatform.wool.execution.WoolVariable;
import eu.woolplatform.wool.execution.WoolVariableStore;
import eu.woolplatform.wool.model.WoolNodeBody;
import eu.woolplatform.wool.parser.WoolBodyToken;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public abstract class WoolInputAbstractTextCommand extends WoolInputCommand {
	private String variableName;
	private Integer min = null;
	private Integer max = null;
	private Boolean allowNumbers = Boolean.TRUE;
	private Boolean allowSpecialCharacters = Boolean.TRUE;
	private Boolean allowSpaces = Boolean.TRUE;
	private Boolean capCharacters = Boolean.FALSE;
	private Boolean capWords = Boolean.FALSE;
	private Boolean capSentences = Boolean.FALSE;
	private Boolean forceCapCharacters = Boolean.FALSE;
	private Boolean forceCapWords = Boolean.FALSE;
	private Boolean forceCapSentences = Boolean.FALSE;

	public WoolInputAbstractTextCommand(String type, String variableName) {
		super(type);
		this.variableName = variableName;
	}

	public WoolInputAbstractTextCommand(WoolInputAbstractTextCommand other) {
		super(other);
		this.variableName = other.variableName;
		this.min = other.min;
		this.max = other.max;
		this.allowNumbers = other.allowNumbers;
		this.allowSpecialCharacters = other.allowSpecialCharacters;
		this.allowSpaces = other.allowSpaces;
		this.capCharacters = other.capCharacters;
		this.capWords = other.capWords;
		this.capSentences = other.capSentences;
		this.forceCapCharacters = other.forceCapCharacters;
		this.forceCapWords = other.forceCapWords;
		this.forceCapSentences = other.forceCapSentences;
	}

	public String getVariableName() {
		return variableName;
	}

	public void setVariableName(String variableName) {
		this.variableName = variableName;
	}

	/**
	 * Returns the minimum number of characters allowed for this text input command,
	 * or {@code null} if no minimum is set.
	 * @return the minimum number of characters allowed for this text input command.
	 */
	public Integer getMin() {
		return min;
	}

	/**
	 * Sets the minimum number of characters needed for this text input command,
	 * or {@code null} if no minimum should be set.
	 * @param min the minimum number of characters needed for this text input command.
	 */
	public void setMin(Integer min) {
		this.min = min;
	}

	/**
	 * Returns the maximum number of characters allowed for this text input command,
	 * or {@code null} if no maximum is set.
	 * @return the maximum number of characters allowed for this text input command.
	 */
	public Integer getMax() {
		return max;
	}

	/**
	 * Sets the maximum number of characters allowed for this text input command,
	 * or {@code null} if no maximum should be set.
	 * @param max the maximum number of characters allowed for this text input command.
	 */
	public void setMax(Integer max) {
		this.max = max;
	}

	/**
	 * Returns whether or not numbers are allowed in this text input command.
	 * @return whether or not numbers are allowed in this text input command.
	 */
	public Boolean getAllowNumbers() {
		return allowNumbers;
	}

	/**
	 * Sets whether or not numbers are allowed in this text input command. If set to
	 * {@code null} the value reverts to its default value of {@code Boolean.TRUE}.
	 * @param allowNumbers whether or not numbers are allowed in this text input command.
	 */
	public void setAllowNumbers(Boolean allowNumbers) {
		if(allowNumbers != null) this.allowNumbers = allowNumbers;
		else this.allowNumbers = Boolean.TRUE;
	}

	/**
	 * Returns whether or not special characters are allowed in this text input command.
	 * @return whether or not special characters are allowed in this text input command.
	 */
	public Boolean getAllowSpecialCharacters() {
		return allowSpecialCharacters;
	}

	/**
	 * Sets whether or not special characters are allowed in this text input command.
	 * Special characters are defined as anything except letters [a-zA-Z], numbers
	 * [0-9] or the "space" character. If set to {@code null} the value reverts to its
	 * default value of {@code Boolean.TRUE}.
	 * @param allowSpecialCharacters whether or not special characters are allowed in this text input command.
	 */
	public void setAllowSpecialCharacters(Boolean allowSpecialCharacters) {
		if(allowSpecialCharacters != null) this.allowSpecialCharacters = allowSpecialCharacters;
		else this.allowSpecialCharacters = Boolean.TRUE;
	}

	/**
	 * Returns whether or not spaces are allowed in this text input command.
	 * @return whether or not spaces are allowed in this text input command.
	 */
	public Boolean getAllowSpaces() {
		return allowSpaces;
	}

	/**
	 * Sets whether or not spaces are allowed in this text input command. If set to
	 * {@code null} the value reverts to its default value of {@code Boolean.TRUE}.
	 * @param allowSpaces whether or not spaces are allowed in this text input command.
	 */
	public void setAllowSpaces(Boolean allowSpaces) {
		if(allowSpaces != null) this.allowSpaces = allowSpaces;
		else this.allowSpaces = Boolean.TRUE;
	}

	/**
	 * Returns whether or not to hint capitalization on character level.
	 * @return whether or not to hint capitalization on character level.
	 */
	public Boolean getCapCharacters() {
		return capCharacters;
	}

	/**
	 * Sets whether or not to hint capitalization on character level. If set to
	 * {@code null} the value reverts to its default value of {@code Boolean.FALSE}.
	 * @param capCharacters whether or not to hint capitalization on character level.
	 */
	public void setCapCharacters(Boolean capCharacters) {
		if(capCharacters != null) this.capCharacters = capCharacters;
		else this.capCharacters = Boolean.FALSE;
	}

	/**
	 * Returns whether or not to hint capitalization on word level.
	 * @return whether or not to hint capitalization on word level.
	 */
	public Boolean getCapWords() {
		return capWords;
	}

	/**
	 * Sets whether or not to hint capitalization on word level. If set to
	 * {@code null} the value reverts to its default value of {@code Boolean.FALSE}.
	 * @param capWords whether or not to hint capitalization on word level.
	 */
	public void setCapWords(Boolean capWords) {
		if(capWords != null) this.capWords = capWords;
		else this.capWords = Boolean.FALSE;
	}

	/**
	 * Returns whether or not to hint capitalization on sentence level.
	 * @return whether or not to hint capitalization on sentence level.
	 */
	public Boolean getCapSentences() {
		return capSentences;
	}

	/**
	 * Sets whether or not to hint capitalization on sentence level. If set to
	 * {@code null} the value reverts to its default value of {@code Boolean.FALSE}.
	 * @param capSentences whether or not to hint capitalization on character level.
	 */
	public void setCapSentences(Boolean capSentences) {
		if(capSentences != null) this.capSentences = capSentences;
		else this.capSentences = Boolean.FALSE;
	}

	/**
	 * Returns whether or not to force capitalization on character level.
	 * @return whether or not to force capitalization on character level.
	 */
	public Boolean getForceCapCharacters() {
		return forceCapCharacters;
	}

	/**
	 * Sets whether or not to force capitalization on character level. If set to
	 * {@code null} the value reverts to its default value of {@code Boolean.FALSE}.
	 * @param forceCapCharacters whether or not to force capitalization on character level.
	 */
	public void setForceCapCharacters(Boolean forceCapCharacters) {
		if(forceCapCharacters != null) this.forceCapCharacters = forceCapCharacters;
		else this.forceCapCharacters = Boolean.FALSE;
	}

	/**
	 * Returns whether or not to force capitalization on word level.
	 * @return whether or not to force capitalization on word level.
	 */
	public Boolean getForceCapWords() {
		return forceCapWords;
	}

	/**
	 * Sets whether or not to force capitalization on word level. If set to
	 * {@code null} the value reverts to its default value of {@code Boolean.FALSE}.
	 * @param forceCapWords whether or not to force capitalization on word level.
	 */
	public void setForceCapWords(Boolean forceCapWords) {
		if(forceCapWords != null) this.forceCapWords = forceCapWords;
		else this.forceCapWords = Boolean.FALSE;
	}

	/**
	 * Returns whether or not to force capitalization on sentence level.
	 * @return whether or not to force capitalization on sentence level.
	 */
	public Boolean getForceCapSentences() {
		return forceCapSentences;
	}

	/**
	 * Sets whether or not to force capitalization on sentence level. If set to
	 * {@code null} the value reverts to its default value of {@code Boolean.FALSE}.
	 * @param forceCapSentences whether or not to force capitalization on sentence level.
	 */
	public void setForceCapSentences(Boolean forceCapSentences) {
		if(forceCapSentences != null) this.forceCapSentences = forceCapSentences;
		else this.forceCapSentences = Boolean.FALSE;
	}

	@Override
	public Map<String, ?> getParameters() {
		Map<String,Object> result = new LinkedHashMap<>();
		result.put("variableName", variableName);
		if(min != null) result.put("min", min);
		if(max != null) result.put("max", max);
		result.put("allowNumbers",allowNumbers);
		result.put("allowSpecialCharacters",allowSpecialCharacters);
		result.put("allowSpaces",allowSpaces);
		result.put("capCharacters",capCharacters);
		result.put("capWords",capWords);
		result.put("capSentences",capSentences);
		result.put("forceCapCharacters",forceCapCharacters);
		result.put("forceCapWords",forceCapWords);
		result.put("forceCapSentences",forceCapSentences);
		return result;
	}

	@Override
	public void getReadVariableNames(Set<String> varNames) {
	}

	@Override
	public void getWriteVariableNames(Set<String> varNames) {
		varNames.add(variableName);
	}

	@Override
	public void executeBodyCommand(Map<String, Object> variables,
			WoolNodeBody processedBody) throws EvaluationException {
		processedBody.addSegment(new WoolNodeBody.CommandSegment(this));
	}

	@Override
	public String getStatementLog(WoolVariableStore varStore) {
		WoolVariable woolVariable = varStore.getWoolVariable(variableName);
		Value value = new Value(woolVariable.getValue());
		return value.toString();
	}

	@Override
	public String toString() {
		String result = toStringStart();
		result += " value=\"$" + variableName + "\"";
		if (min != null)
			result += " min=\"" + min + "\"";

		if (max != null)
			result += " max=\"" + max + "\"";

		if (!allowNumbers) {
			result += " allowNumbers=\"false\"";
		}

		if (!allowSpecialCharacters) {
			result += " allowSpecialCharacters=\"false\"";
		}

		if (!allowSpaces) {
			result += " allowSpaces=\"false\"";
		}

		if (capCharacters) {
			result += " capCharacters=\"true\"";
		}

		if (capWords) {
			result += " capWords=\"true\"";
		}

		if (capSentences) {
			result += " capSentences=\"true\"";
		}

		if (forceCapCharacters) {
			result += " forceCapCharacters=\"true\"";
		}

		if (forceCapWords) {
			result += " forceCapWords=\"true\"";
		}

		if (forceCapSentences) {
			result += " forceCapSentences=\"true\"";
		}

		result += ">>";
		return result;
	}

	public static void parseAttributes(WoolInputAbstractTextCommand command,
			WoolBodyToken cmdStartToken, Map<String,WoolBodyToken> attrs)
			throws LineNumberParseException {
		command.setMin(readIntAttr("min", attrs, cmdStartToken, false, null, null));
		command.setMax(readIntAttr("max", attrs, cmdStartToken, false, null, null));
		command.setAllowNumbers(readBooleanAttr("allowNumbers",attrs,cmdStartToken,false));
		command.setAllowSpecialCharacters(readBooleanAttr("allowSpecialCharacters",attrs,cmdStartToken,false));
		command.setAllowSpaces(readBooleanAttr("allowSpaces",attrs,cmdStartToken,false));
		command.setCapCharacters(readBooleanAttr("capCharacters",attrs,cmdStartToken,false));
		command.setCapWords(readBooleanAttr("capWords",attrs,cmdStartToken,false));
		command.setCapSentences(readBooleanAttr("capSentences",attrs,cmdStartToken,false));
		command.setForceCapCharacters(readBooleanAttr("forceCapCharacters",attrs,cmdStartToken,false));
		command.setForceCapWords(readBooleanAttr("forceCapWords",attrs,cmdStartToken,false));
		command.setForceCapSentences(readBooleanAttr("forceCapSentences",attrs,cmdStartToken,false));
	}
}
