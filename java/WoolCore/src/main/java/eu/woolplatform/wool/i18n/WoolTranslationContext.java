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

import java.util.LinkedHashMap;
import java.util.Map;

public class WoolTranslationContext {
	public enum Gender {
		MALE,
		FEMALE
	}

	private Gender defaultAgentGender = Gender.MALE;
	private Map<String, Gender> agentGenders = new LinkedHashMap<>();
	private Gender userGender = Gender.MALE;

	public Gender getDefaultAgentGender() {
		return defaultAgentGender;
	}

	public void setDefaultAgentGender(Gender defaultAgentGender) {
		this.defaultAgentGender = defaultAgentGender;
	}

	public Map<String, Gender> getAgentGenders() {
		return agentGenders;
	}

	public void setAgentGenders(Map<String, Gender> agentGenders) {
		this.agentGenders = agentGenders;
	}

	public Gender getUserGender() {
		return userGender;
	}

	public void setUserGender(Gender userGender) {
		this.userGender = userGender;
	}
}
