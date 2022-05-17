package eu.woolplatform.webservice.controller.model;

public class WoolVariableParam {

	private String name;
	private String value;
	private Long lastUpdated;

	// ----- Constructors

	public WoolVariableParam(String name, String value, Long lastUpdated) {
		this.name = name;
		this.value = value;
		this.lastUpdated = lastUpdated;
	}

	// ----- Getters & Setters

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Long getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(Long lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

}
