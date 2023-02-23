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

package eu.woolplatform.web.service.controller;

import eu.woolplatform.web.service.Application;
import eu.woolplatform.web.service.ProtocolVersion;
import eu.woolplatform.web.service.QueryRunner;
import eu.woolplatform.web.service.execution.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import nl.rrd.utils.AppComponents;
import nl.rrd.utils.exception.DatabaseException;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for the /log/... end-points of the WOOL Web Service. These end-points provide external
 * access to logged dialogue information.
 *
 * @author Harm op den Akker
*/

@RestController
@SecurityRequirement(name = "X-Auth-Token")
@RequestMapping(value = {"/v{version}/log", "/log"})
@Tag(name = "4. Logging", description = "End-points for retrieving information about logged" +
		" dialogues")
public class LogController {

	@Autowired
	Application application;

	private final Logger logger = AppComponents.getLogger(getClass().getSimpleName());

	// ------------------------------------------------------ //
	// ---------- END-POINT: "/log/verify-id" ---------- //
	// ------------------------------------------------------ //

	@Operation(
		summary = "Verify whether a dialogue session identifier is already in use for a given " +
				"user.",
		description = "When starting a dialogue (through the /dialogue/start end-point), you can " +
				"provide an optional sessionId that will be stored in the dialogue logs. If you " +
				"do not provide one, an identifier is generated that is guaranteed to be unique. " +
				"If you do want to provide a sessionId, you can use this method first to verify " +
				"that the identifier is not yet in use.")
	@RequestMapping(value="/verify-id", method= RequestMethod.GET)
	public Boolean verifyId(
			HttpServletRequest request,
			HttpServletResponse response,

			@Parameter(hidden = true, description = "API Version to use, e.g. '1'")
			@PathVariable(value = "version")
			String version,

			@Parameter(description = "Session ID for which to check its existence.")
			@RequestParam(value="sessionId")
			String sessionId,

			@Parameter(description = "The user for which to check the session id (if left empty" +
					"it is assumed to be the currently logged in user.")
			@RequestParam(value="woolUserId", required = false)
			String woolUserId) throws Exception {

		// If no versionName is provided, or versionName is empty, assume the latest version
		if (version == null || version.equals("")) {
			version = ProtocolVersion.getLatestVersion().versionName();
		}

		// Log this call to the service log
		String logInfo = "GET /v" + version + "/log/verify-id?sessionId=" + sessionId;
		if(woolUserId != null && !woolUserId.equals("")) logInfo += "&woolUserId=" + woolUserId;
		logger.info(logInfo);

		if(woolUserId == null || woolUserId.equals("")) {
			return QueryRunner.runQuery(
					(protocolVersion, user) -> doVerifyId(user, sessionId),
					version, request, response, woolUserId, application);
		} else {
			return QueryRunner.runQuery(
					(protocolVersion, user) -> doVerifyId(woolUserId, sessionId),
					version, request, response, woolUserId, application);
		}

	}

	private Boolean doVerifyId(String woolUserId, String sessionId) throws DatabaseException {
		// TODO: Test this method.
		UserService userService = application.getApplicationManager().getActiveUserService(woolUserId);
		return userService.existsSessionId(sessionId);
	}

}
