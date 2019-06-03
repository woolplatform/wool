package nl.rrd.wool.model.statement;

public class WoolStatementMultimediaTimer extends WoolStatementMultimedia {
	
	private int timerDuration;
	
	// ---------- Constructors:

	public WoolStatementMultimediaTimer(MultimediaType multimediaType, int timerDuration) {
		super(multimediaType);
		this.timerDuration = timerDuration;
	}
	
	// ---------- Getters:
	
	public int getTimerDuration() {
		return this.timerDuration;
	}
	
	// ---------- Setters:
	
	public void setTimerDuration(int timerDuration) {
		this.timerDuration = timerDuration;
	}
	
	// ---------- Utility:

	@Override
	public String toString() {
		String result = "";
		result += "WoolStatementMultimedia: \"Type is "+this.multimediaType+" and resource name is "+this.timerDuration+"\".";
		return result;
	}
	
	@Override
	public String toFriendlyString() {
		String result = "";
		result += "Multimedia "+this.multimediaType+" duration "+this.timerDuration+".";
		return result;
	}
}
