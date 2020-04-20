package eu.woolplatform.webservice.controller;

import eu.woolplatform.utils.AppComponents;
import eu.woolplatform.utils.exception.ParseException;
import eu.woolplatform.utils.http.URLParameters;
import eu.woolplatform.webservice.*;
import eu.woolplatform.webservice.controller.model.LoginParams;
import eu.woolplatform.webservice.controller.model.LoginResult;
import eu.woolplatform.webservice.exception.*;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v{version}/auth")
public class AuthController {
	private static final Object AUTH_LOCK = new Object();

	@RequestMapping(value="/login", method= RequestMethod.POST, consumes={
			MediaType.APPLICATION_JSON_VALUE })
	public LoginResult login(
			HttpServletRequest request,
			HttpServletResponse response,
			@PathVariable("version")
			@ApiIgnore
			String versionName,
			@RequestBody
			LoginParams loginParams) throws HttpException, Exception {
		synchronized (AUTH_LOCK) {
			return QueryRunner.runAuthQuery(
					(version, user) -> doLogin(request, loginParams),
					versionName, null, response);
		}
	}

	private LoginResult doLogin(HttpServletRequest request,
			LoginParams loginParams) throws HttpException, Exception {
		Logger logger = AppComponents.getLogger(getClass().getSimpleName());
		validateForbiddenQueryParams(request, "user", "password");
		String user = loginParams.getUser();
		String password = loginParams.getPassword();
		List<HttpFieldError> fieldErrors = new ArrayList<>();
		if (user == null || user.length() == 0) {
			fieldErrors.add(new HttpFieldError("user",
					"Parameter \"user\" not defined"));
		}
		if (password == null || password.length() == 0) {
			fieldErrors.add(new HttpFieldError("password",
					"Parameter \"password\" not defined"));
		}
		if (!fieldErrors.isEmpty()) {
			logger.info("Failed login attempt: " + fieldErrors);
			throw BadRequestException.withInvalidInput(fieldErrors);
		}
		UserCredentials creds = UserFile.findUser(user);
		String invalidError = "Username or password is invalid";
		if (creds == null) {
			logger.info("Failed login attempt for user {}: user unknown",
					user);
			throw new UnauthorizedException(ErrorCode.INVALID_CREDENTIALS,
					invalidError);
		}
		if (!creds.getPassword().equals(password)) {
			logger.info("Failed login attempt for user {}: invalid credentials",
					user);
			throw new UnauthorizedException(ErrorCode.INVALID_CREDENTIALS,
					invalidError);
		}
		logger.info("User {} logged in", creds.getUsername());
		Date expiration = null;
		DateTime now = new DateTime();
		if (loginParams.getTokenExpiration() != null) {
			expiration = now.plusMinutes(loginParams.getTokenExpiration())
					.toDate();
		}
		AuthDetails details = new AuthDetails(user, now.toDate(), expiration);
		String token = AuthToken.createToken(details);
		return new LoginResult(creds.getUsername(), token);
	}

	private void validateForbiddenQueryParams(HttpServletRequest request,
			String... paramNames) throws BadRequestException {
		if (request.getQueryString() == null)
			return;
		Map<String,String> params;
		try {
			params = URLParameters.parseParameterString(
					request.getQueryString());
		} catch (ParseException ex) {
			throw new BadRequestException(ex.getMessage());
		}
		if (params == null)
			return;
		for (String name : paramNames) {
			if (params.containsKey(name)) {
				throw new BadRequestException(
						"Query parameters not accepted, parameters must be set in the request body");
			}
		}
	}
}
