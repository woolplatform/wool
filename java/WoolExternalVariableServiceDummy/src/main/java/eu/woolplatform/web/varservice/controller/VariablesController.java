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
package eu.woolplatform.web.varservice.controller;

import eu.woolplatform.utils.AppComponents;
import eu.woolplatform.web.varservice.QueryRunner;
import eu.woolplatform.web.varservice.controller.model.WoolVariableParam;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Controller for the /variables/ -end-points of the WOOL External Variable Service
 * Dummy.
 *
 * @author Harm op den Akker
 * @author Tessa Beinema
 */
@RestController
@SecurityRequirement(name = "X-Auth-Token")
@RequestMapping("/v{version}/variables")
@Tag(name = "Variables", description = "End-points for retrieving variables from- and sending to the service")
public class VariablesController {

	private Logger logger = AppComponents.getLogger(getClass().getSimpleName());

	// ----- END-POINT: "retrieve-updates"

	@Operation(summary = "Retrieve updates for a given list of WOOL Variables",
			description = "The use case for this end-point is as follows. Before executing a WOOL Dialogue, " +
					"you (or e.g. the WOOL Web Service) may gather a list of all the WOOL Variables used in its " +
					"execution. Before starting the execution, you may call this end-point with the list of WOOL " +
					"Variables in order to verify that you have the latest values for all variables. In return you" +
					"will receive a list - which is a subset of the list you provided - that contains all WOOL " +
					"Variables for which an updated value is available. You are basically asking: 'Hey, I have this " +
					"list of WOOL Variables for this user, is this up-to-date?'.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful operation",
					content = @Content(array = @ArraySchema(schema = @Schema(implementation = WoolVariableParam.class)))) })
	@RequestMapping(value="/retrieve-updates/{userId}", method= RequestMethod.POST, consumes={
			MediaType.APPLICATION_JSON_VALUE })
	public List<WoolVariableParam> retrieveUpdates (
			HttpServletRequest request,
			HttpServletResponse response,
			@Parameter(description = "API Version to use, e.g. '1.0.0'")
				@PathVariable("version") String versionName,
			@Parameter(description = "The userId of the WOOL user")
				@PathVariable String userId,
			@Parameter(description = "List of WOOL Variables for which to check for updates.",
					required=true, content = @Content(array = @ArraySchema(schema = @Schema(implementation = WoolVariableParam.class))))
				@RequestBody List<WoolVariableParam> woolVariables) throws Exception {

		// Log endpoint calls:
		String logInfo = "POST /retrieve-updates?userId=" + userId;
		logger.info(logInfo);

		if(userId.equals("")) {
			return QueryRunner.runQuery(
				(version, user) -> executeRetrieveUpdates(user, woolVariables),
				versionName, request, response, userId);
		} else {
			return QueryRunner.runQuery(
				(version, user) -> executeRetrieveUpdates(userId, woolVariables),
				versionName, request, response, userId);
		}
	}

	/**
	 * This method performs the "dummy" updating of the requested list of WOOL Variables. For a real-world
	 * implementation you should replace this method and make sure it does something useful.
	 *
	 * In this dummy implementation, the method does the following. There is a 50% chance that this
	 * method will return an empty list (i.e. no variables need to be updated). The other 50% of the
	 * time, this method will return the given input list of parameters, with each of the {@code value}s reversed,
	 * and the {@code lastUpdated} set to the current time.
	 *
	 * @param userId the {@code String} identifier of the user who's variable updates are requested.
	 * @param params the {@code List} of {@link WoolVariableParam}s for which it should be verified if an update is needed.
	 * @return a {@code List} of {@link WoolVariableParam}s with each of the parameters for which an updated value has been found
	 * (note that this may be an empty list).
	 */
	private List<WoolVariableParam> executeRetrieveUpdates (String userId, List<WoolVariableParam> params) {

		List<WoolVariableParam> result = new ArrayList<>();

		Random random = new Random();

		if(random.nextBoolean()) { // With 50% chance, reverse all parameter values

			for (WoolVariableParam param : params) {
				WoolVariableParam newParam = new WoolVariableParam(
						param.getName(),
						new StringBuilder(param.getValue()).reverse().toString(),
						System.currentTimeMillis());
				result.add(newParam);
			}

		}

		return result;
	}

	// ----- END-POINT: "notify-updated"

	@Operation(summary = "Inform that the given list of WOOL Variables have been updated",
			description = "With this end-point you can inform this WOOL External Variable Service " +
					"that a list of WOOL Variables have been updated (e.g. during dialogue execution) " +
					"for a particular user.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful operation") })
	@RequestMapping(value="/notify-updated/{userId}", method= RequestMethod.POST, consumes={
			MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<?> notifyUpdated(
			HttpServletRequest request,
			HttpServletResponse response,
			@Parameter(description = "API Version to use, e.g. '1.0.0'")
				@PathVariable("version") String versionName,
			@Parameter(description = "The userId of the WOOL user")
				@PathVariable String userId,
			@Parameter(description = "List of WOOL Variables for which to check for updates.",
					required=true, content = @Content(array = @ArraySchema(schema = @Schema(implementation = WoolVariableParam.class))))
				@RequestBody List<WoolVariableParam> woolVariables) {
		String logInfo = "POST /notify-updated?userId=" + userId;
		logger.info(logInfo);
		return executeNotifyUpdated(userId, woolVariables);
	}

	/**
	 * This method performs the Dummy implementation for updating the given variables in the external
	 * service. As this is a dummy implementation and there is no real external service, the implementation
	 * is simply to return "OK", without doing anything.
	 *
	 *
	 * @param userId the {@code String} identifier of the user for whom variable updates are available.
	 * @param params the {@code List} of {@link WoolVariableParam}s that were updated and may need to be
	 *               processed in the external service.
	 * @return a {@link ResponseEntity} to indicate whether the update was executed successfully.
	 */
	private ResponseEntity<?> executeNotifyUpdated(String userId, List<WoolVariableParam> params) {
		return new ResponseEntity<ResponseEntity<?>>(HttpStatus.OK);
	}
}