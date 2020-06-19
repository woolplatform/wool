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

package eu.woolplatform.wool.model.command;

import eu.woolplatform.utils.CurrentIterator;
import eu.woolplatform.utils.exception.LineNumberParseException;
import eu.woolplatform.utils.expressions.EvaluationException;
import eu.woolplatform.utils.expressions.Expression;
import eu.woolplatform.utils.expressions.Value;
import eu.woolplatform.utils.expressions.types.AssignExpression;
import eu.woolplatform.wool.model.WoolNodeBody;
import eu.woolplatform.wool.model.WoolReply;
import eu.woolplatform.wool.model.nodepointer.WoolNodePointer;
import eu.woolplatform.wool.parser.WoolBodyParser;
import eu.woolplatform.wool.parser.WoolBodyToken;
import eu.woolplatform.wool.parser.WoolNodeState;

import java.util.*;

/**
 * This class models the &lt;&lt;if ...&gt;&gt; command in Wool. It can be part
 * of a {@link WoolNodeBody WoolNodeBody} (not inside a reply).
 * 
 * @author Dennis Hofs (RRD)
 */
public class WoolIfCommand extends WoolExpressionCommand {
	private List<Clause> ifClauses = new ArrayList<>();
	private WoolNodeBody elseClause = null;

	public WoolIfCommand() {
	}

	public WoolIfCommand(WoolIfCommand other) {
		for (Clause ifClause : other.ifClauses) {
			this.ifClauses.add(new Clause(ifClause));
		}
		if (other.elseClause != null)
			this.elseClause = new WoolNodeBody(other.elseClause);
	}

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
	public WoolReply findReplyById(int replyId) {
		for (Clause clause : ifClauses) {
			WoolReply reply = clause.statement.findReplyById(replyId);
			if (reply != null)
				return reply;
		}
		if (elseClause != null)
			return elseClause.findReplyById(replyId);
		else
			return null;
	}

	@Override
	public void getReadVariableNames(Set<String> varNames) {
		for (Clause clause : ifClauses) {
			varNames.addAll(clause.expression.getVariableNames());
			clause.statement.getReadVariableNames(varNames);
		}
		if (elseClause != null)
			elseClause.getReadVariableNames(varNames);
	}

	@Override
	public void getWriteVariableNames(Set<String> varNames) {
		for (Clause clause : ifClauses) {
			clause.statement.getWriteVariableNames(varNames);
		}
		if (elseClause != null)
			elseClause.getWriteVariableNames(varNames);
	}

	@Override
	public void getNodePointers(Set<WoolNodePointer> pointers) {
		for (Clause clause : ifClauses) {
			clause.statement.getNodePointers(pointers);
		}
		if (elseClause != null)
			elseClause.getNodePointers(pointers);
	}

	@Override
	public void executeBodyCommand(Map<String, Object> variables,
			WoolNodeBody processedBody) throws EvaluationException {
		for (Clause clause : ifClauses) {
			Value clauseEval = clause.expression.evaluate(variables);
			if (clauseEval.asBoolean()) {
				clause.statement.execute(variables, false, processedBody);
				return;
			}
		}
		if (elseClause != null)
			elseClause.execute(variables, false, processedBody);
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

	public static WoolIfCommand parse(WoolBodyToken cmdStartToken,
			CurrentIterator<WoolBodyToken> tokens, WoolNodeState nodeState)
			throws LineNumberParseException {
		WoolIfCommand command = new WoolIfCommand();
		ReadContentResult content = readCommandContent(cmdStartToken, tokens);
		ParseContentResult parsedIf = parseCommandContentExpression(
				cmdStartToken, content, "if");
		checkNoAssignment(cmdStartToken, parsedIf.name, parsedIf.expression);
		while (true) {
			WoolBodyParser bodyParser = new WoolBodyParser(nodeState);
			WoolBodyParser.ParseUntilCommandClauseResult bodyParse =
					bodyParser.parseUntilCommandClause(tokens,
					Arrays.asList("action", "if", "random", "set"),
					Arrays.asList("elseif", "else", "endif"));
			if (bodyParse.cmdClauseStartToken == null) {
				throw new LineNumberParseException(
						"Command \"if\" not terminated",
						cmdStartToken.getLineNum(), cmdStartToken.getColNum());
			}
			if (parsedIf.name.equals("if") || parsedIf.name.equals("elseif")) {
				command.addIfClause(new Clause(parsedIf.expression,
						bodyParse.body));
			} else {
				command.setElseClause(bodyParse.body);
			}
			WoolBodyToken clauseStartToken = bodyParse.cmdClauseStartToken;
			String clauseName = bodyParse.cmdClauseName;
			content = readCommandContent(clauseStartToken, tokens);
			switch (clauseName) {
			case "elseif":
				if (command.elseClause != null) {
					throw new LineNumberParseException(
							"Found \"elseif\" after \"else\"",
							clauseStartToken.getLineNum(),
							clauseStartToken.getColNum());
				}
				parsedIf = parseCommandContentExpression(clauseStartToken,
						content, clauseName);
				checkNoAssignment(clauseStartToken, parsedIf.name,
						parsedIf.expression);
				break;
			case "else":
				if (command.elseClause != null) {
					throw new LineNumberParseException(
							"Found more than one \"else\"",
							clauseStartToken.getLineNum(),
							clauseStartToken.getColNum());
				}
				parsedIf = parseCommandContentName(clauseStartToken, content,
						clauseName);
				break;
			case "endif":
				parseCommandContentName(clauseStartToken, content, clauseName);
				return command;
			}
		}
	}
	
	private static void checkNoAssignment(WoolBodyToken cmdStartToken,
			String name, Expression expression)
			throws LineNumberParseException {
		List<Expression> list = new ArrayList<>();
		list.add(expression);
		list.addAll(expression.getDescendants());
		for (Expression expr : list) {
			if (expr instanceof AssignExpression) {
				throw new LineNumberParseException(String.format(
						"Found assignment expression in \"%s\" command", name),
						cmdStartToken.getLineNum(), cmdStartToken.getColNum());
			}
		}
	}

	@Override
	public WoolIfCommand clone() {
		return new WoolIfCommand(this);
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

		public Clause(Clause other) {
			this.expression = other.expression;
			this.statement = new WoolNodeBody(other.statement);
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
