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

import nl.rrd.utils.AppComponents;
import eu.woolplatform.web.logservice.ProtocolVersion;
import eu.woolplatform.web.logservice.QueryRunner;
import eu.woolplatform.web.logservice.controller.schema.WoolVariablePayload;
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
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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

	

	// -----------------------------------------------------------------------
	// -------------------- END-POINT: "retrieve-updates" --------------------
	// -----------------------------------------------------------------------

	@Operation(summary = "Retrieve updates for a given list of WOOL Variables",
		description = "The use case for this end-point is as follows. Before executing a WOOL " +
			"Dialogue, you (or e.g. the WOOL Web Service) may gather a list of all the WOOL " +
			"Variables used in its execution. Before starting the execution, you may call this " +
			"end-point with the list of WOOL Variables in order to verify that you have the " +
			"latest values for all variables. In return you will receive a list - which is a " +
			"subset of the list you provided - that contains all WOOL Variables for which an " +
			"updated value is available. You are basically asking: 'Hey, I have this list of " +
			"WOOL Variables for this user, is this up-to-date?'." +
			"<br/><br/>You must pass along the current timezone of the user (client) so that " +
			"certain time sensitive variables may be correctly set according to the timezone of " +
			"the user." +
			"<br/><br/>In this dummy implementation, for every variable that you include in the " +
			"request list there is a 50% chance that it will be returned in the response list, " +
			"with the same value as provided in the request, and the lastUpdated time set to the " +
			"current UTC time in epoch seconds.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200",
			description = "Successful operation",
			content = @Content(
				array = @ArraySchema(
					schema = @Schema(implementation = WoolVariablePayload.class)))) })
	@RequestMapping(value="/retrieve-updates", method= RequestMethod.POST, consumes={
			MediaType.APPLICATION_JSON_VALUE })
	public List<WoolVariablePayload> retrieveUpdates (
			HttpServletRequest request,
			HttpServletResponse response,

			@Parameter(hidden = true, description = "API Version to use, e.g. '1'")
			@PathVariable(value = "version")
			String versionName,

			@Parameter(description = "The userId of the WOOL user")
			@RequestParam(value="userId")
			String userId,

			@Parameter(description = "The current time zone of the WOOL user")
			@RequestParam(value="timeZone")
			String timeZone,

			@Parameter(description = "List of WOOL Variables for which to check for updates.",
					required = true,
					content = @Content(
						array = @ArraySchema(
							schema = @Schema(implementation = WoolVariablePayload.class))))
			@RequestBody List<WoolVariablePayload> woolVariables) throws Exception {

		// If no explicit protocol version is provided, assume the latest version
		if(versionName == null) versionName = ProtocolVersion.getLatestVersion().versionName();

		// Log this call to the service log
		logger.info("POST /v"+versionName+"/variables/retrieve-updates?userId=" + userId +
				"&timeZone=" + timeZone + " with the following variables:");
		for(WoolVariablePayload woolVariableResultParam : woolVariables) {
			logger.info(woolVariableResultParam.toString());
		}

		// Execute either for the provided userId or for the currently logged in user
		if(userId.equals("")) {
			return QueryRunner.runQuery(
				(version, user) -> executeRetrieveUpdates(user, timeZone, woolVariables),
				versionName, request, response, userId);
		} else {
			return QueryRunner.runQuery(
				(version, user) -> executeRetrieveUpdates(userId, timeZone, woolVariables),
				versionName, request, response, userId);
		}
	}

	/**
	 * This method performs the "dummy" updating of the requested list of WOOL Variables. For a
	 * real-world implementation you should replace this method and make sure it does something
	 * useful.
	 *
	 * In this dummy implementation, the method does the following. For every WOOL Variable for
	 * which an update is requested, there is a 50% chance that this variable will be included in
	 * the result set with a lastUpdated timestamp of "now" (in the provided time zone of the user).
	 *
	 * @param userId the {@code String} identifier of the user who's variable updates are requested.
	 * @param timeZone the time zone of the user as one of {@code TimeZone.getAvailableIDs()}
	 *                 (IANA Codes)
	 * @param params the {@code List} of {@link WoolVariablePayload}s for which it should be
	 *               verified if an update is needed.
	 * @return a {@code List} of {@link WoolVariablePayload}s with each of the parameters for which
	 * an updated value has been found (note that this may be an empty list).
	 */
	private List<WoolVariablePayload> executeRetrieveUpdates (String userId, String timeZone,
															  List<WoolVariablePayload> params)
			throws BadRequestException {

		// Parse the timeZone String into a ZoneId to verify it was given in the right format
		ZoneId timeZoneId = ControllerFunctions.parseTimeZone(timeZone);

		List<WoolVariablePayload> result = new ArrayList<>();

		Random random = new Random();

		for (WoolVariablePayload param : params) {

			if(param.getUpdatedTime() != null && param.getUpdatedTimeZone() != null) {
				ZonedDateTime paramZonedDateTime =
						ZonedDateTime.ofInstant(
								Instant.ofEpochMilli(
										param.getUpdatedTime()),
								ControllerFunctions.parseTimeZone(param.getUpdatedTimeZone()));

				DateTimeFormatter formatter =
						DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss Z");
				String readableTimeString = paramZonedDateTime.format(formatter);
				logger.info("The WOOL Variable '"+param.getName()+"' " +
						"with value '"+param.getValue()+"' " +
						"was last updated at '"+param.getUpdatedTime()+"', " +
						"which was '"+readableTimeString+"' " +
						"in time zone: '"+param.getUpdatedTimeZone()+"'.");
			} else {
				logger.info("The WOOL Variable '"+param.getName()+"' " +
						"with value '"+param.getValue()+"' " +
						"was last updated at an unknown time.");
			}

			// With 50% chance, return the variable as if it has been updated
			if(random.nextBoolean()) {
				WoolVariablePayload newParam = new WoolVariablePayload(
						param.getName(),
						param.getValue(),
						Instant.now().toEpochMilli(),
						timeZone);
				result.add(newParam);
			}
		}

		return result;
	}

	// ---------------------------------------------------------------------
	// -------------------- END-POINT: "notify-updated" --------------------
	// ---------------------------------------------------------------------

	@Operation(summary = "Inform that the given list of WOOL Variables have been updated",
		description = "With this end-point you can inform this WOOL External Variable Service " +
			"that a list of WOOL Variables have been updated (e.g. during dialogue execution) " +
			"for a particular user." +
			"<br/><br/>You must pass along the current timezone of the user (client) so that " +
			"certain time sensitive variables may be correctly set according to the timezone of " +
			"the user." +
			"<br/><br/>In this dummy implementation, the service will do nothing with your " +
			"provided variables, and the end-point will simply return a status 200 (OK).")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful operation") })
	@RequestMapping(value="/notify-updated", method= RequestMethod.POST, consumes={
			MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<?> notifyUpdated(
		HttpServletRequest request,
		HttpServletResponse response,

		@Parameter(hidden = true, description = "API Version to use, e.g. '1'")
		@PathVariable(value = "version")
		String versionName,

		@Parameter(description = "The userId of the WOOL user")
		@RequestParam(value="userId")
		String userId,

		@Parameter(description = "The current time zone of the WOOL user")
		@RequestParam(value="timeZone")
		String timeZone,

		@Parameter(
			description = "List of WOOL Variables for which to check for updates.",
			required = true,
			content = @Content(
				array = @ArraySchema(
					schema = @Schema(
						implementation = WoolVariablePayload.class)
				)
			)
		)
		@RequestBody List<WoolVariablePayload> woolVariables
	) throws Exception {

		// If no explicit protocol version is provided, assume the latest version
		if(versionName == null) versionName = ProtocolVersion.getLatestVersion().versionName();

		// Log this call to the service log
		logger.info("POST /v"+versionName+"/variables/notify-updated?userId=" + userId +
				"&timeZone=" + timeZone + " with the following variables:");
		for(WoolVariablePayload woolVariableResultParam : woolVariables) {
			logger.info(woolVariableResultParam.toString());
		}

		// Execute either for the provided userId or for the currently logged-in user
		// TODO: Not correct, an error should be thrown on an empty userId
		if(userId.equals("")) {
			return QueryRunner.runQuery(
				(version, user) -> executeNotifyUpdated(user, timeZone, woolVariables),
				versionName, request, response, userId);
		} else {
			return QueryRunner.runQuery(
				(version, user) -> executeNotifyUpdated(userId, timeZone, woolVariables),
				versionName, request, response, userId);
		}

	}

	/**
	 * This method performs the Dummy implementation for updating the given variables in the
	 * external service. As this is a dummy implementation and there is no real external service,
	 * the implementation is simply to return "OK", without doing anything.
	 *
	 * @param userId the {@code String} identifier of the user for whom variable updates are
	 *               available.
	 * @param timeZone the timeZone of the user as one of {@code TimeZone.getAvailableIDs()} (IANA
	 *                 Codes)
	 * @param params the {@code List} of {@link WoolVariablePayload}s that were updated and may need
	 *               to be processed in the external service.
	 * @return a {@link ResponseEntity} to indicate whether the update was executed successfully.
	 */
	private ResponseEntity<?> executeNotifyUpdated(String userId, String timeZone,
												   List<WoolVariablePayload> params)
			throws BadRequestException {

		// Parse the timeZone String into a ZoneId to verify it was given in the right format
		ControllerFunctions.parseTimeZone(timeZone);

		return new ResponseEntity<ResponseEntity<?>>(HttpStatus.OK);
	}

	// ---------------------------------------------------------------------
	// -------------------- END-POINT: "notify-cleared" --------------------
	// ---------------------------------------------------------------------

	@Operation(summary = "Inform that the WOOL Variable store has been completed cleared",
		description = "With this end-point you can inform this WOOL External Variable Service " +
			"that a full clear of the WOOL Variable Store has occurred for a particular user" +
			"<br/><br/>You must pass along the current timezone of the user (client) so that " +
			"certain time sensitive variables may be correctly set according to the timezone of " +
			"the user." +
			"<br/><br/>In this dummy implementation, the service will do nothing and will simply " +
			"return a status 200 (OK).")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful operation") })
	@RequestMapping(value="/notify-cleared", method= RequestMethod.POST)
	public ResponseEntity<?> notifyCleared(
			HttpServletRequest request,
			HttpServletResponse response,

			@Parameter(hidden = true, description = "API Version to use, e.g. '1'")
			@PathVariable(value = "version")
			String versionName,

			@Parameter(description = "The userId of the WOOL user")
			@RequestParam(value="userId")
			String userId,

			@Parameter(description = "The current time zone of the WOOL user")
			@RequestParam(value="timeZone")
			String timeZone
	) throws Exception {

		// If no explicit protocol version is provided, assume the latest version
		if(versionName == null) versionName = ProtocolVersion.getLatestVersion().versionName();

		// Log this call to the service log
		logger.info("POST /v"+versionName+"/variables/notify-cleared?userId=" + userId +
				"&timeZone=" + timeZone);

		// Execute either for the provided userId or for the currently logged-in user
		// TODO: Not correct, an error should be thrown on an empty userId
		if(userId.equals("")) {
			return QueryRunner.runQuery(
					(version, user) -> executeNotifyCleared(user, timeZone),
					versionName, request, response, userId);
		} else {
			return QueryRunner.runQuery(
					(version, user) -> executeNotifyCleared(userId, timeZone),
					versionName, request, response, userId);
		}

	}

	/**
	 * This method performs the Dummy implementation for receiving the notification that a WOOL
	 * Variable store has been completely cleared. As this is a dummy implementation and there is no
	 * real external service, the implementation is simply to return "OK", without doing anything.
	 *
	 * @param userId the {@code String} identifier of the user for whom variable updates are
	 *               available.
	 * @param timeZone the timeZone of the user as one of {@code TimeZone.getAvailableIDs()} (IANA
	 *                 Codes)
	 * @return a {@link ResponseEntity} to indicate whether the update was executed successfully.
	 */
	private ResponseEntity<?> executeNotifyCleared(String userId, String timeZone)
			throws BadRequestException {

		// Parse the timeZone String into a ZoneId to verify it was given in the right format
		ControllerFunctions.parseTimeZone(timeZone);

		return new ResponseEntity<ResponseEntity<?>>(HttpStatus.OK);
	}

}
