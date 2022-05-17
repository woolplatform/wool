package eu.woolplatform.webservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import eu.woolplatform.webservice.controller.ErrorController;

/**
 * This exception results in a HTTP response with status 403 Forbidden. The
 * exception message (default "Forbidden") will be written to the response.
 * It is handled by the {@link ErrorController ErrorController}.
 * 
 * @author Dennis Hofs (RRD)
 */
@ResponseStatus(value=HttpStatus.FORBIDDEN)
public class ForbiddenException extends HttpException {
	private static final long serialVersionUID = 1L;

	public ForbiddenException() {
		super("Forbidden");
	}

	public ForbiddenException(String message) {
		super(message);
	}
	
	public ForbiddenException(String code, String message) {
		super(code, message);
	}
	
	public ForbiddenException(HttpError error) {
		super(error);
	}
}
