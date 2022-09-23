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

import com.fasterxml.jackson.core.type.TypeReference;
import eu.woolplatform.utils.AppComponents;
import eu.woolplatform.utils.exception.DatabaseException;
import eu.woolplatform.utils.exception.ParseException;
import eu.woolplatform.utils.io.FileUtils;
import eu.woolplatform.utils.json.JsonMapper;
import eu.woolplatform.web.service.Application;
import eu.woolplatform.web.service.QueryRunner;
import eu.woolplatform.web.service.exception.HttpFieldError;
import eu.woolplatform.web.service.exception.NotFoundException;
import eu.woolplatform.web.service.model.LoggedDialogue;
import eu.woolplatform.web.service.model.LoggedDialogueStoreIO;
import eu.woolplatform.web.service.execution.UserServiceManager;
import eu.woolplatform.web.service.execution.UserService;
import eu.woolplatform.web.service.exception.BadRequestException;
import eu.woolplatform.web.service.exception.HttpException;
import eu.woolplatform.wool.exception.WoolException;
import eu.woolplatform.wool.execution.ExecuteNodeResult;
import eu.woolplatform.wool.model.DialogueState;
import eu.woolplatform.wool.model.WoolLoggedInteraction;
import eu.woolplatform.wool.model.WoolMessageSource;
import eu.woolplatform.wool.model.protocol.DialogueMessage;
import eu.woolplatform.wool.model.protocol.DialogueMessageFactory;
import eu.woolplatform.wool.model.protocol.NullableResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for the /dialogue/... end-points of the WOOL Web Service
 *
 * @author Dennis Hofs (RRD)
 * @author Harm op den Akker
 */
@RestController
@SecurityRequirement(name = "X-Auth-Token")
@RequestMapping("/v{version}/dialogue")
@Tag(name = "Dialogues", description = "End-points for starting, and controlling the lifecycle of remotely executed dialogues")
public class DialogueController {
	@Autowired
	Application application;

	private final Logger logger = AppComponents.getLogger(getClass().getSimpleName());

	// ----- END-POINT: "start-dialogue"

