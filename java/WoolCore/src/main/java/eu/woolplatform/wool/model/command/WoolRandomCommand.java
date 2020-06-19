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
import eu.woolplatform.wool.model.WoolNodeBody;
import eu.woolplatform.wool.model.WoolReply;
import eu.woolplatform.wool.model.nodepointer.WoolNodePointer;
import eu.woolplatform.wool.parser.WoolBodyParser;
import eu.woolplatform.wool.parser.WoolBodyToken;
import eu.woolplatform.wool.parser.WoolNodeState;

import java.util.*;

/**
 * This class models the &lt;&lt;random ...&gt;&gt; command in Wool. It can be
 * part of a {@link WoolNodeBody WoolNodeBody} (not inside a reply).
 * 
 * @author Dennis Hofs (RRD)
 */
public class WoolRandomCommand extends WoolAttributesCommand {
	private final Random random = new Random();

	private List<Clause> clauses = new ArrayList<>();

	public WoolRandomCommand() {
	}

	public WoolRandomCommand(WoolRandomCommand other) {
		for (Clause clause : other.clauses) {
			this.clauses.add(new Clause(clause));
		}
	}

	/**
	 * Returns the clauses. There should be at least one clause.
	 * 
	 * @return the clauses
	 */
	public List<Clause> getClauses() {
		return clauses;
	}

	/**
	 * Sets the clauses. There should be at least one clause.
	 * 
	 * @param clauses the clauses
	 */
	public void setClauses(List<Clause> clauses) {
		this.clauses = clauses;
	}
	
	/**
	 * Adds a clause. There should be at least one clause.
	 * 
	 * @param clause the clause
	 */
	public void addClause(Clause clause) {
		clauses.add(clause);
	}

	@Override
	public WoolReply findReplyById(int replyId) {
		for (Clause clause : clauses) {
			WoolReply reply = clause.statement.findReplyById(replyId);
			if (reply != null)
				return reply;
		}
		return null;
	}

	@Override
	public void getReadVariableNames(Set<String> varNames) {
		for (Clause clause : clauses) {
			clause.statement.getReadVariableNames(varNames);
		}
	}

	@Override
	public void getWriteVariableNames(Set<String> varNames) {
		for (Clause clause : clauses) {
			clause.statement.getWriteVariableNames(varNames);
		}
	}

	@Override
	public void getNodePointers(Set<WoolNodePointer> pointers) {
		for (Clause clause : clauses) {
			clause.statement.getNodePointers(pointers);
		}
	}

	@Override
	public void executeBodyCommand(Map<String, Object> variables,
			WoolNodeBody processedBody) throws EvaluationException {
		float totalWeight = 0;
		for (Clause clause : clauses) {
			totalWeight += clause.weight;
		}
		float selWeight = random.nextFloat() * totalWeight;
		float currWeight = 0;
		Clause selClause = null;
		for (int i = 0; selClause == null && i < clauses.size(); i++) {
			Clause clause = clauses.get(i);
			currWeight += clause.weight;
			if (selWeight <= currWeight)
				selClause = clause;
		}
		if (selClause == null)
			selClause = clauses.get(clauses.size() - 1);
		selClause.statement.execute(variables, false, processedBody);
	}

	@Override
	public String toString() {
		String newline = System.getProperty("line.separator");
		Clause clause = clauses.get(0);
		StringBuilder result = new StringBuilder("<<random");
		if (clause.weight != 1)
			result.append(" weight=\"" + clause.weight + "\"");
		result.append(">>" + newline);
		result.append(clause.statement + newline);
		for (int i = 1; i < clauses.size(); i++) {
			clause = clauses.get(i);
			result.append("<<or");
			if (clause.weight != 1)
				result.append(" weight=\"" + clause.weight + "\"");
			result.append(">>" + newline);
			result.append(clause.statement + newline);
		}
		result.append("<<endrandom>>");
		return result.toString();
	}

	public static WoolRandomCommand parse(WoolBodyToken cmdStartToken,
			CurrentIterator<WoolBodyToken> tokens, WoolNodeState nodeState)
			throws LineNumberParseException {
		Map<String,WoolBodyToken> attrs = parseAttributesCommand(cmdStartToken,
				tokens);
		WoolRandomCommand command = new WoolRandomCommand();
		Float weight = readFloatAttr("weight", attrs, cmdStartToken, false, 0f,
				null);
		if (weight == null)
			weight = 1f;
		while (true) {
			WoolBodyParser bodyParser = new WoolBodyParser(nodeState);
			WoolBodyParser.ParseUntilCommandClauseResult bodyParse =
					bodyParser.parseUntilCommandClause(tokens,
					Arrays.asList("action", "if", "random", "set"),
					Arrays.asList("or", "endrandom"));
			if (bodyParse.cmdClauseStartToken == null) {
				throw new LineNumberParseException(
						"Command \"random\" not terminated",
						cmdStartToken.getLineNum(), cmdStartToken.getColNum());
			}
			command.addClause(new Clause(weight, bodyParse.body));
			WoolBodyToken clauseStartToken = bodyParse.cmdClauseStartToken;
			String clauseName = bodyParse.cmdClauseName;
			attrs = parseAttributesCommand(clauseStartToken, tokens);
			switch (clauseName) {
			case "or":
				weight = readFloatAttr("weight", attrs, cmdStartToken, false,
						0f, null);
				if (weight == null)
					weight = 1f;
				break;
			case "endrandom":
				return command;
			}
		}
	}

	@Override
	public WoolRandomCommand clone() {
		return new WoolRandomCommand(this);
	}

	/**
	 * This class models a clause of a "random" statement. That is the "random"
	 * clause or an "or" clause.
	 */
	public static class Clause {
		private float weight;
		private WoolNodeBody statement;

		/**
		 * Constructs a new clause.
		 * 
		 * @param weight the weight for this clause
		 * @param statement the statement that should be output if this clause
		 * is selected
		 */
		public Clause(float weight, WoolNodeBody statement) {
			this.weight = weight;
			this.statement = statement;
		}

		public Clause(Clause other) {
			this.weight = other.weight;
			this.statement = new WoolNodeBody(other.statement);
		}

		/**
		 * Returns the weight for this clause.
		 *
		 * @return the weight for this clause
		 */
		public float getWeight() {
			return weight;
		}

		/**
		 * Sets the weight for this clause.
		 *
		 * @param weight the weight for this clause
		 */
		public void setWeight(float weight) {
			this.weight = weight;
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
