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
