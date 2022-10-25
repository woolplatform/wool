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

import com.fasterxml.jackson.core.type.TypeReference;
import eu.woolplatform.utils.AppComponents;
import eu.woolplatform.utils.datetime.DateTimeUtils;
import eu.woolplatform.utils.exception.DatabaseException;
import eu.woolplatform.utils.exception.ParseException;
import eu.woolplatform.utils.io.FileUtils;
import eu.woolplatform.utils.json.JsonMapper;
import eu.woolplatform.web.service.Application;
import eu.woolplatform.web.service.ProtocolVersion;
import eu.woolplatform.web.service.QueryRunner;
import eu.woolplatform.web.service.controller.schema.OngoingDialoguePayload;
import eu.woolplatform.web.service.exception.BadRequestException;
import eu.woolplatform.web.service.exception.HttpException;
import eu.woolplatform.web.service.execution.UserService;
import eu.woolplatform.web.service.storage.LoggedDialogue;
import eu.woolplatform.web.service.storage.LoggedDialogueStoreIO;
import eu.woolplatform.wool.exception.WoolException;
import eu.woolplatform.wool.execution.ExecuteNodeResult;
import eu.woolplatform.wool.model.DialogueState;
import eu.woolplatform.wool.model.WoolLoggedInteraction;
import eu.woolplatform.wool.model.WoolMessageSource;
import eu.woolplatform.wool.model.protocol.DialogueMessage;
import eu.woolplatform.wool.model.protocol.DialogueMessageFactory;
import eu.woolplatform.wool.model.protocol.NullableResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Controller for the /dialogue/... end-points of the WOOL Web Service.
 *
 * @author Dennis Hofs (RRD)
 * @author Harm op den Akker
 */
@RestController
@SecurityRequirement(name = "X-Auth-Token")
@RequestMapping("/v{version}/dialogue")
@Tag(name = "2. Dialogue", description = "End-points for starting and controlling the lifecycle of remotely executed dialogues")
public class DialogueController {

	@Autowired
	Application application;

	private final Logger logger = AppComponents.getLogger(getClass().getSimpleName());

	// -------------------------------------------------- //
	// ---------- END-POINT: "/dialogue/start" ---------- //
	// -------------------------------------------------- //

	@Operation(
		summary = "Start the step-by-step execution of the dialogue identified by the given parameters",
		description = "A client application that wants to start executing a dialogue should use this end-point " +
					"to do so. The dialogueName (which is the dialogue's filename without it's .wool extension and " +
					"language are mandatory parameters. The 'woolUserId' is an optional parameter that may be used " +
					"if the currently authorized user is an admin and wants to execute a dialogue on behalf of another " +
					"user. If the authenticated user is running a dialogue 'for himself' this should be left empty.")
	@RequestMapping(value="/start", method= RequestMethod.POST)
	public DialogueMessage startDialogue(
		HttpServletRequest request,
		HttpServletResponse response,

		@Parameter(hidden = true)
		@PathVariable(value = "version")
		String versionName,

		@Parameter(description = "Name of the WOOL Dialogue to start (excluding .wool)")
		@RequestParam(value="dialogueName")
		String dialogueName,

		@Parameter(description = "Language code of the language in which to start the dialogue (e.g. 'en')")
		@RequestParam(value="language")
		String language,

		@Parameter(description = "The current time zone of the WOOL user (as IANA, e.g. 'Europe/Lisbon')")
		@RequestParam(value="timeZone")
		String timeZone,

		@Parameter(description = "The user for which to execute the dialogue (leave empty if executing for the currently authenticated user)")
		@RequestParam(value="woolUserId", required=false)
		String woolUserId
	) throws Exception {

		// If no versionName is provided, or versionName is empty, assume the latest version
		if (versionName == null || versionName.equals("")) {
			versionName = ProtocolVersion.getLatestVersion().versionName();
		}

		// Log this call to the service log
		String logInfo = "POST /v" + versionName + "/dialogue/start?dialogueName=" + dialogueName + "&language=" + language;
		if(!(woolUserId == null) && (!woolUserId.equals(""))) logInfo += "&woolUserId="+woolUserId;
		if(!timeZone.equals("")) logInfo += "&timeZone=" + timeZone;
		logger.info(logInfo);

		if(woolUserId == null || woolUserId.equals("")) {
			return QueryRunner.runQuery(
					(version, user) -> doStartDialogue(user, dialogueName,
							language, timeZone),
					versionName, request, response, woolUserId, application);
		} else {
			return QueryRunner.runQuery(
					(version, user) -> doStartDialogue(woolUserId, dialogueName,
							language, timeZone),
					versionName, request, response, woolUserId, application);
		}
	}

