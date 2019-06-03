package nl.rrd.wool.model.statement;

public class WoolStatementMultimediaImage extends WoolStatementMultimedia {
	
	private String resourceName;
	
	// ---------- Constructors:

	public WoolStatementMultimediaImage(MultimediaType multimediaType, String resourceName) {
		super(multimediaType);
		this.resourceName = resourceName;
	}
	
	// ---------- Getters:
	
	public String getResourceName() {
		return this.resourceName;
	}
	
	// ---------- Setters:

	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}
	
	// ---------- Utility:

	@Override
	public String toString() {
		String result = "";
		result += "WoolStatementMultimedia: \"Type is "+this.multimediaType+" and resource name is "+this.resourceName+"\".";
		return result;
	}
	
	@Override
	public String toFriendlyString() {
		String result = "";
		result += "Multimedia "+this.multimediaType+" named "+this.resourceName+".";
		return result;
	}
}
