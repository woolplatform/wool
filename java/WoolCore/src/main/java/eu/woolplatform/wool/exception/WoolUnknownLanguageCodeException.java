package eu.woolplatform.wool.exception;

public class WoolUnknownLanguageCodeException extends Exception {

	private String languageCode;

	/**
	 * Constructs a new exception for the specified language code.
	 *
	 * @param message the message
	 * @param languageCode the unknown language code
	 */
	public WoolUnknownLanguageCodeException(String message, String languageCode) {
		super(message);
		this.languageCode = languageCode;
	}

	/**
	 * Return the language code associated with this {@link WoolUnknownLanguageCodeException}.
	 *
	 * @return the language code or {@code null}.
	 */
	public String getLanguageCode() {
		return languageCode;
	}
}