	/**
	 * Processes a call to the /dialogue/start end-point.
	 * @param woolUserId the {@link String} identifier of the user for whom to start a dialogue.
	 * @param dialogueName the name of the dialogue to start executing
	 * @param language the language in which to start the dialogue
	 * @param timeZone the timeZone of the client as one of {@code TimeZone.getAvailableIDs()} (IANA Codes)
	 * @return the {@link DialogueMessage} that represents the start node of the dialogue.
	 * @throws HttpException in case of an error in the dialogue execution.
	 * @throws DatabaseException in case of an error in retrieving the current active user.
	 * @throws IOException in case of any network error.
	 */
	private DialogueMessage doStartDialogue(
			String woolUserId, String dialogueName, String language,
			String timeZone) throws HttpException, IOException, DatabaseException {
		ZoneId timeZoneId = ControllerFunctions.parseTimeZone(timeZone);
		UserService userService = application.getServiceManager()
				.getActiveUserService(woolUserId);
		userService.getWoolUser().setTimeZone(timeZoneId);
		ExecuteNodeResult node;
		try {
			node = userService.startDialogue(dialogueName, null, language);
			return DialogueMessageFactory.generateDialogueMessage(node);
		} catch (WoolException e) {
			throw ControllerFunctions.createHttpException(e);
		}
	}

	// ----------------------------------------------------- //
	// ---------- END-POINT: "/dialogue/progress" ---------- //
	// ----------------------------------------------------- //

