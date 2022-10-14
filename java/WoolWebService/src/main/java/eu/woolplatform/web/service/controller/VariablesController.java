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
import eu.woolplatform.web.service.Application;
import eu.woolplatform.web.service.ProtocolVersion;
import eu.woolplatform.web.service.QueryRunner;
import eu.woolplatform.web.service.exception.BadRequestException;
import eu.woolplatform.web.service.exception.ErrorCode;
import eu.woolplatform.web.service.exception.HttpError;
import eu.woolplatform.web.service.exception.HttpFieldError;
import eu.woolplatform.web.service.execution.UserService;
import eu.woolplatform.wool.execution.WoolVariable;
import eu.woolplatform.wool.execution.WoolVariableStore;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

@RestController
@SecurityRequirement(name = "X-Auth-Token")
@Tag(name = "3. Variables", description = "End-points for retrieving or setting WOOL Variables")
@RequestMapping("/variables")
public class VariablesController {

	@Autowired
	Application application;

	private final Logger logger = AppComponents.getLogger(getClass().getSimpleName());

	// ----------------------------------------------------------- //
	// ---------- END-POINT: "/variables/get-variables" ---------- //
	// ----------------------------------------------------------- //

	@Operation(
		summary = "Retrieve all- or a subset of WOOL variables for a given user",
		description = "Use this end-point to get the latest known WOOL Variables values for a given user " +
				"(either the currently logged in user, or the one provided through the end-point). Either provide " +
				"a list of variable names for which to retrieve its values, or leave this empty to retrieve all known " +
				"WOOL Variable data.")
	@RequestMapping(value="/get-variables", method=RequestMethod.GET)
	public Map<String,Object> getVariables(
			HttpServletRequest request,
			HttpServletResponse response,

			@Parameter(description = "A space-separated list of WOOL variable names, or leave empty to retrieve all known variables")
			@RequestParam(value="variableNames", required=false)
			String variableNames,

			@Parameter(description = "The user for which to request wool variable info (leave empty if executing for the currently authenticated user)")
			@RequestParam(value="woolUserId", required=false)
			String woolUserId) throws Exception {

		// Versioning is removed for the time being, assume the latest version
		String versionName = ProtocolVersion.getLatestVersion().versionName();

		// Log this call to the service log
		String logInfo = "GET /variables/get-variables?names=" + variableNames;
		if(!(woolUserId == null) && (!woolUserId.equals(""))) logInfo += "&woolUserId="+woolUserId;
		logger.info(logInfo);

		// Make sure the passed on String is not null
		String variableNameList = Objects.requireNonNullElse(variableNames, "");

		if(woolUserId == null || woolUserId.equals("")) {
			return QueryRunner.runQuery(
				(version, user) -> doGetVariables(user, variableNameList),
				versionName, request, response, woolUserId, application);
		} else {
			return QueryRunner.runQuery(
				(version, user) -> doGetVariables(woolUserId, variableNameList),
				versionName, request, response, woolUserId, application);
		}
	}

	/**
	 * For the given {@code woolUserId} returns a mapping from names to value of WOOL Variables for
	 * those variables provided in the given {@code variableNames} string. The {@code variableNames}
	 * string should be a 'space-separated' list of WOOL Variable names (e.g. "variable1
	 * variable_two variable-three").
	 *
	 * @param woolUserId the WOOL user for which to retrieve variable data.
	 * @param variableNames a space-separated list of variable names, or the empty string
	 * @return a mapping of variable names to variable values
	 * @throws Exception in case of an error retrieving variable data from file.
	 * TODO: Return a list of WoolVariable objects
	 */
	private Map<String,Object> doGetVariables(String woolUserId, String variableNames)
			throws Exception {
		UserService userService = application.getServiceManager()
				.getActiveUserService(woolUserId);

		WoolVariableStore woolVariableStore = userService.getVariableStore();

		variableNames = variableNames.trim();

		List<String> nameList;
		if (variableNames.length() == 0) {
			nameList = woolVariableStore.getSortedWoolVariableNames();
		} else {
			List<String> invalidNames = new ArrayList<>();
			String[] nameArray = variableNames.split("\\s+");
			for (String name : nameArray) {
				if (!name.matches("[A-Za-z]\\w*"))
					invalidNames.add(name);
			}
			if (!invalidNames.isEmpty()) {
				HttpFieldError error = new HttpFieldError("names",
						"Invalid variable names: " +
								String.join(", ", invalidNames));
				throw BadRequestException.withInvalidInput(error);
			}
			nameList = Arrays.asList(nameArray);
		}

		Map<String,Object> result = new LinkedHashMap<>();
		for (String name : nameList) {
			result.put(name, woolVariableStore.getWoolVariable(name).getValue());
		}
		return result;
	}

	// ---------------------------------------------------------- //
	// ---------- END-POINT: "/variables/set-variable" ---------- //
	// ---------------------------------------------------------- //

