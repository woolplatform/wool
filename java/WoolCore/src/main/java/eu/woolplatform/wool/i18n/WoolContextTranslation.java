package eu.woolplatform.wool.i18n;

import java.util.Set;

/**
 * This class models the translation of a phrase in a WOOL dialogue, along with
 * a context set. Supported contexts are: male_speaker, female_speaker,
 * male_addressee, female_addressee.
 *
 * @author Dennis Hofs (RRD)
 */
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