	/**
	 * End point that returns the next statement by the agent and its
	 * corresponding replies (based on the reply selected for the previous
	 * statement). The request body may contain a JSON object with variables
	 * from input segments.
	 *
	 * @param request the HTTPRequest object (to retrieve authentication headers and optional body parameters).
	 * @param response the HTTP response (to add header WWW-Authenticate in
	 *                  case of 401 Unauthorized).
	 * @param loggedDialogueId The identifier of the (in-progress) dialogue to progress
	 * @param loggedInteractionIndex The interaction index is the step in the dialogue execution from which to progress the dialogue
	 * @param replyId the id of the reply that was selected for the previous statement.
	 * @param woolUserId The user for which to execute the dialogue (leave empty if executing for the currently authenticated user)
	 * @return a {@link NullableResponse} containing either a {@link DialogueMessage} or {@code null}.
	 */
	@Operation(
			summary = "Progresses a given dialogue from a given state with a given reply id",
			description = "The client application that wants to progress a previously started dialogue should use this end-point " +
					"to do so. The loggedDialogueId identifies the ongoing dialogue (and will have been provided by a call to start-dialogue) " +
					"and the loggedInteractionIndex identifies the current step in the dialogue execution (also provided previously). The replyId " +
					"depicts the reply that the user has chosen to progress the dialogue. The 'woolUserId' is an optional parameter that may be " +
					"used if the currently authorized user is an admin and wants to execute a dialogue on behalf of another " +
					"user. If the authenticated user is running a dialogue 'for himself' this should be left empty.")
	@RequestMapping(value="/progress", method=RequestMethod.POST)
	public NullableResponse<DialogueMessage> progressDialogue(
			HttpServletRequest request,
			HttpServletResponse response,

			@Parameter(hidden = true)
			@PathVariable(value = "version")
			String versionName,

			@Parameter(description = "The identifier of the (in-progress) dialogue to progress")
			@RequestParam(value="loggedDialogueId")
			String loggedDialogueId,

			@Parameter(description = "The interaction index is the step in the dialogue execution from which to progress the dialogue")
			@RequestParam(value="loggedInteractionIndex")
			int loggedInteractionIndex,

			@Parameter(description = "The identifier of the reply that the user has chose to progress the dialogue")
			@RequestParam(value="replyId")
			int replyId,

			@Parameter(description = "The user for which to execute the dialogue (leave empty if executing for the currently authenticated user)")
			@RequestParam(value="woolUserId", required=false)
			String woolUserId
	) throws Exception {

		// If no versionName is provided, or versionName is empty, assume the latest version
		if (versionName == null || versionName.equals("")) {
			versionName = ProtocolVersion.getLatestVersion().versionName();
		}

		// Log this call to the service log
		String logInfo = "POST /v" + versionName + "/dialogue/progress?replyId=" + replyId;
		if(!(woolUserId == null) && (!woolUserId.equals(""))) logInfo += "&woolUserId="+woolUserId;
		logger.info(logInfo);

		if(woolUserId == null || woolUserId.equals("")) {
			return QueryRunner.runQuery(
				(version, user) -> doProgressDialogue(user, request,
						loggedDialogueId, loggedInteractionIndex, replyId),
				versionName, request, response, woolUserId, application);
		} else {
			return QueryRunner.runQuery(
				(version, user) -> doProgressDialogue(woolUserId, request,
					loggedDialogueId, loggedInteractionIndex, replyId),
				versionName, request, response, woolUserId, application);
		}
	}

	/**
	 * Processes a call to the /dialogue/progress end-point.
	 * @param woolUserId the user for which to execute the dialogue (leave empty or {@code null} if executing
	 *                      for the currently authenticated user).
	 * @param request the HTTPRequest object (to retrieve authentication headers and optional body parameters).
	 * @param loggedDialogueId the identifier of the (in-progress) dialogue to progress.
	 * @param loggedInteractionIndex the interaction index is the step in the dialogue execution from which to progress the dialogue.
	 * @param replyId the identifier of the reply that the user has chosen to progress the dialogue.
	 * @return a {@link NullableResponse} containing either a {@link DialogueMessage} or {@code null}.
	 * @throws HttpException in case of a network error.
	 * @throws DatabaseException in case of an error retrieving the current dialogue state.
	 * @throws IOException in case of a network error.
	 */
	private NullableResponse<DialogueMessage> doProgressDialogue(String woolUserId,
			HttpServletRequest request, String loggedDialogueId,
			int loggedInteractionIndex, int replyId) throws HttpException, DatabaseException,
			IOException {

		String body;
		try (InputStream input = request.getInputStream()) {
			body = FileUtils.readFileString(input);
		}
		Map<String,?> variables = new LinkedHashMap<>();
		if (body.trim().length() > 0) {
			try {
				variables = JsonMapper.parse(body,
						new TypeReference<>() {
						});
			} catch (ParseException ex) {
				throw new BadRequestException(
						"Request body is not a JSON object: " +
								ex.getMessage());
			}
		}
		try {
			UserService userService = application.getServiceManager()
					.getActiveUserService(woolUserId);

			ZonedDateTime eventTime = DateTimeUtils.nowMs(userService.getWoolUser().getTimeZone());

			DialogueState state = userService.getDialogueState(loggedDialogueId,
					loggedInteractionIndex);
			if (!variables.isEmpty())
				userService.storeReplyInput(state, variables,eventTime);
			ExecuteNodeResult nextNode = userService.progressDialogue(state, replyId);
			if (nextNode == null)
				return new NullableResponse<>(null);
			DialogueMessage reply = DialogueMessageFactory.generateDialogueMessage(
					nextNode);
			return new NullableResponse<>(reply);
		} catch (WoolException e) {
			throw ControllerFunctions.createHttpException(e);
		}
	}

