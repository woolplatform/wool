package eu.woolplatform.utils.exception;

/**
 * This exception indicates that a client received an error message from the
 * server.
 * 
 * @author Dennis Hofs (RRD)
 */
public class ClientException extends Exception {
	private static final long serialVersionUID = 1L;

	public ClientException(String message) {
		super(message);
	}

	public ClientException(String message, Throwable cause) {
		super(message, cause);
	}
}
