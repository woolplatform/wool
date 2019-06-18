package nl.rrd.wool.model;

public class WoolNode {
	
	private WoolNodeHeader header;
	private WoolNodeBody body;
	
	// ---------- Constructors:
	
	public WoolNode() { }
	
	public WoolNode(WoolNodeHeader header) {
		this.header = header;
	}
	
	public WoolNode(WoolNodeHeader header, WoolNodeBody body) {
		this.header = header;
		this.body = body;
	}
	
	// ---------- Getters:
	
	public WoolNodeHeader getHeader() {
		return header;
	}
	
	public WoolNodeBody getBody() {
		return body;
	}

	// ---------- Setters;
	
	public void setHeader(WoolNodeHeader header) {
		this.header = header;
	}
	
	public void setBody(WoolNodeBody body) {
		this.body = body;
	}
	
	// ---------- Utility:
	
	/**
	 * Returns the title of this {@link WoolNode} as defined in its corresponding {@link WoolHeader}. Returns the same
	 * as {@code this.getHeader().getTitle()} or {@code null} if no {@link WoolHeader} has been set, or its title attribute is {@code null}.
	 * @return the title of this {@link WoolNode} as defined in its corresponding {@link WoolHeader}.
	 */
	public String getTitle() {
		if(header != null)
			return header.getTitle();
		else return null;
	}
	
	@Override
	public String toString() {
		String newline = System.getProperty("line.separator");
		return header + newline + "---" + newline + body;
	}
}
