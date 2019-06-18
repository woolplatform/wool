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

package nl.rrd.wool.expressions;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An expression is some code that can be evaluated as a {@link Value Value}.
 * Instances can be obtained from text input using the {@link ExpressionParser
 * ExpressionParser}. It has one method: {@link #evaluate(Map) evaluate()},
 * which takes a map of variables. The expression may contain variable names
 * and their values will be taken from the map. A variable name that is not
 * included in the map, will be evaluated as null. The variable values should be
 * the same elementary types as in {@link Value Value}. That is one of the
 * following:
 * 
 * <p><ul>
 * <li>null</li>
 * <li>{@link String String}</li>
 * <li>{@link Number Number}</li>
 * <li>{@link Boolean Boolean}</li>
 * <li>{@link List List}</li>
 * <li>{@link Map Map}: the keys must be strings</li>
 * </ul></p>
 * 
 * <p>Each element of a list or map should also be one of these types.</p>
 * 
 * @author Dennis Hofs (RRD)
 */
public interface Expression {
	
	/**
	 * Evaluates this expression using the specified variable values. The
	 * variable map may be modified as a result of an "assign" expression.
	 * 
	 * @param variables the variable values (can be null)
	 * @return the value of the expression
	 * @throws EvaluationException if the expression can't be evaluted with
	 * the specified variables
	 */
	Value evaluate(Map<String,Object> variables) throws EvaluationException;

	/**
	 * Returns the child expressions of this expression.
	 * 
	 * @return the child expressions
	 */
	List<Expression> getChildren();
	
	/**
	 * Returns all descendant expressions of this expression.
	 * 
	 * @return the descendant expressions
	 */
	List<Expression> getDescendants();

	/**
	 * Returns all variable names that occur in this expression and its
	 * descendants.
	 * 
	 * @return the variable names
	 */
	Set<String> getVariableNames();
}
