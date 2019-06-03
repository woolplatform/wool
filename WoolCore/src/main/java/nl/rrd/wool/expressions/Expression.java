package nl.rrd.wool.expressions;

import java.util.List;
import java.util.Map;

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
	 * Evaluates this expression using the specified variable values.
	 * 
	 * @param variables the variable values (can be null)
	 * @return the value of the expression
	 * @throws EvaluationException if the expression can't be evaluted with
	 * the specified variables
	 */
	Value evaluate(Map<String,?> variables) throws EvaluationException;
}
