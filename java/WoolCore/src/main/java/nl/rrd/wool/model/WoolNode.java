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

public class WoolNode {
	
	private WoolNodeHeader header;
	private WoolNodeBody body;
	
	// ---------- Constructors:
	
	public WoolNode() { }
	
	public WoolNode(WoolNodeHeader header) {
		this.header = header;
	}
	
	public WoolNode(WoolNodeHeader header, WoolNodeBody body) {
		this.header = header;
		this.body = body;
	}

	public WoolNode(WoolNode other) {
		header = new WoolNodeHeader(other.header);
		body = new WoolNodeBody(other.body);
	}
	
	// ---------- Getters:
	
	public WoolNodeHeader getHeader() {
		return header;
	}
	
	public WoolNodeBody getBody() {
		return body;
	}

	// ---------- Setters;
	
	public void setHeader(WoolNodeHeader header) {
		this.header = header;
	}
	
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
