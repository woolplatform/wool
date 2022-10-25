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

/**
 * A {@link WoolTranslationContext} describes relevant parameters defining the context in which
 * dialogue statements should be translated from source- to target languages.
 *
 * <p>This currently holds the genders, as either {@link Gender#MALE} or {@link Gender#FEMALE},
 * of the user (the person interacting with the dialogue) and the various possible "agents" involved
 * in the dialogue.</p>
 *
 * <p>This information is relevant as in some languages, speaker- or addressee gender may affect the
 * translation, and different variations may be provided in the translation script.</p>
 *
 * @author Dennis Hofs
 * @author Harm op den Akker
 */
public class WoolTranslationContext {

	public enum Gender {
		MALE,
		FEMALE
	}

	private Gender defaultAgentGender = Gender.MALE;
	private Gender userGender = Gender.MALE;
	private Map<String, Gender> agentGenders = new LinkedHashMap<>();


	// --------------------------------------------------------
	// -------------------- Constructor(s) --------------------
	// --------------------------------------------------------

	/**
	 * Creates an empty instance of a {@link WoolTranslationContext}.
	 */
	public WoolTranslationContext() { }

	/**
	 * Creates an instance of a {@link WoolTranslationContext} with a given {@code userGender} and
	 * a mapping of {@code agentGenders}.
	 * @param userGender the {@link Gender} of the user (person interacting with the dialogues).
	 * @param agentGenders a mapping of agent-names to {@link Gender}s, specifying the genders of
	 *                     the agents involved in a dialogue or dialogue set.
	 */
	public WoolTranslationContext(Gender userGender, Map<String, Gender> agentGenders) {
		this.userGender = userGender;
		this.agentGenders = agentGenders;
	}

	// -----------------------------------------------------------
	// -------------------- Getters & Setters --------------------
	// -----------------------------------------------------------

	/**
	 * Returns the default {@link Gender} that is defined for the agent, if no specific gender is
	 * given.
	 * @return the default {@link Gender} of the agent.
	 */
	public Gender getDefaultAgentGender() {
		return defaultAgentGender;
	}

	/**
	 * Sets the {@link Gender} that is assumed for agents if no specific gender is given in
	 * {@link WoolTranslationContext#getAgentGenders()}.
	 * @param defaultAgentGender the default {@link Gender} for agents.
	 */
	public void setDefaultAgentGender(Gender defaultAgentGender) {
		this.defaultAgentGender = defaultAgentGender;
	}

	/**
	 * Returns the set {@link Gender} of the user, or {@link Gender#MALE} if none is specifically
	 * set.
 	 * @return the {@link Gender} of the user.
	 */
	public Gender getUserGender() {
		return userGender;
	}

	/**
	 * Sets the {@link Gender} of the user.
	 * @param userGender the {@link Gender} of the user.
	 */
	public void setUserGender(Gender userGender) {
		this.userGender = userGender;
	}

	/**
	 * Returns a mapping of agent-names to {@link Gender}s, specifying the genders of the agents
	 * involved in a dialogue or dialogue set.
	 * @return the mapping of agent-names to {@link Gender}.
	 */
	public Map<String, Gender> getAgentGenders() {
		return agentGenders;
	}

	/**
	 * Sets the mapping of agent-names to {@link Gender}s, specifying the genders of the agents
	 * involved in a dialogue or dialogue set.
	 * @param agentGenders the mapping of agent-names to {@link Gender}.
	 */
	public void setAgentGenders(Map<String, Gender> agentGenders) {
		this.agentGenders = agentGenders;
	}
}
