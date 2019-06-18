package nl.rrd.wool.expressions;

public class EvaluationException extends Exception {
	private static final long serialVersionUID = 1L;

	public EvaluationException() {
	}

	public EvaluationException(String message) {
		super(message);
	}

	public EvaluationException(String message, Throwable cause) {
		super(message, cause);
	}
}
