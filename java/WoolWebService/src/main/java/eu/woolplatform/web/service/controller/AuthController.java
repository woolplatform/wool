/*
 * Copyright 2019-2022 WOOL Foundation.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */
package eu.woolplatform.web.service.controller;

import eu.woolplatform.utils.AppComponents;
import eu.woolplatform.utils.exception.ParseException;
import eu.woolplatform.utils.http.URLParameters;
import eu.woolplatform.web.service.controller.model.LoginParams;
import eu.woolplatform.web.service.exception.BadRequestException;
import eu.woolplatform.web.service.exception.ErrorCode;
import eu.woolplatform.web.service.exception.HttpFieldError;
import eu.woolplatform.web.service.exception.UnauthorizedException;
import eu.woolplatform.web.service.*;
import eu.woolplatform.web.service.controller.model.LoginResult;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v{version}/auth")
public class AuthController {
	@Autowired
	Application application;

	private static final Object AUTH_LOCK = new Object();

	@RequestMapping(value="/login", method= RequestMethod.POST, consumes={
			MediaType.APPLICATION_JSON_VALUE })
	public LoginResult login(
			HttpServletRequest request,
			HttpServletResponse response,
			@PathVariable("version")
			String versionName,
			@RequestBody
			LoginParams loginParams) throws Exception {
		synchronized (AUTH_LOCK) {
			return QueryRunner.runQuery(
					(version, user) -> doLogin(request, loginParams),
					versionName, null, response, "", application);
		}
	}

	private LoginResult doLogin(HttpServletRequest request,
			LoginParams loginParams) throws Exception {
		Logger logger = AppComponents.getLogger(getClass().getSimpleName());
		validateForbiddenQueryParams(request, "user", "password");
		String user = loginParams.getUser();
		String password = loginParams.getPassword();
		Integer tokenExpiration = loginParams.getTokenExpiration();
		List<HttpFieldError> fieldErrors = new ArrayList<>();
		if (user == null || user.length() == 0) {
			fieldErrors.add(new HttpFieldError("user",
					"Parameter \"user\" not defined"));
		}
		if (password == null || password.length() == 0) {
			fieldErrors.add(new HttpFieldError("password",
					"Parameter \"password\" not defined"));
		}
		if (tokenExpiration != null && tokenExpiration <= 0) {
			fieldErrors.add(new HttpFieldError("tokenExpiration",
					"Parameter \"tokenExpiration\" must be greater than 0 or \"never\""));
		}
		if (!fieldErrors.isEmpty()) {
			logger.info("Failed login attempt: " + fieldErrors);
			throw BadRequestException.withInvalidInput(fieldErrors);
		}
		UserCredentials userCredentials = UserFile.findUser(user);
		String invalidError = "Username or password is invalid";
		if (userCredentials == null) {
			logger.info("Failed login attempt for user {}: user unknown",
					user);
			throw new UnauthorizedException(ErrorCode.INVALID_CREDENTIALS,
					invalidError);
		}
		if (!userCredentials.getPassword().equals(password)) {
			logger.info("Failed login attempt for user {}: invalid credentials",
					user);
			throw new UnauthorizedException(ErrorCode.INVALID_CREDENTIALS,
					invalidError);
		}
		logger.info("User {} logged in", userCredentials.getUsername());
		Date expiration = null;
		DateTime now = new DateTime();
		if (loginParams.getTokenExpiration() != null) {
			expiration = now.plusMinutes(loginParams.getTokenExpiration())
					.toDate();
		}
		AuthDetails details = new AuthDetails(user, now.toDate(), expiration);
		String token = AuthToken.createToken(details);
		return new LoginResult(userCredentials.getUsername(), token);
	}

	private void validateForbiddenQueryParams(HttpServletRequest request,
			String... paramNames) throws BadRequestException {
		if (request.getQueryString() == null)
			return;
		Map<String,String> params;
		try {
			params = URLParameters.parseParameterString(request.getQueryString());
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