	// ----------------------------------------------------- //
	// ---------- END-POINT: "/dialogue/continue" ---------- //
	// ----------------------------------------------------- //

	@Operation(
			summary = "Continue the latest ongoing dialogue with a given name",
			description = "Pick up the conversation by providing a dialogue name. If there is an ongoing dialogue " +
					"with the given name (that is not finished or cancelled), this method will return the next step " +
					"in that conversation. As with all methods that 'start' dialogue executions, a valid time zone " +
					"in which the user currently resided must be provided so that time sensitive information may be " +
					"processed correctly.")
	@RequestMapping(value="/continue", method=RequestMethod.POST)
	public NullableResponse<DialogueMessage> continueDialogue(
			HttpServletRequest request,
			HttpServletResponse response,

			@Parameter(hidden = true)
			@PathVariable(value = "version")
			String versionName,

			@Parameter(description = "Name of the WOOL Dialogue to continue (excluding .wool)")
			@RequestParam(value="dialogueName")
			String dialogueName,

			@Parameter(description = "The current time zone of the WOOL user (as IANA, e.g. 'Europe/Lisbon')")
			@RequestParam(value="timeZone")
			String timeZone,

			@Parameter(description = "The user for which to continue executing the dialogue (leave empty if executing for the currently authenticated user)")
			@RequestParam(value="woolUserId", required=false, defaultValue="")
			String woolUserId
	) throws Exception {

		// If no versionName is provided, or versionName is empty, assume the latest version
		if (versionName == null || versionName.equals("")) {
			versionName = ProtocolVersion.getLatestVersion().versionName();
		}

		// Log this call to the service log
		String logInfo = "POST /v" + versionName + "/dialogue/continue?dialogueName=" + dialogueName + "&timeZone=" + timeZone;
		if(!(woolUserId == null) && (!woolUserId.equals(""))) logInfo += "&woolUserId="+woolUserId;
		logger.info(logInfo);

		if(woolUserId == null || woolUserId.equals("")) {
			return QueryRunner.runQuery(
					(version, user) -> doContinueDialogue(user, dialogueName, timeZone),
					versionName, request, response, woolUserId, application);
		} else {
			return QueryRunner.runQuery(
					(version, user) -> doContinueDialogue(woolUserId, dialogueName, timeZone),
					versionName, request, response, woolUserId, application);
		}
	}

	/**
	 * Processes a call to the /dialogue/continue end-point.
	 * @param woolUserId the user for which to continue executing the dialogue (leave empty if executing for the currently authenticated user)").
	 * @param dialogueName name of the WOOL Dialogue to continue (excluding .wool).
	 * @param timeZone the current time zone of the WOOL user (as IANA, e.g. 'Europe/Lisbon').
	 * @return a {@link NullableResponse} object containing the {@link DialogueMessage} or {@code null}.
	 * @throws HttpException in case of a network error.
	 * @throws DatabaseException in case of an error retrieving the ongoing dialogue from the database.
	 * @throws IOException in case of a file io error.
	 */
	private NullableResponse<DialogueMessage> doContinueDialogue(
			String woolUserId, String dialogueName, String timeZone)
			throws HttpException, DatabaseException, IOException {

		// Update/set the WOOL User's timezone to the given value
		ZoneId timeZoneId = ControllerFunctions.parseTimeZone(timeZone);
		UserService userService = application.getServiceManager()
				.getActiveUserService(woolUserId);
		userService.getWoolUser().setTimeZone(timeZoneId);

		// Determine the event timestamp
		ZonedDateTime continueDialogueEventTime =
				DateTimeUtils.nowMs(userService.getWoolUser().getTimeZone());

		LoggedDialogue currDlg =
				LoggedDialogueStoreIO.findLatestOngoingDialogue(woolUserId,
						dialogueName);
		WoolLoggedInteraction lastInteraction = null;
		if (currDlg != null && !currDlg.getInteractionList().isEmpty()) {
			lastInteraction = currDlg.getInteractionList().get(
					currDlg.getInteractionList().size() - 1);
		}

		if (lastInteraction != null && lastInteraction.getMessageSource() ==
				WoolMessageSource.AGENT) {
			ExecuteNodeResult node;
			try {
				DialogueState state = userService.getDialogueState(currDlg,
						currDlg.getInteractionList().size() - 1);
				node = userService.executeCurrentNode(state,continueDialogueEventTime);
			} catch (WoolException ex) {
				throw ControllerFunctions.createHttpException(ex);
			}
			DialogueMessage result =
					DialogueMessageFactory.generateDialogueMessage(node);
			return new NullableResponse<>(result);
		} else {
			return new NullableResponse<>(null);
		}
	}

