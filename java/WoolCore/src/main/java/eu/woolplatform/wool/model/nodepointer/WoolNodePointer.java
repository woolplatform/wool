/*
 * Copyright 2019-2022 WOOL Foundation - Licensed under the MIT License:
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package eu.woolplatform.wool.model.nodepointer;

import eu.woolplatform.wool.model.WoolNode;

/**
 * An abstract representation of a pointer to a next node. 
 * 
 * @author Tessa Beinema
 * @see WoolNodePointerInternal
 * @see WoolNodePointerExternal
 */
public abstract class WoolNodePointer implements Cloneable {
	
	protected String nodeId;

	// ---------- Constructors:
	
	/**
	 * Creates an instance of a {@link WoolNodePointer} with given {@code nodeId}.
	 * @param nodeId the unique identifier of the {@link WoolNode} that this NodePointer
	 * refers to.
	 */
	public WoolNodePointer(String nodeId) {
		this.nodeId = nodeId;
	}

	public WoolNodePointer(WoolNodePointer other) {
		this.nodeId = other.nodeId;
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

	@Override
	public int hashCode() {
		return nodeId.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		WoolNodePointer other = (WoolNodePointer)obj;
		if (!nodeId.equals(other.nodeId))
			return false;
		return true;
	}

	@Override
	public abstract WoolNodePointer clone();
}
