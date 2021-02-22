package eu.woolplatform.wool.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import eu.woolplatform.wool.model.language.WoolLanguageMap;

@JacksonXmlRootElement(localName = "wool-project")
public class WoolProjectMetaData {

	@JacksonXmlProperty(isAttribute = true)
	private String name;

	@JacksonXmlProperty(isAttribute = true, localName = "base-path")
	private String basePath;

	private String description;

	@JacksonXmlProperty(isAttribute = true)
	private String version;

	@JacksonXmlProperty(localName = "language-map")
	private WoolLanguageMap woolLanguageMap;

	// ----- Constructors

	public WoolProjectMetaData() { }

	public WoolProjectMetaData(String name, String basePath, String description, String version, WoolLanguageMap woolLanguageMap) {
		this.name = name;
		this.basePath = basePath;
		this.description = description;
		this.version = version;
		this.woolLanguageMap = woolLanguageMap;
	}

	// ----- Getters

	/**
	 * Returns the name of this {@link WoolProject} as a String.
	 * @return the name of this {@link WoolProject} as a String.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns a String representation of the base path of this {@link WoolProject}.
	 * @return a String representation of the base path of this {@link WoolProject}.
	 */
	public String getBasePath() {
		return basePath;
	}

	/**
	 * Returns the description of this {@link WoolProject}.
	 * @return the description of this {@link WoolProject}.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Returns the version of this {@link WoolProject}.
	 * @return the version of this {@link WoolProject}.
	 */
	public String getVersion() {
		return version;
	}

	// ----- Methods

	public String toString() {
		String result = "";
		result += "Wool Project Metadata:\n";
		result += "[name:"+name+"]\n";
		result += "[basePath:"+basePath+"]\n";
		result += "[description:"+description+"]\n";
		result += "[version:"+version+"]\n";
		result += woolLanguageMap.toString();
		return result;
	}
}