	// --------------------------------------------------- //
	// ---------- END-POINT: "/dialogue/cancel" ---------- //
	// --------------------------------------------------- //

	@Operation(
		summary = "Cancels a dialogue that is currently in progress, terminating its execution state",
		description = "If a client application detects that a user has navigated away, or has deliberately requested " +
				"to stop an ongoing dialogue through a user interface action, this end-point should be called so that the " +
				"dialogue's state can be updated, indicating that it is no longer ongoing.")
	@RequestMapping(value="/cancel", method=RequestMethod.POST)
	public void cancelDialogue(
			HttpServletRequest request,
			HttpServletResponse response,

			@Parameter(hidden = true)
			@PathVariable(value = "version")
			String versionName,

			@Parameter(description = "The identifier of the (in-progress) dialogue to progress")
			@RequestParam(value="loggedDialogueId")
			String loggedDialogueId,

			@Parameter(description = "The user for which to execute the dialogue (leave empty if executing for the currently authenticated user)")
			@RequestParam(value="woolUserId", required=false)
			String woolUserId) throws Exception {

		// If no versionName is provided, or versionName is empty, assume the latest version
		if (versionName == null || versionName.equals("")) {
			versionName = ProtocolVersion.getLatestVersion().versionName();
		}

		// Log this call to the service log
		String logInfo = "POST /v" + versionName + "/dialogue/cancel?loggedDialogueId=" + loggedDialogueId;
		if(!(woolUserId == null) && (!woolUserId.equals(""))) logInfo += "&woolUserId="+woolUserId;
		logger.info(logInfo);

		if(woolUserId == null || woolUserId.equals("")) {
			QueryRunner.runQuery((version, user) -> doCancelDialogue(user, loggedDialogueId),
					versionName, request, response, woolUserId, application);
		} else {
			QueryRunner.runQuery((version, user) -> doCancelDialogue(woolUserId, loggedDialogueId),
					versionName, request, response, woolUserId, application);
		}
	}

	/**
	 * Processes a call to the /dialogue/cancel end-point.
	 * @param woolUserId the user for which to execute the dialogue (leave empty or {@code null} if executing
	 * 	 *               for the currently authenticated user).
	 * @param loggedDialogueId the identifier of the (in-progress) dialogue to progress.
	 * @return {@code null}
	 * @throws DatabaseException in case of an error in retrieving the specified dialogue.
	 * @throws IOException in case of any network error.
	 */
	private Object doCancelDialogue(String woolUserId, String loggedDialogueId)
			throws DatabaseException, IOException {
		application.getServiceManager().getActiveUserService(woolUserId)
				.cancelDialogue(loggedDialogueId);
		return null;
	}

