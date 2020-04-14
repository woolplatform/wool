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

package eu.woolplatform.wool.parser;

import java.util.ArrayList;
import java.util.List;

import eu.woolplatform.wool.model.nodepointer.WoolNodePointer;

public class WoolNodeState {
	private String title = null;
	private String speaker = null;
	private int speakerLine = 0;
	private int speakerColumn = 0;
	private int nextReplyId = 1;
	private List<NodePointerToken> nodePointerTokens = new ArrayList<>();

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSpeaker() {
		return speaker;
	}

	public void setSpeaker(String speaker) {
		this.speaker = speaker;
	}

	public int getSpeakerLine() {
		return speakerLine;
	}

	public void setSpeakerLine(int speakerLine) {
		this.speakerLine = speakerLine;
	}

	public int getSpeakerColumn() {
		return speakerColumn;
	}

	public void setSpeakerColumn(int speakerColumn) {
		this.speakerColumn = speakerColumn;
	}

	public int createNextReplyId() {
		return nextReplyId++;
	}
	
	public List<NodePointerToken> getNodePointerTokens() {
		return nodePointerTokens;
	}
	
	public void addNodePointerToken(WoolNodePointer pointer,
			WoolBodyToken token) {
		nodePointerTokens.add(new NodePointerToken(title, pointer, token));
	}

	public static class NodePointerToken {
		private String nodeTitle;
		private WoolNodePointer pointer;
		private WoolBodyToken token;
		
		public NodePointerToken(String nodeTitle, WoolNodePointer pointer,
				WoolBodyToken token) {
			this.nodeTitle = nodeTitle;
			this.pointer = pointer;
			this.token = token;
		}
		
		public String getNodeTitle() {
			return nodeTitle;
		}

		public WoolNodePointer getPointer() {
			return pointer;
		}

		public WoolBodyToken getToken() {
			return token;
		}
	}
}
