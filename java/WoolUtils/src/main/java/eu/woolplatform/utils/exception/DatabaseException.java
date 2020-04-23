package eu.woolplatform.utils.exception;

public class DatabaseException extends Exception {
	private static final long serialVersionUID = -3551210930766623185L;

	public DatabaseException() {
		super();
	}

	public DatabaseException(String message, Throwable cause) {
		super(message, cause);
	}

	public DatabaseException(String message) {
		super(message);
	}

	public DatabaseException(Throwable cause) {
		super(cause);
	}
}
