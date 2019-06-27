/*
 * Copyright 2019 Roessingh Research and Development.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the "Software"), 
 * to deal in the Software without restriction, including without limitation 
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, 
 * and/or sell copies of the Software, and to permit persons to whom the 
 * Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER 
 * DEALINGS IN THE SOFTWARE.
 */

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
	// map from lower-case node titles to nodes
	private Map<String,WoolNode> nodes = new LinkedHashMap<>();
	private Set<String> speakers = new HashSet<>();
	private Set<String> variablesNeeded = new HashSet<>();
	private Set<String> variablesWritten = new HashSet<>();
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
		return nodes.get("start");
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
		if (node.getHeader().getSpeaker() != null)
			speakers.add(node.getHeader().getSpeaker());
		node.getBody().getReadVariableNames(variablesNeeded);
		node.getBody().getWriteVariableNames(variablesWritten);
		Set<WoolNodePointer> nodePointers = new HashSet<>();
		node.getBody().getNodePointers(nodePointers);
		for (WoolNodePointer nodePointer : nodePointers) {
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
	
	public Set<String> getVariablesWritten() {
		return Collections.unmodifiableSet(variablesWritten);
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
	 * Returns the total number of different variables written in executing this {@link WoolDialogue}.
	 * @return the total number of different variables written in executing this {@link WoolDialogue}.
	 */
	public int getVariablesWrittenCount() {
		return variablesWritten.size();
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
		
		summaryString += "Dialogues referenced ("+getDialoguesReferencedCount()+"):\n";
		List<String> names = new ArrayList<>(getDialoguesReferenced());
		Collections.sort(names);
		for(String s : names) {
			summaryString += "  - " + s + "\n";
		}
		
		summaryString += "Variables needed ("+getVariablesNeededCount()+"):\n";
		names = new ArrayList<>(getVariablesNeeded());
		Collections.sort(names);
		for(String s : names) {
			summaryString += "  - " + s + "\n";
		}
		
		summaryString += "Variables written ("+getVariablesWrittenCount()+"):\n";
		names = new ArrayList<>(getVariablesWritten());
		Collections.sort(names);
		for(String s : names) {
			summaryString += "  - " + s + "\n";
		}
		
		return summaryString;
	}
}