	@Operation(
		summary = "Set the value of a single WOOL Variable for a given user",
		description = "Use this end-point to get set a WOOL Variable to a specific value (or to remove the " +
				"stored value by setting it to the empty string.")
	@RequestMapping(value="/set-variable", method=RequestMethod.POST)
	public void setVariable(
		HttpServletRequest request,
		HttpServletResponse response,

		@Parameter(description = "The name of the WOOL Variable to set")
		@RequestParam(value="name")
		String name,

		@Parameter(description = "The value for the WOOL Variable (or leave empty to erase the WOOL variable)")
		@RequestParam(value="value", required=false)
		String value,

		@Parameter(description = "The user for which to set the wool variable (leave empty if setting for the currently authenticated user)")
		@RequestParam(value="woolUserId",required=false,defaultValue="")
		String woolUserId
	) throws Exception {

		// Versioning is removed for the time being, assume the latest version
		String versionName = ProtocolVersion.getLatestVersion().versionName();

		// Log this call to the service log
		String logInfo = "POST /variables/set-variable?name=" + name;
		if(!(value == null) && (!value.equals(""))) logInfo += "&value="+value;
		if(!(woolUserId == null) && (!woolUserId.equals(""))) logInfo += "&woolUserId="+woolUserId;
		logger.info(logInfo);

		if(woolUserId == null || woolUserId.equals("")) {
			QueryRunner.runQuery((version, user) ->
				doSetVariable(user, name, value),
				versionName, request, response, woolUserId, application);
		} else {
			QueryRunner.runQuery((version, user) ->
				doSetVariable(woolUserId, name, value),
				versionName, request, response, woolUserId, application);
		}
	}

	/**
	 * Sets a WOOL Variable defined by the given {@code name} to the given {@code value} for the specified
	 * {@code woolUserid} in the given {@code timeZone}.
	 * @param woolUserId the {@link String} identifier of the user for whom to set the variable.
	 * @param name the name of the WOOL Variable
	 * @param value the value for the WOOL Variable (or {@code null} in case the variable should be reset).
	 * @return {@code null}
	 * @throws Exception in case of an invalid variable name, invalid timezone, or an error accessing the variable store.
	 */
	private Object doSetVariable(String woolUserId, String name, String value) throws Exception {
		List<HttpFieldError> errors = new ArrayList<>();

		if (!name.matches("[A-Za-z]\\w*")) {
			errors.add(new HttpFieldError("name",
					"Invalid variable name: " + name));
			throw BadRequestException.withInvalidInput(errors);
		}

		UserService userService = application.getServiceManager()
				.getActiveUserService(woolUserId);

		ZonedDateTime updatedTime = ZonedDateTime.now(userService.getWoolUser().getTimeZone());

		userService.getVariableStore().setValue(name, value, true, updatedTime);
		return null;
	}

	// ----------------------------------------------------------- //
	// ---------- END-POINT: "/variables/set-variables" ---------- //
	// ----------------------------------------------------------- //

	@Operation(
		summary = "Set the value of one or multiple WOOL Variable for a given user",
		description = "Use this end-point to get set one or many WOOL Variables to their given values (or to remove the " +
				"stored value by setting it to the empty string.")
	@RequestMapping(value="/set-variables", method=RequestMethod.POST)
	public void setVariables(
			HttpServletRequest request,
			HttpServletResponse response,

			@Parameter(description = "The user for which to set the wool variables (leave empty if setting for the currently authenticated user)")
			@RequestParam(value="woolUserId",required=false)
			String woolUserId,

			@Parameter(description = "The current time zone of the WOOL user (as IANA, e.g. 'Europe/Lisbon')")
			@RequestParam(value="timeZone")
			String timeZone,

			@Parameter(description = "A JSON map of WOOL Variable names to values")
			@RequestBody
			Map<String,Object> woolVariables) throws Exception {

		// Versioning is removed for the time being, assume the latest version
		String versionName = ProtocolVersion.getLatestVersion().versionName();

		// Log this call to the service log
		String logInfo = "POST /variables/set-variables";
		if(!(woolUserId == null) && (!woolUserId.equals(""))) logInfo += "?woolUserId="+woolUserId;
		logger.info(logInfo);

		if(woolUserId == null || woolUserId.equals("")) {
			QueryRunner.runQuery((version, user) -> doSetVariables(user, woolVariables, timeZone),
				versionName, request, response, woolUserId, application);
		} else {
			QueryRunner.runQuery((version, user) -> doSetVariables(woolUserId, woolVariables, timeZone),
				versionName, request, response, woolUserId, application);
		}
	}

	/**
	 * Sets the WOOL Variables in the given {@code woolVariables} map for the given {@code woolUserId}.
	 * @param woolUserId the {@link String} identifier of the user for whom to set the variables.
	 * @param woolVariables a mapping of WOOL Variable names to value.
	 * @return {@code null}
	 * @throws Exception in case of an invalid variable name, or an error writing variables to the database.
	 */
	private Object doSetVariables(String woolUserId, Map<String,Object> woolVariables,
								  String timeZoneString) throws Exception {

		List<String> invalidNames = new ArrayList<>();
		for (String name : woolVariables.keySet()) {
			if (!name.matches("[A-Za-z]\\w*"))
				invalidNames.add(name);
		}

		if (!invalidNames.isEmpty()) {
			HttpError error = new HttpError(ErrorCode.INVALID_INPUT,
					"Invalid variable names: " +
					String.join(", ", invalidNames));
			throw new BadRequestException(error);
		}

		// Update the WOOL User's time zone with the latest given value
		ZoneId timeZoneId = ControllerFunctions.parseTimeZone(timeZoneString);
		UserService userService = application.getServiceManager().getActiveUserService(woolUserId);
		userService.getWoolUser().setTimeZone(timeZoneId);

		WoolVariableStore woolVariableStore = userService.getVariableStore();
		for(Map.Entry<String, Object> entry : woolVariables.entrySet()) {
			woolVariableStore.setValue(
					entry.getKey(),
					entry.getValue(),
					true,
					ZonedDateTime.now(userService.getWoolUser().getTimeZone()));
		}

		return null;
	}
}
