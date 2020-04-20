package eu.woolplatform.webservice;

public enum ProtocolVersion {
	V1("1");
	
	private String versionName;
	
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
