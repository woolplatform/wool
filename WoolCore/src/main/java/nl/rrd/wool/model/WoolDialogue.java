package nl.rrd.wool.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.rrd.wool.model.nodepointer.WoolNodePointer;
import nl.rrd.wool.model.nodepointer.WoolNodePointerExternal;

/**
 * Object representation of a Wool Dialogue definition. A Wool Dialogue has a name, an
 * (unordered) list of {@link WoolNode}s and a specific start node.
 * 
 * @author Harm op den Akker
 */
public class WoolDialogue {
	
	private String dialogueName;
	private WoolNode startNode;
	// map from lower-case node titles to nodes
	private Map<String,WoolNode> nodes = new LinkedHashMap<>();
	private Set<String> speakers = new HashSet<>();
	private Set<String> variablesNeeded = new HashSet<>();
	private Set<String> dialoguesReferenced = new HashSet<>();
	
	// ---------- Constructors:
	
	/**
	 * Creates an empty instance of a {@link WoolDialogue}.
	 */
	public WoolDialogue() {
	}
	
	/**
	 * Creates an instance of a {@link WoolDialogue} with a given {@code dialogueName}.
	 * @param dialogueName the name of this {@link WoolDialogue}.
	 */
	public WoolDialogue(String dialogueName) {
		this.dialogueName = dialogueName;
	}
	
	// ---------- Getters:
	
	/**
	 * Returns the name of this {@link WoolDialogue}.
	 * @return the name of this {@link WoolDialogue}.
	 */
	public String getDialogueName() {
		return this.dialogueName;
	}
	
	/**
	 * Returns the starting {@link WoolNode} for this {@link WoolDialogue}.
	 * @return the starting {@link WoolNode} for this {@link WoolDialogue}.
	 */
	public WoolNode getStartNode() {
		return this.startNode;
	}
	
	/**
	 * Returns the nodes as an unmodifiable list.
	 * 
	 * @return the nodes as an unmodifiable list
	 */
	public List<WoolNode> getNodes() {
		return Collections.unmodifiableList(new ArrayList<>(nodes.values()));
	}
	
	public void addNode(WoolNode node) {
		nodes.put(node.getTitle().toLowerCase(), node);
		speakers.add(node.getHeader().getSpeaker());
		node.getBody().getReadVariableNames(variablesNeeded);
		for (WoolNodePointer nodePointer : node.getBody().getNodePointers()) {
			if (!(nodePointer instanceof WoolNodePointerExternal))
				continue;
			WoolNodePointerExternal extPointer =
					(WoolNodePointerExternal)nodePointer;
			dialoguesReferenced.add(extPointer.getDialogueId());
		}
	}
	
	public Set<String> getSpeakers() {
		return Collections.unmodifiableSet(speakers);
	}
	
	public List<String> getSpeakersList() {
		List<String> speakersList = new ArrayList<>(speakers);
		Collections.sort(speakersList);
		return Collections.unmodifiableList(speakersList);
	}

	public Set<String> getVariablesNeeded() {
		return Collections.unmodifiableSet(variablesNeeded);
	}
	
	public Set<String> getDialoguesReferenced() {
		return Collections.unmodifiableSet(dialoguesReferenced);
	}
	
	// ---------- Setters:
	
	/**
	 * Sets the name of this {@link WoolDialogue}.
	 * @param dialogueName the name of this {@link WoolDialogue}.
	 */
	public void setDialogueName(String dialogueName) {
		this.dialogueName = dialogueName;
	}
	
	// ---------- Functions:
	
	public boolean nodeExists(String nodeId) {
		return nodes.containsKey(nodeId.toLowerCase());
	}
	
	public WoolNode getNodeById(String nodeId) {
		return nodes.get(nodeId.toLowerCase());
	}
	
	/**
	 * Returns the total number of nodes in this {@link WoolDialogue}.
	 * @return the total number of nodes in this {@link WoolDialogue}.
	 */
	public int getNodeCount() {
		return nodes.size();
	}
	
	/**
	 * Returns the total number of speakers present in this {@link WoolDialogue}.
	 * @return the total number of speakers present in this {@link WoolDialogue}.
	 */
	public int getSpeakerCount() {
		return speakers.size();
	}
	
	/**
	 * Returns the total number of different dialogues referenced from this {@link WoolDialogue}.
	 * @return the total number of different dialogues referenced from this {@link WoolDialogue}.
	 */
	public int getDialoguesReferencedCount() {
		return dialoguesReferenced.size();
	}
	
	/**
	 * Returns the total number of different variables needed in executing this {@link WoolDialogue}.
	 * @return the total number of different variables needed in executing this {@link WoolDialogue}.
	 */
	public int getVariablesNeededCount() {
		return variablesNeeded.size();
	}
	
	/**
	 * Returns a human readable multi-line summary string, representing the contents of this {@link WoolDialogue}.
	 */
	public String toString() {
		String summaryString = "";
		
		summaryString += "Dialogue Name: "+getDialogueName()+"\n";
		summaryString += "Number of Nodes: "+getNodeCount()+"\n";
		
		summaryString += "\n";
		
		summaryString += "Speakers present ("+getSpeakerCount()+"):\n";
		for(String s : getSpeakers()) {
			summaryString += "  - " + s + "\n";
		}
		
		summaryString += "Dialogues Referenced ("+getDialoguesReferencedCount()+"):\n";
		for(String s : getDialoguesReferenced()) {
			summaryString += "  - " + s + "\n";
		}
		
		summaryString += "Variables Needed ("+getVariablesNeededCount()+"):\n";
		for(String s : getVariablesNeeded()) {
			summaryString += "  - " + s + "\n";
		}
		
		return summaryString;
	}
}
