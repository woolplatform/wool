package eu.woolplatform.webservice.exception;

/**
 * Possible error codes that may be returned by the R2D2 service.
 * 
 * @author Dennis Hofs (RRD)
 */
public class ErrorCode {
	public static final String AUTH_TOKEN_NOT_FOUND = "AUTH_TOKEN_NOT_FOUND";
	public static final String AUTH_TOKEN_INVALID = "AUTH_TOKEN_INVALID";
	public static final String AUTH_TOKEN_EXPIRED = "AUTH_TOKEN_EXPIRED";
	public static final String INVALID_INPUT = "INVALID_INPUT";
	public static final String INVALID_CREDENTIALS = "INVALID_CREDENTIALS";
}
