package eu.woolplatform.utils.exception;

/**
 * This exception is thrown if an error occurs while performing a task.
 *
 * @author Dennis Hofs (RRD)
 */
public class TaskException extends Exception {
	private static final long serialVersionUID = -831895863323306502L;

	public TaskException(String message) {
		super(message);
	}

	public TaskException(String message, Throwable cause) {
		super(message, cause);
	}
}