	// ------------------------------------------------- //
	// ---------- END-POINT: "/dialogue/back" ---------- //
	// ------------------------------------------------- //

	@Operation(
			summary = "Go back to the previous step in an ongoing dialogue",
			description = "Use this end-point by providing a loggedDialogueId (specifying an ongoing dialogue) and the " +
					"loggedInteractionIndex (identifying the current step in the dialogue). This end-point will return the " +
					"previous dialogue step (based on the loggedInteractionIndex) by providing that previous DialogueMessage. " +
					"<br/><br/><b>Caution: Using this method takes the dialogue back to the previous step as if there was a " +
					"regular reply option leading back to that step, but it will not undo any variable operations (i.e. setting " +
					"WOOL Variables) that may have occurred in the execution of the current step. This may lead to unexpected " +
					"results if the execution of the 'previous' dialogue step is affected by variables set in the 'current' step.</b>")
	@RequestMapping(value="/back", method=RequestMethod.POST)
	public DialogueMessage backDialogue(
		HttpServletRequest request,
		HttpServletResponse response,

		@Parameter(hidden = true)
		@PathVariable(value = "version")
		String versionName,

		@Parameter(description = "The identifier of the (in-progress) dialogue to take a step back in")
		@RequestParam(value="loggedDialogueId")
		String loggedDialogueId,

		@Parameter(description = "The interaction index is the step in the dialogue execution from which to take a step back in the dialogue")
		@RequestParam(value="loggedInteractionIndex")
		int loggedInteractionIndex,

		@Parameter(description = "The user for which to take a step back in the dialogue (leave empty if executing for the currently authenticated user)")
		@RequestParam(value="woolUserId", required=false)
		String woolUserId
	) throws Exception {

		// If no versionName is provided, or versionName is empty, assume the latest version
		if (versionName == null || versionName.equals("")) {
			versionName = ProtocolVersion.getLatestVersion().versionName();
		}

		// Log this call to the service log
		String logInfo = "POST /v" + versionName + "/dialogue/back?loggedDialogueId=" + loggedDialogueId + "&loggedInteractionIndex=" + loggedInteractionIndex;
		if(!(woolUserId == null) && (!woolUserId.equals(""))) logInfo += "&woolUserId="+woolUserId;
		logger.info(logInfo);

		if(woolUserId == null || woolUserId.equals("")) {
			return QueryRunner.runQuery(
				(version, user) -> doBackDialogue(user, loggedDialogueId,
						loggedInteractionIndex),
				versionName, request, response, woolUserId, application);
		} else {
			return QueryRunner.runQuery(
				(version, user) -> doBackDialogue(woolUserId, loggedDialogueId,
						loggedInteractionIndex),
				versionName, request, response, woolUserId, application);
		}
	}

	/**
	 * Processes a call to the /dialogue/back-dialogue end-point.
	 * @param woolUserId the user for which to take a step back in the dialogue (leave empty if executing for the currently authenticated user).
	 * @param loggedDialogueId the identifier of the (in-progress) dialogue to take a step back in.
	 * @param loggedInteractionIndex the interaction index is the step in the dialogue execution from which to take a step back in the dialogue.
	 * @return a {@link DialogueMessage} object depicting the previous step in the given ongoing dialogue.
	 * @throws HttpException in case of any network error.
	 * @throws DatabaseException in case of an error retrieving the ongoing dialogue from the database.
	 * @throws IOException in case of a file IO error.
	 */
	private DialogueMessage doBackDialogue(String woolUserId,
			String loggedDialogueId, int loggedInteractionIndex) throws HttpException,
			DatabaseException, IOException {

		try {
			UserService userService = application.getServiceManager()
					.getActiveUserService(woolUserId);

			// Determine the event time stamp
			ZonedDateTime backDialogueEventTime =
					DateTimeUtils.nowMs(userService.getWoolUser().getTimeZone());

			DialogueState state = userService.getDialogueState(loggedDialogueId,
					loggedInteractionIndex);
			ExecuteNodeResult prevNode = userService.backDialogue(state, backDialogueEventTime);
			return DialogueMessageFactory.generateDialogueMessage(prevNode);
		} catch (WoolException e) {
			throw ControllerFunctions.createHttpException(e);
		}
	}

