package nl.rrd.wool.model.reply;

import java.util.HashMap;
import java.util.Map;

import nl.rrd.wool.model.WoolNode;
import nl.rrd.wool.model.WoolNodeBody;
import nl.rrd.wool.model.nodepointer.WoolNodePointer;

/**
 * An abstract representation of a reply option in the Wool specification. Each reply option
 * automatically gets an identifier ({@code int} value) assigned to it based on its order in the 
 * Wool dialogue specification.
 * 
 * @author Harm op den Akker
 * @see {@link WoolReplyBasic}, {@link WoolReplyAutoForward}
 */
public abstract class WoolReply {
	
	private int replyId;
	private WoolNodePointer nodePointer;
	public Map<String, String> variablesToSet; 
	
	// ---------- Constructors:
	
	/**
	 * Creates an instance of a {@link WoolReply} with given {@code replyId} and {@code nodePointer}.
	 * @param replyId the automatically assigned identifier (unique for {@link WoolReply}s within a 
	 * {@link WoolNodeBody}. 
	 * @param nodePointer a pointer to the {@link WoolNode} that should follow when this
	 * {@link WoolReply} is selected by the user. This can be an internal or external pointer (i.e. 
	 * to a different dialogue).
	 */
	public WoolReply(int replyId, WoolNodePointer nodePointer) {
		this.replyId = replyId;
		this.nodePointer = nodePointer;
		this.variablesToSet = new HashMap<String, String>();

	}
	
	// ---------- Getters:
	
	/**
	 * Returns the unique identifier for this {@link WoolReply}.
	 * @return the unique identifier for this {@link WoolReply}.
	 */
	public int getReplyId() {
		return replyId;
	}
	
	/**
	 * Returns the pointer to the {@link WoolNode} that should follow after this {@link WoolReply}.
	 * @return the pointer to the {@link WoolNode} that should follow after this {@link WoolReply}.
	 */
	public WoolNodePointer getNodePointer() {
		return this.nodePointer;
	}
	
	public Map<String, String> getVariablesToSet(){
		return this.variablesToSet;
	}
	
	// ---------- Setters:
	
	/**
	 * Sets the unique identifier for this {@link WoolReply}.
	 * @param replyId the unique identifier for this {@link WoolReply}.
	 */
	public void setReplyId(int replyId) {
		this.replyId = replyId;
	}
	
	/**
	 * Sets the pointer to the {@link WoolNode} that should follow after this {@link WoolReply}. 
	 * @param nodePointer the pointer to the the {@link WoolNode} that should follow after this {@link WoolReply}.
	 */
	public void setNodePointer(WoolNodePointer nodePointer) {
		this.nodePointer = nodePointer;
	}
	
	public void setVariablesToSet(Map<String, String> variables) {
		this.variablesToSet = variables;
	}
	

	// ---------- Functions:
	
	public abstract String toString();
	
	public abstract String toFriendlyString();
	
}
