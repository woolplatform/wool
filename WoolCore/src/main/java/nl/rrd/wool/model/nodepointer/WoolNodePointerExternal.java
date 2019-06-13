package nl.rrd.wool.model.nodepointer;

/**
 * A pointer to a node that is part of a different dialogue than the dialogue of which the node that is being referred from is a part. 
 * 
 * @author Tessa Beinema
 * @see {@link WoolReply} for usage example
 */
public class WoolNodePointerExternal extends WoolNodePointer {
	
	private String dialogueId;
	
	public WoolNodePointerExternal (String dialogueId, String nodeId) {
		super(nodeId);
		this.dialogueId = dialogueId;
	}
	
	// ---------- Getters:

	/**
	 * Returns the identifier of the {@link WoolDialogue} that this pointer refers to.
	 * @return the identifier of the {@link WoolDialogue} that this pointer refers to.
	 */
	public String getDialogueId() {
		return this.dialogueId;
	}
	
	// ---------- Functions:
	
	@Override
	public String toString() {
		return this.dialogueId + "." + this.nodeId;
	}
}