	// -------------------------------------------------------- //
	// ---------- END-POINT: "/dialogue/get-ongoing" ---------- //
	// -------------------------------------------------------- //

	@Operation(
			summary = "Get information about the latest ongoing dialogue for a given user",
			description = "This end-point answers the question 'was there any unfinished business? and if so, how long ago?'. As a client " +
					"application, you may want to call this end-point at the start of a session to see if there was an ongoing dialogue left " +
					"over from a previous session. If so, you will get the dialogue-name and the time (in seconds) since the last 'engagement' with" +
					"that dialogue (the last time since either the user or the agent said something). If this wasn't too long ago, you may decide" +
					"to continue the conversation by passing the dialogue name to the /dialogues/continue-dialogue/ end-point. ")
	@RequestMapping(value="/get-ongoing", method=RequestMethod.GET)
	public NullableResponse<OngoingDialoguePayload> getOngoingDialogue(
			HttpServletRequest request,
			HttpServletResponse response,

			@Parameter(hidden = true)
			@PathVariable(value = "version")
			String versionName,

			@Parameter(description = "The user for which to retrieve the latest ongoing dialogue information (leave empty if retrieving for the currently authenticated user)")
			@RequestParam(value="woolUserId", required=false, defaultValue="")
			String woolUserId
	) throws Exception {

		// If no versionName is provided, or versionName is empty, assume the latest version
		if (versionName == null || versionName.equals("")) {
			versionName = ProtocolVersion.getLatestVersion().versionName();
		}

		// Log this call to the service log
		String logInfo = "GET /v" + versionName + "/dialogue/get-ongoing";
		if(!(woolUserId == null) && (!woolUserId.equals(""))) logInfo += "?woolUserId="+woolUserId;
		logger.info(logInfo);

		if(woolUserId == null || woolUserId.equals("")) {
			return QueryRunner.runQuery(
					(version, user) -> doGetOngoingDialogue(user),
					versionName, request, response, woolUserId, application);
		} else {
			return QueryRunner.runQuery(
					(version, user) -> doGetOngoingDialogue(woolUserId),
					versionName, request, response, woolUserId, application);
		}
	}

	/**
	 * Processes a call to the /dialogue/get-ongoing end-point.
	 * @param woolUserId the user for which to retrieve the latest ongoing dialogue information (leave empty
	 *                      if retrieving for the currently authenticated user).
	 * @return a {@link NullableResponse} containing either a {@link OngoingDialoguePayload} object or {@code null}.
	 * @throws DatabaseException in case of an error retrieving logged dialogues from the database.
	 * @throws IOException in case of any network error.
	 */
	private NullableResponse<OngoingDialoguePayload> doGetOngoingDialogue(String woolUserId)
			throws DatabaseException, IOException {

		LoggedDialogue latestOngoingDialogue =
				LoggedDialogueStoreIO.findLatestOngoingDialogue(woolUserId);

		if(latestOngoingDialogue != null) {
			String dialogueName = latestOngoingDialogue.getDialogueName();
			long latestInteractionTimestamp = latestOngoingDialogue.getLatestInteractionTimestamp();
			long secondsSinceLastEngagement = (long) Math.floor((System.currentTimeMillis() - latestInteractionTimestamp) / 1000.0);
			OngoingDialoguePayload ongoingDialoguePayload = new OngoingDialoguePayload(dialogueName, secondsSinceLastEngagement);
			return new NullableResponse<>(ongoingDialoguePayload);
		} else {
			return new NullableResponse<>(null);
		}
	}

}
