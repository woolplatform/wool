/*
 * Copyright 2019-2022 WOOL Foundation - Licensed under the MIT License:
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package eu.woolplatform.web.service;

import nl.rrd.utils.AppComponents;
import eu.woolplatform.web.service.exception.*;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.slf4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This class can run queries. It can validate an authentication token.
 * 
 * @author Dennis Hofs (RRD)
 * @author Harm op den Akker
 */
public class QueryRunner {

	/**
	 * Runs a query on the authentication database. If the HTTP request is
	 * specified, it will validate the authentication token. If there is no
	 * token in the request, or the token is empty or invalid, it throws an
	 * HttpException with 401 Unauthorized. If the request is null, it will not
	 * validate anything. This can be used for a login or signup.
	 * 
	 * @param query the query
	 * @param versionName the protocol version name (see {@link ProtocolVersion
	 * ProtocolVersion})
	 * @param request the HTTP request or null
	 * @param response the HTTP response (to add header WWW-Authenticate in
	 *                 case of 401 Unauthorized)
	 * @param woolUserId the "wool user" for which this query should be run, or ""
	 *                   if this should be for the currently authenticated user
	 * @param application the {@link Application} context used to access {@link UserCredentials}
	 *                    in a non-static way.
	 * @return the query result
	 * @throws HttpException if the query should return an HTTP error status
	 * @throws Exception if an unexpected error occurs. This results in HTTP
	 * error status 500 Internal Server Error.
	 */
	public static <T> T runQuery(AuthQuery<T> query,
			String versionName, HttpServletRequest request,
			HttpServletResponse response, String woolUserId, Application application) throws HttpException, Exception {
		ProtocolVersion version;
		try {
			version = ProtocolVersion.forVersionName(versionName);
		} catch (IllegalArgumentException ex) {
			throw new BadRequestException("Unknown protocol version: " +
					versionName);
		}
		try {
			UserCredentials user = null;
			if (request != null)
				user = validateToken(request, application);
			if(woolUserId == null || woolUserId.equals("")) { // If the request was made for "this" (authenticated) user
				String queryUserName = "";
				if(user != null) queryUserName = user.getUsername();
				return query.runQuery(version, queryUserName);
			} else if((user != null) && woolUserId.equals(user.getUsername())) { // If the request was made for a specific woolUserId that happens to be "this" (authenticated) user
				return query.runQuery(version, user.getUsername());
			} else if((user != null) && (user.getRole().equals(UserCredentials.USER_ROLE_ADMIN))) { // If "this" user is an admin
				return query.runQuery(version, user.getUsername());
			} else {
				throw new UnauthorizedException("Attempting to run query for woolUserId '"+woolUserId+"', but currently logged in user '"+user+"' is not an admin.");
			}
		} catch (UnauthorizedException ex) {
			response.addHeader("WWW-Authenticate", "None");
			throw ex;
		} catch (HttpException ex) {
			throw ex;
		} catch (Exception ex) {
			Logger logger = AppComponents.getLogger(
					QueryRunner.class.getSimpleName());
			logger.error("Internal Server Error: " + ex.getMessage(), ex);
			throw new InternalServerErrorException();
		}
	}

	/**
	 * Validates the authentication token in the specified HTTP request. If no
	 * token is specified, or the token is empty or invalid, it will throw an
	 * HttpException with 401 Unauthorized. Otherwise, it will return the {@link UserCredentials}
	 * for the authenticated user.
	 *
	 * @param request     the HTTP request
	 * @param application the {@link Application} context used to access {@link UserCredentials}
	 *                     in a non-static way.
	 * @return the {@link UserCredentials} for the authenticated user
	 * @throws UnauthorizedException if no token is specified, or the token is
	 *                               empty or invalid
	 */
	private static UserCredentials validateToken(HttpServletRequest request, Application application)
			throws UnauthorizedException {
		String token = request.getHeader("X-Auth-Token");
		if (token != null)
			return validateDefaultToken(token, application);
		throw new UnauthorizedException(ErrorCode.AUTH_TOKEN_NOT_FOUND,
				"Authentication token not found");
	}
	
	/**
	 * Validates a token from request header X-Auth-Token. If it's empty or
	 * invalid, it will throw an HttpException with 401 Unauthorized. Otherwise,
	 * it will return the user object for the authenticated user.
	 *
	 * @param token       the authentication token (not null)
	 * @param application the {@link Application} context used to access {@link UserCredentials}
	 *                    in a non-static way.
	 * @return the authenticated user
	 * @throws UnauthorizedException if the token is empty or invalid
	 */
	private static UserCredentials validateDefaultToken(String token, Application application)
			throws UnauthorizedException {
		Logger logger = AppComponents.getLogger(
				QueryRunner.class.getSimpleName());
		if (token.trim().length() == 0) {
			logger.info("Invalid authentication token: token empty");
			throw new UnauthorizedException(ErrorCode.AUTH_TOKEN_INVALID,
					"Authentication token invalid");
		}
		AuthDetails details;
		try {
			details = AuthToken.parseToken(token);
		} catch (ExpiredJwtException ex) {
			throw new UnauthorizedException(ErrorCode.AUTH_TOKEN_EXPIRED,
					"Authentication token expired");
		} catch (JwtException ex) {
			logger.info("Invalid authentication token: failed to parse: " +
					ex.getMessage());
			throw new UnauthorizedException(ErrorCode.AUTH_TOKEN_INVALID,
					"Authentication token invalid");
		}

		UserCredentials user = application.getServiceManager().getUserCredentialsForUsername(details.getSubject());
		long now = System.currentTimeMillis();
		if (user == null) {
			logger.info("Invalid authentication token: user not found: " +
					details.getSubject());
			throw new UnauthorizedException(ErrorCode.AUTH_TOKEN_INVALID,
					"Authentication token invalid");
		}
		if (details.getExpiration() != null &&
				details.getExpiration().getTime() < now) {
			throw new UnauthorizedException(ErrorCode.AUTH_TOKEN_EXPIRED,
					"Authentication token expired");
		}
		return user;
	}
}
