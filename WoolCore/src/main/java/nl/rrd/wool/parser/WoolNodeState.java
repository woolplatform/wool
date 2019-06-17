package nl.rrd.wool.parser;

import java.util.ArrayList;
import java.util.List;

import nl.rrd.wool.model.nodepointer.WoolNodePointer;

public class WoolNodeState {
	private int nextReplyId = 1;
	private List<NodePointerToken> nodePointerTokens = new ArrayList<>();
	
	public int createNextReplyId() {
		return nextReplyId++;
	}
	
	public List<NodePointerToken> getNodePointerTokens() {
		return nodePointerTokens;
	}
	
	public void addNodePointerToken(WoolNodePointer pointer,
			WoolBodyToken token) {
		nodePointerTokens.add(new NodePointerToken(pointer, token));
	}

	public static class NodePointerToken {
		private WoolNodePointer pointer;
		private WoolBodyToken token;
		
		public NodePointerToken(WoolNodePointer pointer, WoolBodyToken token) {
			this.pointer = pointer;
			this.token = token;
		}

		public WoolNodePointer getPointer() {
			return pointer;
		}

		public WoolBodyToken getToken() {
			return token;
		}
	}
}
