package eu.woolplatform.wool.i18n;

import java.util.Set;

public class WoolContextTranslation {
	private Set<String> context;
	private WoolTranslatable translation;

	public WoolContextTranslation(Set<String> context,
			WoolTranslatable translation) {
		this.context = context;
		this.translation = translation;
	}

	public Set<String> getContext() {
		return context;
	}

	public WoolTranslatable getTranslation() {
		return translation;
	}
}
