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

package eu.woolplatform.wool.i18n;

import eu.woolplatform.utils.exception.ParseException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WoolTranslationParserResult {
	private Map<WoolTranslatable,List<WoolContextTranslation>> translations = null;
	private List<ParseException> parseErrors = new ArrayList<>();
	private List<String> warnings = new ArrayList<>();

	public Map<WoolTranslatable,List<WoolContextTranslation>> getTranslations() {
		return translations;
	}

	public void setTranslations(
			Map<WoolTranslatable,List<WoolContextTranslation>> translations) {
		this.translations = translations;
	}

	public List<ParseException> getParseErrors() {
		return parseErrors;
	}

	public void setParseErrors(List<ParseException> parseErrors) {
		this.parseErrors = parseErrors;
	}

	public List<String> getWarnings() {
		return warnings;
	}

	public void setWarnings(List<String> warnings) {
		this.warnings = warnings;
	}
}
