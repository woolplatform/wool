package eu.woolplatform.wool.i18n;

public class WoolTranslationTerm {

	public String term;
	public String context;

	public WoolTranslationTerm () {}

	public WoolTranslationTerm (String term, String context) {
		this.term = term;
		this.context = context;
	}

	public String getTerm () {
		return this.term;
	}

	public void setTerm (String term) {
		this.term = term;
	}

	public String getContext () {
		return this.context;
	}

	public void setContext () {
		this.context = context;
	}

	@Override
	public String toString() {
		return "Term: '" + this.term + "' context: '" + this.context + "'.";
	}
}
