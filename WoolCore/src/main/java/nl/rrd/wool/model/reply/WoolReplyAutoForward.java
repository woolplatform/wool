package nl.rrd.wool.model.reply;

import nl.rrd.wool.model.nodepointer.WoolNodePointer;

public class WoolReplyAutoForward extends WoolReply {

	public WoolReplyAutoForward(int replyId, WoolNodePointer nextNodePointer) {
		super(replyId, nextNodePointer);
	}
	
	// ---------- Getters:

	/**
	 * Returns if this reply goes to a 'end dialogue' node.
	 * @return if this reply goes to a 'end dialogue' node.
	 */
	public boolean getEndsDialogue() {
		if (this.getNodePointer().getNodeId().equalsIgnoreCase("end")) {
			return true;
		}
		else return false;
	}
	
	// ---------- Functions:

	@Override
	public String toString() {
		String result = "WoolReplyAutoForward ("+this.getReplyId()+") [[\"...\" -> "+this.getNodePointer().toFriendlyString()+"]]";
		return result;
	}

	@Override
	public String toFriendlyString() {
		String result = "[[\"...\"]]";
		return result;
	}

}
