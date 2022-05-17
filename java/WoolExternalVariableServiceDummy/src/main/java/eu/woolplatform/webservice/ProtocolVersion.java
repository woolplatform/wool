package eu.woolplatform.webservice;

/**
 * All endpoints will be available at {base_path}/{protocol_version} whereby
 * the {protocol_version} is defined by the last available item in this {@code enum}.
 */
public enum ProtocolVersion {
	V1("0.1.0");
	
	private final String versionName;
	
	ProtocolVersion(String versionName) {
		this.versionName = versionName;
	}
	
	public String versionName() {
		return versionName;
	}
	
	public static ProtocolVersion forVersionName(String versionName)
			throws IllegalArgumentException {
		for (ProtocolVersion value : ProtocolVersion.values()) {
			if (value.versionName.equals(versionName))
				return value;
		}
		throw new IllegalArgumentException("Version not found: " +
				versionName);
	}
}
