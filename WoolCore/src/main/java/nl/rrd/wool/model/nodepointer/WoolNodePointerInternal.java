package nl.rrd.wool.model.nodepointer;

/**
 * A pointer to a node that is part of the same dialogue as the node that is being referred from. 
 * 
 * @author Tessa Beinema
 * @see {@link WoolReply} for usage example
 */
public class WoolNodePointerInternal extends WoolNodePointer {
	
	public WoolNodePointerInternal (String nodeId) {
		super(nodeId);
	}
	
	// ---------- Functions:
	
	@Override
	public String toString() {
		return this.nodeId;
	}
	
	@Override
	public String toFriendlyString() {
		return this.nodeId;
	}
}
