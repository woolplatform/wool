package eu.woolplatform.wool.i18n;

public class WoolSourceTranslatable {
	public static final String USER = "_user";

	private String speaker;
	private String addressee;
	private WoolTranslatable translatable;

	public WoolSourceTranslatable(String speaker, String addressee,
			WoolTranslatable translatable) {
		this.speaker = speaker;
		this.addressee = addressee;
		this.translatable = translatable;
	}

	public String getSpeaker() {
		return speaker;
	}

	public String getAddressee() {
		return addressee;
	}

	public WoolTranslatable getTranslatable() {
		return translatable;
	}
}
