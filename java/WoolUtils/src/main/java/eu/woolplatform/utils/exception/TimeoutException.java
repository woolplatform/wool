package eu.woolplatform.utils.exception;

public class TimeoutException extends Exception {
	public TimeoutException(String message) {
		super(message);
	}

	public TimeoutException(String message, Throwable cause) {
		super(message, cause);
	}
}
