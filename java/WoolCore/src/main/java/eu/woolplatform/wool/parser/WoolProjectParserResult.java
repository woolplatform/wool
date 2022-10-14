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

package eu.woolplatform.wool.parser;

import eu.woolplatform.utils.exception.ParseException;
import eu.woolplatform.wool.model.WoolProject;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This class contains the result of parsing a WOOL project with the {@link
 * WoolProjectParser WoolProjectParser}.
 *
 * @author Dennis Hofs (RRD)
 */
public class WoolProjectParserResult {
	private WoolProject project = null;
	private Map<String,List<ParseException>> parseErrors =
			new LinkedHashMap<>();
	private Map<String,List<String>> warnings = new LinkedHashMap<>();

	/**
	 * Returns the project if parsing succeeded. Otherwise it returns null.
	 *
	 * @return the project or null
	 */
	public WoolProject getProject() {
		return project;
	}

	/**
	 * Sets the project if parsing succeeded.
	 *
	 * @param project the project
	 */
	public void setProject(WoolProject project) {
		this.project = project;
	}

	/**
	 * Returns the parse errors. The keys are the paths to files with parse
	 * errors. A value should be a list with 1 or more errors.
	 *
	 * @return the parse errors
	 */
	public Map<String,List<ParseException>> getParseErrors() {
		return parseErrors;
	}

	/**
	 * Sets the parse errors. The keys are the paths to files with parse errors.
	 * A value should be a list with 1 or more errors.
	 *
	 * @param parseErrors the parse errors
	 */
	public void setParseErrors(Map<String,List<ParseException>> parseErrors) {
		this.parseErrors = parseErrors;
	}

	/**
	 * Returns the warnings. They keys are the paths to files with warnings. A
	 * value should be a list with 1 or more warnings.
	 *
	 * @return the warnings
	 */
	public Map<String, List<String>> getWarnings() {
		return warnings;
	}

	/**
	 * Sets the warnings. They keys are the paths to files with warnings. A
	 * value should be a list with 1 or more warnings.
	 *
	 * @param warnings the warnings
	 */
	public void setWarnings(Map<String, List<String>> warnings) {
		this.warnings = warnings;
	}
}
