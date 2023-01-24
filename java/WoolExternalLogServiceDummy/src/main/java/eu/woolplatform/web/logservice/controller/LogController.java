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

package eu.woolplatform.web.logservice.controller;

import eu.woolplatform.web.logservice.ProtocolVersion;
import eu.woolplatform.web.logservice.QueryRunner;
import eu.woolplatform.web.logservice.controller.schema.LogEventPayload;
import eu.woolplatform.web.logservice.exception.BadRequestException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import nl.rrd.utils.AppComponents;
import org.slf4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * Controller for the /log/ -end-points of the WOOL External Log Service Dummy.
 *
 * @author Harm op den Akker
 */
@RestController
@SecurityRequirement(name = "X-Auth-Token")
@RequestMapping("/v{version}/log")
@Tag(name = "2. Logging",
	 description = "End-points for consuming logging info from a WOOL Web Service")
public class LogController {

	private final Logger logger = AppComponents.getLogger(getClass().getSimpleName());

	// ---------------------------------------------------------------------------
	// -------------------- END-POINT: "push-dialogue-events" --------------------
	// ---------------------------------------------------------------------------

	@Operation(summary = "Pushes a list of dialogue log events for the given user",
		description = "Notify the logging service of a new set of dialogue-related events for a " +
				"given user.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200",
			description = "Successful operation",
			content = @Content(
				array = @ArraySchema(
					schema = @Schema(implementation = LogEventPayload.class)))) })
	@RequestMapping(value="/push-dialogue-events", method= RequestMethod.POST, consumes={
			MediaType.APPLICATION_JSON_VALUE })
	public Object pushDialogueEvents (
			HttpServletRequest request,
			HttpServletResponse response,

			@Parameter(hidden = true, description = "API Version to use, e.g. '1'")
			@PathVariable(value = "version")
			String versionName,

			@Parameter(description = "The userId of the WOOL user")
			@RequestParam(value="userId")
			String userId,

			@Parameter(description = "List of Dialogue Log Events that should be stored.",
					required = true,
					content = @Content(
						array = @ArraySchema(
							schema = @Schema(implementation = LogEventPayload.class))))
			@RequestBody List<LogEventPayload> logEvents) throws Exception {

		// If no explicit protocol version is provided, assume the latest version
		if(versionName == null) versionName = ProtocolVersion.getLatestVersion().versionName();

		// Log this call to the service log
		logger.info("POST /v"+versionName+"/log/push-dialogue-events?userId=" + userId +
				" with the following log events:");
		for(LogEventPayload logEvent : logEvents) {
			logger.info(logEvent.toString());
		}

		return QueryRunner.runQuery(
			(version, user) -> executePushLogEvents(userId, logEvents),
				versionName, request, response);
	}

	/**
	 * This method performs the "dummy" operation of receiving log events for a given wool user. For
	 * a real-world implementation you should replace this method and make sure it does something
	 * useful.
	 *
	 * @param userId the {@code String} identifier of the user for whom new log events are being
	 *               pushed.
	 * @param logEvents the {@code List} of {@link LogEventPayload}s that are being pushed.
	 * @return {@code null}.
	 */
	private Object executePushLogEvents (String userId, List<LogEventPayload> logEvents)
			throws BadRequestException {

		return null;

	}

}
