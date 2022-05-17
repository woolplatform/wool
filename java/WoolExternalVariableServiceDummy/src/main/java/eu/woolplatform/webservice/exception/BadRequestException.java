package eu.woolplatform.webservice.exception;

import eu.woolplatform.webservice.controller.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This exception results in a HTTP response with status 400 Bad Request. The
 * exception message (default "Bad Request") will be written to the response.
 * It is handled by the {@link ErrorController ErrorController}.
 * 
 * @author Dennis Hofs (RRD)
 */
@ResponseStatus(value=HttpStatus.BAD_REQUEST)
public class BadRequestException extends HttpException {
	private static final long serialVersionUID = 1L;
	
	public BadRequestException() {
		super("Bad Request");
	}

	public BadRequestException(String message) {
		super(message);
	}
	
	public BadRequestException(String code, String message) {
		super(code, message);
	}
	
	public BadRequestException(HttpError error) {
		super(error);
	}
	
	public BadRequestException appendInvalidInput(BadRequestException other) {
		List<HttpFieldError> errors = new ArrayList<>();
		errors.addAll(getError().getFieldErrors());
		errors.addAll(other.getError().getFieldErrors());
		return withInvalidInput(errors);
	}
	
	public BadRequestException appendInvalidInput(
			HttpFieldError... fieldErrors) {
		return appendInvalidInput(Arrays.asList(fieldErrors));
	}
	
	public BadRequestException appendInvalidInput(
			List<HttpFieldError> fieldErrors) {
		List<HttpFieldError> newErrors = new ArrayList<>();
		newErrors.addAll(getError().getFieldErrors());
		newErrors.addAll(fieldErrors);
		return withInvalidInput(newErrors);
	}
	
	public static BadRequestException withInvalidInput(
			HttpFieldError... fieldErrors) {
		return withInvalidInput(Arrays.asList(fieldErrors));
	}

	public static BadRequestException withInvalidInput(
			List<HttpFieldError> fieldErrors) {
		StringBuilder errorMsg = new StringBuilder();
		String newline = System.getProperty("line.separator");
		for (HttpFieldError fieldError : fieldErrors) {
			if (errorMsg.length() > 0)
				errorMsg.append(newline);
			errorMsg.append(fieldError.getMessage());
		}
		HttpError error = new HttpError(ErrorCode.INVALID_INPUT,
				errorMsg.toString());
		for (HttpFieldError fieldError : fieldErrors) {
			error.addFieldError(fieldError);
		}
		return new BadRequestException(error);
	}
}
