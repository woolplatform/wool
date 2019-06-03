package nl.rrd.wool.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nl.rrd.wool.exception.WoolInvalidStartNodeException;

/**
 * Object representation of a Wool Dialogue definition. A Wool Dialogue has a name, an
 * (unordered) list of {@link WoolNode}s and a specific start node.
 * 
 * @author Harm op den Akker
 */
public class WoolDialogue {
	
	private String dialogueName;
	private WoolNode startNode;
	private List<WoolNode> nodes;
	private Set<String> speakers;
	private Set<String> variablesNeeded;
	private Set<String> dialoguesReferenced;
	
	// ---------- Constructors:
	
	/**
	 * Creates an empty instance of a {@link WoolDialogue}.
	 */
	public WoolDialogue() {
		this.nodes = new ArrayList<WoolNode>();
		this.speakers = new HashSet<String>();
		this.variablesNeeded = new HashSet<String>();
		this.dialoguesReferenced = new HashSet<String>();
	}
	
	/**
	 * Creates an instance of a {@link WoolDialogue} with a given {@code dialogueName}.
	 * @param dialogueName the name of this {@link WoolDialogue}.
	 */
	public WoolDialogue(String dialogueName) {
		this.dialogueName = dialogueName;
		this.nodes = new ArrayList<WoolNode>();
		this.speakers = new HashSet<String>();
		this.variablesNeeded = new HashSet<String>();
		this.dialoguesReferenced = new HashSet<String>();
	}
	
	/**
	 * Creates a fully instantiated instance of a {@link WoolDialogue} with the given {@code dialogueName},
	 * {@code nodes} ({@link List} of {@link WoolNode}s) and {@code startNode} that should be a pointer to a {@link WoolNode}
	 * contained in {@code nodes}.
	 * @param dialogueName the name of this {@link WoolDialogue}.
	 * @param nodes the list of {@link WoolNode}s that make up this {@link WoolDialogue}.
	 * @param startNode the starting {@link WoolNode} for this {@link WoolDialogue}.
	 * @throws WoolInvalidStartNodeException in case the given {@code startNode} is not contained in the given {@code nodes}.
	 */
	public WoolDialogue(String dialogueName, List<WoolNode> nodes, WoolNode startNode, Set<String> speakers, Set<String> variablesNeeded, Set<String> dialoguesReferenced) throws WoolInvalidStartNodeException {
		this.dialogueName = dialogueName;
		if(nodes.contains(startNode)) {
			this.nodes = nodes;
			this.startNode = startNode;
			this.speakers = speakers;
			this.variablesNeeded = variablesNeeded;
			this.dialoguesReferenced = dialoguesReferenced;
		} else {
			throw new WoolInvalidStartNodeException("The indicated starting node for this Wool Dialogue is not valid.");
		}
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
	 * Returns the list of {@link WoolNode}s that make up this {@link WoolDialogue}.
	 * @return the list of {@link WoolNode}s that make up this {@link WoolDialogue}.
	 */
	public List<WoolNode> getNodes() {
		return this.nodes;
	}
	
	public Set<String> getSpeakers() {
		return this.speakers;
	}
	
	public List<String> getSpeakersList() {
		List<String> speakersList = new ArrayList<String>();
		speakersList.addAll(this.speakers);
		Collections.sort(speakersList);
		return speakersList;
	}

	public Set<String> getVariablesNeeded() {
		return this.variablesNeeded;
	}
	
	public Set<String> getDialoguesReferenced() {
		return this.dialoguesReferenced;
	}
	
	// ---------- Setters:
	
	/**
	 * Sets the name of this {@link WoolDialogue}.
	 * @param dialogueName the name of this {@link WoolDialogue}.
	 */
	public void setDialogueName(String dialogueName) {
		this.dialogueName = dialogueName;
	}
	
	/**
	 * Sets the starting {@link WoolNode} for this {@link WoolDialogue}.
	 * @param startNode the starting {@link WoolNode} for this {@link WoolDialogue}.
	 */
	public void setStartNode(WoolNode startNode) {
		this.startNode = startNode;
	}
	
	/**
	 * Sets the list of {@link WoolNode}s that make up this {@link WoolDialogue}.
	 * @param nodes the list of {@link WoolNode}s that make up this {@link WoolDialogue}.
	 */
	public void setNodes(List<WoolNode> nodes) {
		this.nodes = nodes;
	}
	
	public void setSpeakers(Set<String> speakers) {
		this.speakers = speakers;
	}
	
	public void addSpeaker(String speaker) {
		this.speakers.add(speaker);
	}
	
	public void setVariablesNeeded(Set<String> variablesNeeded) {
		this.variablesNeeded = variablesNeeded;
	}
	
	public void addVariableNeeded(String variableNeeded) {
		this.variablesNeeded.add(variableNeeded);
	}
	
	public void setDialoguesReferenced(Set<String> dialoguesReferenced) {
		this.dialoguesReferenced = dialoguesReferenced;
	}
	
	public void addDialogueReference(String dialogueId) {
		this.dialoguesReferenced.add(dialogueId);
	}
	
	// ---------- Functions:
	
	public boolean nodeExists(String nodeId) {
		for(WoolNode node : nodes) {
			if(node.getTitle() != null && node.getTitle().equalsIgnoreCase(nodeId)) {
				return true;
			}
		}
		return false;
	}
	
	public WoolNode getNodeById(String nodeId) {
		for(WoolNode node : nodes) {
			if(node.getTitle() != null && node.getTitle().equalsIgnoreCase(nodeId)) {
				return node;
			}
		}
		return null;
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
