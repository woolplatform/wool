package nl.rrd.wool.model.statement;

import nl.rrd.wool.expressions.Expression;
import nl.rrd.wool.model.reply.WoolReply;

public class WoolStatementCommandIf implements WoolStatement{

	private WoolStatementCommandBody body;
	private Expression expression;
	
	// ---------- Constructors:
	
	public WoolStatementCommandIf(Expression expression) {
		this.body = new WoolStatementCommandBody();
		this.expression = expression;
	}
	
	// ---------- Getters:
	
	public WoolStatementCommandBody getIfBody() {
		return this.body;
	}
	
	public Expression getExpression() {
		return expression;
	}

	// ---------- Setters:
	
	public void setIfBody(WoolStatementCommandBody body) {
		this.body = body;
	}
	
	public void setExpression(Expression expression) {
		this.expression = expression;
	}
	
	// ---------- Utility:
	
	public String toString() {
		String result = "";
		result += "WoolStatementCommandIf: \""+"if "+this.expression.toString()+"\":\n";
		result += "\tStatements are:\n";
		for (WoolStatement statement : body.getStatements()) {
			result += "\t\t"+statement.toString()+"\n";
		}
		result += "\tReplies are:\n";
		for (WoolReply reply : body.getReplies()) {
			result += "\t\t"+reply.toString()+"\n";
		}
		return result;
	}
	
	public String toFriendlyString() {
		String result = "";
		result += "If "+this.expression.toString()+":\n";
		result += "\tState:\n";
		for (WoolStatement statement : body.getStatements()) {
			result += "\t\t"+statement.toFriendlyString()+"\n";
		}
		result += "\tReply:\n";
		for (WoolReply reply : body.getReplies()) {
			result += "\t\t"+reply.toFriendlyString()+"\n";
		}
		return result;
	}

}

