package eu.woolplatform.wool.exception;

public class WoolDuplicateLanguageCodeException extends Exception {

	private String languageCode;

	/**
	 * Constructs a new exception for the specified language code.
	 *
	 * @param message the message
	 * @param languageCode the duplicate language code
	 */
	public WoolDuplicateLanguageCodeException(String message, String languageCode) {
		super(message);
		this.languageCode = languageCode;
	}

	/**
	 * Return the language code associated with this {@link WoolDuplicateLanguageCodeException}.
	 *
	 * @return the language code or {@code null}.
	 */
	public String getLanguageCode() {
		return languageCode;
	}

}
