package eu.woolplatform.wool.model.language;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

/**
 * A {@link WoolLanguage} defines a language used in a wool project with a
 * given name and language code. The 'code' is preferably a specific ISO3 code, an ISO1 code,
 * or for languages that don't actually exist (e.g. Klingon, Orcish) a made up code
 * that is not assigned to any existing language. These codes must be unique within
 * a given wool project.
 *
 * @author Harm op den Akker (Innovation Sprint)
 */
public class WoolLanguage {

	@JacksonXmlProperty(isAttribute = true)
	private String name;

	@JacksonXmlProperty(isAttribute = true)
	private String code;

	// ----- Constructors

	/**
	 * Creates an empty instance of a {@link WoolLanguage}.
	 */
	public WoolLanguage() { }

	/**
	 * Creates an instance of a {@link WoolLanguage} with given {@code name} and {@code code}.
	 * @param name the name of the language
	 * @param code the code (ISO3, ISO1, or made-up) for this {@link WoolLanguage}.
	 */
	public WoolLanguage(String name, String code) {
		this.name = name;
		this.code = code;
	}

	// ----- Getters

	/**
	 * Returns the name of this {@link WoolLanguage}.
	 * @return the name of this {@link WoolLanguage}.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the code of this {@link WoolLanguage}.
	 * @return the code of this {@link WoolLanguage}.
	 */
	public String getCode() {
		return code;
	}

	// ----- Setters

	/**
	 * Sets the name of this {@link WoolLanguage}.
	 * @param name the name of this {@link WoolLanguage}.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Sets the code of this {@link WoolLanguage}.
	 * @param code the code of this {@link WoolLanguage}.
	 */
	public void setCode(String code) {
		this.code = code;
	}

	// ----- Methods

	public String toString() {
		return "[name:"+name+"] [code:"+code+"]";
	}
}
