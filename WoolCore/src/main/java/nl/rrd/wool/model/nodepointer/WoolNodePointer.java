package nl.rrd.wool.model.nodepointer;

import nl.rrd.wool.model.WoolNode;

/**
 * An abstract representation of a pointer to a next node. 
 * 
 * @author Tessa Beinema
 * @see {@link WoolNodePointerInternal}, {@link WoolNodePointerExternal}
 */
public abstract class WoolNodePointer {
	
	protected String nodeId;

	// ---------- Constructors:
	
	/**
	 * Creates an instance of a {@link WoolNodePointer} with given {@code nodeId}.
	 * @param nextNodeId the unique identifier of the {@link WoolNode} that this NodePointer
	 * refers to.
	 */
	public WoolNodePointer(String nodeId) {
		this.nodeId = nodeId;
	}
	
	// ---------- Getters:

	/**
	 * Returns the identifier of the {@link WoolNode} that this pointer refers to.
	 * @return the identifier of the {@link WoolNode} that this pointer refers to.
	 */
	public String getNodeId() {
		return this.nodeId;
	}
	
	// ---------- Setters:
	
	/**
	 * Sets the identifier of the {@link WoolNode} that this pointer refers to. 
	 * @param nodeId the identifier of the {@link WoolNode} that this pointer refers to.
	 */
	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}
}
