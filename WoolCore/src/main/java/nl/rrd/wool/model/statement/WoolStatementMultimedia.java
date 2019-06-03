package nl.rrd.wool.model.statement;

public abstract class WoolStatementMultimedia implements WoolStatement {
	
	public enum MultimediaType {
		IMAGE, VIDEO, TIMER;
	}
	
	protected MultimediaType multimediaType;
	
	// ---------- Constructors:

	public WoolStatementMultimedia(MultimediaType multimediaType) {
		this.multimediaType = multimediaType;
	}
	
	// ---------- Getters:
	
	public MultimediaType getMultimediaType() {
		return this.multimediaType;
	}
	
	
	// ---------- Setters:

	public void setMultimediaType(MultimediaType multimediaType) {
		this.multimediaType = multimediaType;
	}
	
	// ---------- Utility:

	@Override
	public abstract String toString();
	
	@Override
	public abstract String toFriendlyString();
}
