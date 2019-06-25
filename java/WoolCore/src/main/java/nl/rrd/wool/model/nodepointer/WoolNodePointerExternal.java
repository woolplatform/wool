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

package nl.rrd.wool.model.nodepointer;

import nl.rrd.wool.model.WoolDialogue;
import nl.rrd.wool.model.WoolReply;

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
}
