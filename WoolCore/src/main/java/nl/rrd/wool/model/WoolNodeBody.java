package nl.rrd.wool.model;

import java.util.ArrayList;

import nl.rrd.wool.model.reply.WoolReply;
import nl.rrd.wool.model.statement.WoolStatement;

public class WoolNodeBody {
	
	private ArrayList<WoolStatement> statements;
	private ArrayList<WoolReply> replies;
	
	// ---------- Constructors:
	
	public WoolNodeBody() { 
		statements = new ArrayList<WoolStatement>();
		replies = new ArrayList<WoolReply>();
	}
	
	public WoolNodeBody(ArrayList<WoolStatement> statements, ArrayList<WoolReply> replies) {
		this.statements = statements;
		this.replies = replies;
	}
	
	// ---------- Getters:
	
	public ArrayList<WoolStatement> getStatements() {
		return statements;
	}
	
	public ArrayList<WoolReply> getReplies() {
		return replies;
	}
	
	public WoolReply getReplyById(int replyId) {
		for(WoolReply reply : replies) {
			if(reply.getReplyId() == replyId) return reply;
		}
		return null;
	}
	
	// ---------- Setters:
	
	public void setStatements(ArrayList<WoolStatement> statements) {
		this.statements = statements;
	}
	
	public void setReplies(ArrayList<WoolReply> replies) {
		this.replies = replies;
	}
	
	// ---------- Utility:
	
	public void addStatement(WoolStatement statement) {
		statements.add(statement);
	}
	
	public void addReply(WoolReply reply) {
		replies.add(reply);
	}
	
	public String toString() {
		String result = "";
		for(WoolStatement statement : statements) {
			result += statement.toString()+"\n";
		}
		
		for(WoolReply reply : replies) {
			result += reply.toString()+"\n";
		}
		
		return result;		
	}

}
