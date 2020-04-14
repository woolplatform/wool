/*
 * Copyright 2019-2020 Roessingh Research and Development.
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

package eu.woolplatform.wool.model;

/**
 * A {@link WoolNode} represents a single step in a {@link WoolDialogue} definition.
 *
 * @author Harm op den Akker (Roessingh Research and Development)
 */
public class WoolNode {
	
	private WoolNodeHeader header;
	private WoolNodeBody body;
	
	// ---------- Constructors:

	/**
	 * Creates an instance of an empty {@link WoolNode}.
	 */
	public WoolNode() { }

	/**
	 * Creates an instance of a {@link WoolNode} with the given {@code header}.
	 *
	 * @param header the {@link WoolNodeHeader} for this {@link WoolNode}
	 */
	public WoolNode(WoolNodeHeader header) {
		this.header = header;
	}

	/**
	 * Creates an instance of a {@link WoolNode} with the given {@code header} and {@code body}.
	 *
	 * @param header the {@link WoolNodeHeader} for this {@link WoolNode}
	 * @param body the {@link WoolNodeBody} for this {@link WoolNode}
	 */
	public WoolNode(WoolNodeHeader header, WoolNodeBody body) {
		this.header = header;
		this.body = body;
	}

	/**
	 * Creates an instance of a {@link WoolNode} instantiated with the contents from the given {@code other}
	 * {@link WoolNode}.
	 *
	 * @param other the {@link WoolNode} from which to copy its contents into this {@link WoolNode}
	 */
	public WoolNode(WoolNode other) {
		header = new WoolNodeHeader(other.header);
		body = new WoolNodeBody(other.body);
	}
	
	// ---------- Getters:

	/**
	 * Returns the {@link WoolNodeHeader} of this {@link WoolNode}.
	 *
	 * @return the {@link WoolNodeHeader} of this {@link WoolNode}.
	 */
	public WoolNodeHeader getHeader() {
		return header;
	}

	/**
	 * Returns the {@link WoolNodeBody} of this {@link WoolNode}.
	 *
	 * @return the {@link WoolNodeBody} of this {@link WoolNode}.
	 */
	public WoolNodeBody getBody() {
		return body;
	}

	// ---------- Setters;

	/**
	 * Sets the {@link WoolNodeHeader} for this {@link WoolNode}.
	 *
	 * @param header the {@link WoolNodeHeader} for this {@link WoolNode}.
	 */
	public void setHeader(WoolNodeHeader header) {
		this.header = header;
	}

	/**
	 * Sets the {@link WoolNodeBody} for this {@link WoolNode}.
	 *
	 * @param body the {@link WoolNodeBody} for this {@link WoolNode}.
	 */
	public void setBody(WoolNodeBody body) {
		this.body = body;
	}
	
	// ---------- Utility:
	
	/**
	 * Returns the title of this {@link WoolNode} as defined in its
	 * corresponding {@link WoolNodeHeader}. Returns the same as {@code
	 * this.getHeader().getTitle()} or {@code null} if no {@link WoolNodeHeader}
	 * has been set, or its title attribute is {@code null}.
	 *
	 * @return the title of this {@link WoolNode} as defined in its
	 * corresponding {@link WoolNodeHeader}.
	 */
	public String getTitle() {
		if(header != null)
			return header.getTitle();
		else return null;
	}
	
	@Override
	public String toString() {
		String newline = System.getProperty("line.separator");
		return header + newline + "---" + newline + body;
	}
}
