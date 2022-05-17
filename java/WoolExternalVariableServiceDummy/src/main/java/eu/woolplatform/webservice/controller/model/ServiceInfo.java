package eu.woolplatform.webservice.controller.model;

/**
 * A {@code ServiceInfo} object may be used to consolidate certain meta
 * data about this WOOL Web Service instance.
 *
 * @author Harm op den Akker
 */
public class ServiceInfo {

	private String build;
	private String protocolVersion;
	private String serviceVersion;

	public ServiceInfo(String build, String protocolVersion, String serviceVersion) {
		this.build = build;
		this.protocolVersion = protocolVersion;
		this.serviceVersion = serviceVersion;
	}

	public String getBuild() {
		return build;
	}

	public void setBuild(String build) {
		this.build = build;
	}

	public String getProtocolVersion() {
		return protocolVersion;
	}

	public void setProtocolVersion(String protocolVersion) {
		this.protocolVersion = protocolVersion;
	}

	public String getServiceVersion() {
		return serviceVersion;
	}

	public void setServiceVersion(String serviceVersion) {
		this.serviceVersion = serviceVersion;
	}
}