	@Operation(summary = "Start the step-by-step execution of the dialogue identified by the given parameters",
			description = "A client application that wants to start executing a dialogue should use this end-point " +
					"to do so. The dialogueName (which is the dialogue's filename without it's .wool extension and " +
					"language are mandatory parameters. The 'woolUserId' is an optional parameter that may be used " +
					"if the currently authorized user is an admin and wants to execute a dialogue on behalf of another " +
					"user. If the authenticated user is running a dialogue 'for himself' this should be left empty.")
	@RequestMapping(value="/start-dialogue", method= RequestMethod.POST)
	public DialogueMessage startDialogue(
			HttpServletRequest request,
			HttpServletResponse response,
			@PathVariable("version")
			String versionName,
			@RequestParam(value="dialogueName")
			String dialogueName,
			@RequestParam(value="language")
			String language,
			@RequestParam(value="timeZone")
			String timeZone,
			@RequestParam(value="woolUserId", required=false, defaultValue="")
			String woolUserId) throws Exception {

		// Construct a minimal String for logging purposes
		String logInfo = "POST /start-dialogue?dialogueName= " + dialogueName + "&language=" + language;
		if(!woolUserId.equals("")) logInfo += "&userId="+woolUserId;
		if(!timeZone.equals("")) logInfo += "&timeZone=" + timeZone;
		logger.info(logInfo);

		if(woolUserId.equals("")) {
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
	 * Processes a call to the /start-dialogue/ end-point.
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
		DateTime time = parseTime(timeZone);
		logger.info("The date/time of the client is: "+time.toString());
		UserService userService = application.getServiceManager()
				.getActiveUserService(woolUserId);
		userService.setTimeZone(timeZone);
		ExecuteNodeResult node;
		try {
			node = userService.startDialogue(dialogueName, null, language, time);
			return DialogueMessageFactory.generateDialogueMessage(node);
		} catch (WoolException e) {
			throw createHttpException(e);
		}
	}

	// ----- END-POINT: "progress-dialogue"

	/**
	 * End point that returns the next statement by the agent and its
	 * corresponding replies (based on the reply selected for the previous
	 * statement). The request body may contain a JSON object with variables
	 * from input segments.
	 *
	 * @param replyId the id of the reply that was selected for the previous
	 * statement
	 * @return a dialogue message or null
	 */
	@RequestMapping(value="/progress-dialogue", method=RequestMethod.POST)
	public NullableResponse<DialogueMessage> progressDialogue(
			HttpServletRequest request,
			HttpServletResponse response,
			@PathVariable("version")
			String versionName,
			@RequestParam(value="loggedDialogueId")
			String loggedDialogueId,
			@RequestParam(value="loggedInteractionIndex")
			int loggedInteractionIndex,
			@RequestParam(value="replyId")
			int replyId,
			@RequestParam(value="woolUserId", required=false, defaultValue="")
			String woolUserId) throws Exception {
		if(woolUserId.equals("")) {
			logger.info("POST /progress-dialogue?replyId=" + replyId);
			return QueryRunner.runQuery(
				(version, user) -> doProgressDialogue(user, request,
						loggedDialogueId, loggedInteractionIndex, replyId),
				versionName, request, response, woolUserId, application);
		} else {
			logger.info("POST /progress-dialogue?replyId=" + replyId+"&woolUserId="+woolUserId);
			return QueryRunner.runQuery(
				(version, user) -> doProgressDialogue(woolUserId, request,
					loggedDialogueId, loggedInteractionIndex, replyId),
				versionName, request, response, woolUserId, application);
		}
	}

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
			DateTime time = parseTime(userService.getTimeZone());
			DialogueState state = userService.getDialogueState(loggedDialogueId,
					loggedInteractionIndex);
			if (!variables.isEmpty())
				userService.storeReplyInput(state, variables, time);
			ExecuteNodeResult nextNode = userService.progressDialogue(state,
					replyId, time);
			if (nextNode == null)
				return new NullableResponse<>(null);
			DialogueMessage reply = DialogueMessageFactory.generateDialogueMessage(
					nextNode);
			return new NullableResponse<>(reply);
		} catch (WoolException e) {
			throw createHttpException(e);
		}
	}

	// ----- END-POINT: "back-dialogue"

	@RequestMapping(value="/back-dialogue", method=RequestMethod.POST)
	public DialogueMessage backDialogue(
			HttpServletRequest request,
			HttpServletResponse response,
			@PathVariable("version")
			String versionName,
			@RequestParam(value="loggedDialogueId")
			String loggedDialogueId,
			@RequestParam(value="loggedInteractionIndex")
			int loggedInteractionIndex,
			@RequestParam(value="woolUserId", required=false, defaultValue = "")
			String woolUserId) throws Exception {
		if(woolUserId.equals("")) {
			logger.info("POST /back-dialogue");
			return QueryRunner.runQuery(
				(version, user) -> doBackDialogue(user, loggedDialogueId,
						loggedInteractionIndex),
				versionName, request, response, woolUserId, application);
		} else {
			logger.info("POST /back-dialogue?woolUserId="+woolUserId);
			return QueryRunner.runQuery(
				(version, user) -> doBackDialogue(woolUserId, loggedDialogueId,
						loggedInteractionIndex),
				versionName, request, response, woolUserId, application);
		}
	}

	private DialogueMessage doBackDialogue(String woolUserId,
			String loggedDialogueId, int loggedInteractionIndex) throws HttpException,
			DatabaseException, IOException {

		try {
			UserService userService = application.getServiceManager()
					.getActiveUserService(woolUserId);
			DateTime time = parseTime(userService.getTimeZone());
			DialogueState state = userService.getDialogueState(loggedDialogueId,
					loggedInteractionIndex);
			ExecuteNodeResult prevNode = userService.backDialogue(state, time);
			return DialogueMessageFactory.generateDialogueMessage(prevNode);
		} catch (WoolException e) {
			throw createHttpException(e);
		}
	}

	// ----- END-POINT: "current-dialogue"

	@RequestMapping(value="/current-dialogue", method=RequestMethod.GET)
	public NullableResponse<DialogueMessage> getCurrentDialogue(
			HttpServletRequest request,
			HttpServletResponse response,
			@PathVariable("version")
			String versionName,
			@RequestParam(value="dialogueName")
			String dialogueName,
			@RequestParam(value="timeZone")
			String timeZone,
			@RequestParam(value="woolUserId", required=false, defaultValue="")
			String woolUserId) throws Exception {
		if(woolUserId.equals("")) {
			logger.info("Get /current-dialogue?dialogueName=" + dialogueName);
			return QueryRunner.runQuery(
				(version, user) -> doGetCurrentDialogue(user, dialogueName, timeZone),
				versionName, request, response, woolUserId, application);
		} else {
			logger.info("Get /current-dialogue?dialogueName=" + dialogueName+"&woolUserId="+woolUserId);
			return QueryRunner.runQuery(
				(version, user) -> doGetCurrentDialogue(woolUserId, dialogueName, timeZone),
				versionName, request, response, woolUserId, application);
		}
	}

	private NullableResponse<DialogueMessage> doGetCurrentDialogue(
			String woolUserId, String dialogueName, String timeZone)
			throws HttpException, DatabaseException, IOException {
		DateTime time = parseTime(timeZone);
		LoggedDialogue currDlg =
				LoggedDialogueStoreIO.findLatestOngoingDialogue(woolUserId,
				dialogueName);
		WoolLoggedInteraction lastInteraction = null;
		if (currDlg != null && !currDlg.getInteractionList().isEmpty()) {
			lastInteraction = currDlg.getInteractionList().get(
					currDlg.getInteractionList().size() - 1);
		}
		UserService userService = application.getServiceManager()
				.getActiveUserService(woolUserId);
		if (lastInteraction != null && lastInteraction.getMessageSource() ==
				WoolMessageSource.AGENT) {
			ExecuteNodeResult node;
			try {
				DialogueState state = userService.getDialogueState(currDlg,
						currDlg.getInteractionList().size() - 1);
				node = userService.executeCurrentNode(state, time);
			} catch (WoolException ex) {
				throw createHttpException(ex);
			}
			DialogueMessage result =
					DialogueMessageFactory.generateDialogueMessage(node);
			return new NullableResponse<>(result);
		} else {
			return new NullableResponse<>(null);
		}
	}

	// ----- END-POINT: "cancel-dialogue"

	@RequestMapping(value="/cancel-dialogue", method=RequestMethod.POST)
	public void cancelDialogue(
			HttpServletRequest request,
			HttpServletResponse response,
			@PathVariable("version")
			String versionName,
			@RequestParam(value="loggedDialogueId")
			String loggedDialogueId,
			@RequestParam(value="woolUserId", required=false, defaultValue="")
			String woolUserId) throws Exception {
		if(woolUserId.equals("")) {
			logger.info("POST /cancel-dialogue");
			QueryRunner.runQuery((version, user) -> doCancelDialogue(user, loggedDialogueId),
				versionName, request, response, woolUserId, application);
		} else {
			logger.info("POST /cancel-dialogue?woolUserId="+woolUserId);
			QueryRunner.runQuery((version, user) -> doCancelDialogue(woolUserId, loggedDialogueId),
					versionName, request, response, woolUserId, application);
		}
	}

	private Object doCancelDialogue(String woolUserId, String loggedDialogueId)
			throws DatabaseException, IOException {
		application.getServiceManager().getActiveUserService(woolUserId)
				.cancelDialogue(loggedDialogueId);
		return null;
	}

	public static DateTime parseTime(String timezone)
			throws BadRequestException {
		List<HttpFieldError> errors = new ArrayList<>();
		DateTime time = UserServiceManager.parseTimeParameters(timezone, errors);
		if (!errors.isEmpty())
			throw BadRequestException.withInvalidInput(errors);
		return time;
	}

	public static HttpException createHttpException(WoolException exception) {
		switch (exception.getType()) {
			case AGENT_NOT_FOUND:
			case DIALOGUE_NOT_FOUND:
			case NODE_NOT_FOUND:
			case REPLY_NOT_FOUND:
			case NO_ACTIVE_DIALOGUE:
				return new NotFoundException(exception.getMessage());
			default:
				throw new RuntimeException("Unexpected WoolAgentException: " +
						exception.getMessage(), exception);
		}
	}
}
