package nl.rrd.wool.model.command;

import java.util.ArrayList;
import java.util.List;

import nl.rrd.wool.exception.LineNumberParseException;
import nl.rrd.wool.expressions.Expression;
import nl.rrd.wool.model.WoolNodeBody;
import nl.rrd.wool.parser.WoolBodyToken;
import nl.rrd.wool.utils.CurrentIterator;

/**
 * This class models the &lt;&lt;if ...&gt;&gt; command in Wool. It can be part
 * of a {@link WoolNodeBody WoolNodeBody} (not inside a reply).
 * 
 * @author Dennis Hofs (RRD)
 */
public class WoolIfCommand extends WoolCommand {
	private List<Clause> ifClauses = new ArrayList<>();
	private WoolNodeBody elseClause = null;

	/**
	 * Returns the if clauses. They should be processed from first to last.
	 * There should be at least one clause. That is the "if" clause. Any
	 * subsequent clauses are "elseif" clauses.
	 * 
	 * @return the if clauses
	 */
	public List<Clause> getIfClauses() {
		return ifClauses;
	}

	/**
	 * Sets the if clauses. They should be processed from first to last. There
	 * should be at least one clause. That is the "if" clause. Any subsequent
	 * clauses are "elseif" clauses.
	 * 
	 * @param ifClauses the if clauses
	 */
	public void setIfClauses(List<Clause> ifClauses) {
		this.ifClauses = ifClauses;
	}
	
	/**
	 * Adds an if clause. The clauses should be processed from first to last.
	 * There should be at least one clause.That is the "if" clause. Any
	 * subsequent clauses are "elseif" clauses.
	 * 
	 * @param ifClause the if clause
	 */
	public void addIfClause(Clause ifClause) {
		ifClauses.add(ifClause);
	}

	/**
	 * Returns the else clause. If there is no else clause, then this method
	 * returns null (default).
	 * 
	 * @return the else clause or null
	 */
	public WoolNodeBody getElseClause() {
		return elseClause;
	}

	/**
	 * Sets the else clause. If there is no else clause, this can be set to
	 * null (default).
	 * 
	 * @param elseClause the else clause or null
	 */
	public void setElseClause(WoolNodeBody elseClause) {
		this.elseClause = elseClause;
	}
	
	@Override
	public String toString() {
		String newline = System.getProperty("line.separator");
		Clause clause = ifClauses.get(0);
		StringBuilder result = new StringBuilder(
				"<<if " + clause.expression + ">>" + newline);
		result.append(clause.statement + newline);
		for (int i = 1; i < ifClauses.size(); i++) {
			clause = ifClauses.get(i);
			result.append("<<elseif " + clause.expression + ">>" + newline);
			result.append(clause.statement + newline);
		}
		if (elseClause != null) {
			result.append("<<else>>" + newline);
			result.append(elseClause + newline);
		}
		result.append("<<endif>>");
		return result.toString();
	}

	public static WoolActionCommand parse(WoolBodyToken cmdStartToken,
			CurrentIterator<WoolBodyToken> tokens)
			throws LineNumberParseException {
		// TODO
		return null;
	}

	/**
	 * This class models a clause of an if statement. That is the "if" clause
	 * or an "elseif" clause.
	 */
	public static class Clause {
		private Expression expression;
		private WoolNodeBody statement;

		/**
		 * Constructs a new if clause.
		 * 
		 * @param expression the if expression that should be evaluated as a
		 * boolean
		 * @param statement the statement that should be output if the
		 * expression evaluates to true
		 */
		public Clause(Expression expression, WoolNodeBody statement) {
			this.expression = expression;
			this.statement = statement;
		}

		/**
		 * Returns the if expression that should be evaluated as a boolean.
		 * 
		 * @return the if expression
		 */
		public Expression getExpression() {
			return expression;
		}

		/**
		 * Sets the if expression that should be evaluated as a boolean.
		 * 
		 * @param expression the if expression
		 */
		public void setExpression(Expression expression) {
			this.expression = expression;
		}

		/**
		 * Returns the statement that should be output if the expression
		 * evaluates to true.
		 * 
		 * @return the statement
		 */
		public WoolNodeBody getStatement() {
			return statement;
		}

		/**
		 * Sets the statement that should be output if the expression evaluates
		 * to true.
		 * 
		 * @param statement the statement
		 */
		public void setStatement(WoolNodeBody statement) {
			this.statement = statement;
		}
	}
}
