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

package eu.woolplatform.wool.model.nodepointer;

import eu.woolplatform.utils.exception.ParseException;
import eu.woolplatform.wool.model.WoolDialogue;
import eu.woolplatform.wool.model.WoolReply;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A pointer to a node that is part of a different dialogue than the dialogue of which the node that is being referred from is a part. 
 * 
 * @author Tessa Beinema
 * @see WoolReply
 */
public class WoolNodePointerExternal extends WoolNodePointer {
	
	private String dialogueId;
	
	public WoolNodePointerExternal(String containerDialogueId,
			String relNextDialogueId, String nodeId) throws ParseException {
		super(nodeId);
		this.dialogueId = getAbsoluteDialogueId(containerDialogueId,
				relNextDialogueId);
	}

	public WoolNodePointerExternal(WoolNodePointerExternal other) {
		super(other);
		this.dialogueId = other.dialogueId;
	}
	
	// ---------- Getters:

	/**
	 * Returns the identifier of the {@link WoolDialogue} that this pointer refers to.
	 * @return the identifier of the {@link WoolDialogue} that this pointer refers to.
	 */
	public String getDialogueId() {
		return this.dialogueId;
	}

	private static String getAbsoluteDialogueId(String containerDialogueId,
			String relNextDialogueId) throws ParseException {
		if (relNextDialogueId.startsWith("/"))
			return relNextDialogueId.substring(1);
		List<String> relPath = Arrays.asList(relNextDialogueId.split("/"));
		String relPathStr = String.join("/", relPath);
		List<String> absPath = new ArrayList<>(Arrays.asList(
				containerDialogueId.split("/")));
		absPath.remove(absPath.size() - 1);
		String containerPathStr = String.join("/", absPath);
		for (String relPathElem : relPath) {
			if (relPathElem.equals("..")) {
				if (absPath.isEmpty()) {
					throw new ParseException(String.format(
							"Relative path \"%s\" goes above root from \"%s\"",
							relPathStr, containerPathStr));
				}
				absPath.remove(absPath.size() - 1);
			} else {
				absPath.add(relPathElem);
			}
		}
		return String.join("/", absPath);
	}
	
	// ---------- Functions:
	
	@Override
	public String toString() {
		return this.dialogueId + "." + this.nodeId;
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + dialogueId.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (!super.equals(obj))
			return false;
		WoolNodePointerExternal other = (WoolNodePointerExternal)obj;
		if (!dialogueId.equals(other.dialogueId))
			return false;
		return true;
	}

	@Override
	public WoolNodePointerExternal clone() {
		return new WoolNodePointerExternal(this);
	}
}
