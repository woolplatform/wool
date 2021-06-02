package eu.woolplatform.webservice.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import eu.woolplatform.utils.AppComponents;
import eu.woolplatform.utils.exception.DatabaseException;
import eu.woolplatform.utils.exception.ParseException;
import eu.woolplatform.utils.io.FileUtils;
import eu.woolplatform.utils.json.JsonMapper;
import eu.woolplatform.webservice.Application;
import eu.woolplatform.webservice.QueryRunner;
import eu.woolplatform.webservice.dialogue.ServiceManager;
import eu.woolplatform.webservice.dialogue.UserService;
import eu.woolplatform.webservice.exception.BadRequestException;
import eu.woolplatform.webservice.exception.HttpException;
import eu.woolplatform.webservice.exception.HttpFieldError;
import eu.woolplatform.webservice.exception.NotFoundException;
import eu.woolplatform.webservice.model.LoggedDialogue;
import eu.woolplatform.webservice.model.LoggedDialogueStoreIO;
import eu.woolplatform.wool.exception.WoolException;
import eu.woolplatform.wool.execution.ExecuteNodeResult;
import eu.woolplatform.wool.model.DialogueState;
import eu.woolplatform.wool.model.WoolLoggedInteraction;
import eu.woolplatform.wool.model.WoolMessageSource;
import eu.woolplatform.wool.model.protocol.DialogueMessage;
import eu.woolplatform.wool.model.protocol.DialogueMessageFactory;
import eu.woolplatform.wool.model.protocol.NullableResponse;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v{version}/dialogue")
public class DialogueController {
	@Autowired
	Application application;

	private Logger logger = AppComponents.getLogger(getClass().getSimpleName());

	@RequestMapping(value="/start-dialogue", method= RequestMethod.POST)
	public DialogueMessage startDialogue(
			HttpServletRequest request,
			HttpServletResponse response,
			@PathVariable("version")
			@ApiIgnore
			String versionName,
			@RequestParam(value="dialogueName")
			String dialogueName,
			@RequestParam(value="language")
			String language,
			@RequestParam(value="time", required=false, defaultValue="")
			String time,
			@RequestParam(value="timezone", required=false, defaultValue="")
			String timezone) throws HttpException, Exception {
		logger.info("Post /start-dialogue?dialogueName=" + dialogueName +
				"&language=" + language);
		return QueryRunner.runQuery(
				(version, user) -> doStartDialogue(user, dialogueName,
						language, time, timezone),
				versionName, request, response);
	}

	private DialogueMessage doStartDialogue(
			String userId, String dialogueName, String language, String timeStr,
			String timezone) throws HttpException, DatabaseException,
			IOException {
		DateTime time = parseTime(timeStr, timezone);
		UserService userService = application.getServiceManager()
				.getActiveUserService(userId);
		ExecuteNodeResult node;
		try {
			node = userService.startDialogue(dialogueName, null, language,
					time);
			return DialogueMessageFactory.generateDialogueMessage(node);
		} catch (WoolException e) {
			throw createHttpException(e);
		}
	}

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
			@ApiIgnore
			String versionName,
			@RequestParam(value="loggedDialogueId")
			String loggedDialogueId,
			@RequestParam(value="loggedInteractionIndex")
			int loggedInteractionIndex,
			@RequestParam(value="replyId")
			int replyId,
			@RequestParam(value="time", required=false, defaultValue="")
			String time,
			@RequestParam(value="timezone", required=false, defaultValue="")
			String timezone) throws HttpException, Exception {
		logger.info("POST /progress-dialogue?replyId=" + replyId);
		return QueryRunner.runQuery(
				(version, user) -> doProgressDialogue(user, request,
						loggedDialogueId, loggedInteractionIndex, replyId,
						time, timezone),
				versionName, request, response);
	}

	private NullableResponse<DialogueMessage> doProgressDialogue(String userId,
			HttpServletRequest request, String loggedDialogueId,
			int loggedInteractionIndex, int replyId, String timeStr,
			String timezone) throws HttpException, DatabaseException,
			IOException {
		DateTime time = parseTime(timeStr, timezone);
		String body;
		try (InputStream input = request.getInputStream()) {
			body = FileUtils.readFileString(input);
		}
		Map<String,?> variables = new LinkedHashMap<>();
		if (body.trim().length() > 0) {
			try {
				variables = JsonMapper.parse(body,
						new TypeReference<Map<String, ?>>() {});
			} catch (ParseException ex) {
				throw new BadRequestException(
						"Request body is not a JSON object: " +
								ex.getMessage());
			}
		}
		try {
			UserService userService = application.getServiceManager()
					.getActiveUserService(userId);
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

	@RequestMapping(value="/back-dialogue", method=RequestMethod.POST)
	public DialogueMessage backDialogue(
			HttpServletRequest request,
			HttpServletResponse response,
			@PathVariable("version")
			@ApiIgnore
			String versionName,
			@RequestParam(value="loggedDialogueId")
			String loggedDialogueId,
			@RequestParam(value="loggedInteractionIndex")
			int loggedInteractionIndex,
			@RequestParam(value="time", required=false, defaultValue="")
			String time,
			@RequestParam(value="timezone", required=false, defaultValue="")
			String timezone) throws HttpException, Exception {
		logger.info("POST /back-dialogue");
		return QueryRunner.runQuery(
				(version, user) -> doBackDialogue(user, loggedDialogueId,
						loggedInteractionIndex, time, timezone),
				versionName, request, response);
	}

	private DialogueMessage doBackDialogue(String userId,
			String loggedDialogueId, int loggedInteractionIndex,
			String timeStr, String timezone) throws HttpException,
			DatabaseException, IOException {
		DateTime time = parseTime(timeStr, timezone);
		try {
			UserService userService = application.getServiceManager()
					.getActiveUserService(userId);
			DialogueState state = userService.getDialogueState(loggedDialogueId,
					loggedInteractionIndex);
			ExecuteNodeResult prevNode = userService.backDialogue(state, time);
			return DialogueMessageFactory.generateDialogueMessage(prevNode);
		} catch (WoolException e) {
			throw createHttpException(e);
		}
	}

	@RequestMapping(value="/current-dialogue", method=RequestMethod.GET)
	public NullableResponse<DialogueMessage> getCurrentDialogue(
			HttpServletRequest request,
			HttpServletResponse response,
			@PathVariable("version")
			@ApiIgnore
			String versionName,
			@RequestParam(value="dialogueName")
			String dialogueName,
			@RequestParam(value="time", required=false, defaultValue="")
			String time,
			@RequestParam(value="timezone", required=false, defaultValue="")
			String timezone) throws HttpException, Exception {
		logger.info("Get /current-dialogue?dialogueName=" + dialogueName);
		return QueryRunner.runQuery(
				(version, user) -> doGetCurrentDialogue(user, dialogueName,
						time, timezone),
				versionName, request, response);
	}

	private NullableResponse<DialogueMessage> doGetCurrentDialogue(
			String userId, String dialogueName, String timeStr, String timezone)
			throws HttpException, DatabaseException, IOException {
		DateTime time = parseTime(timeStr, timezone);
		LoggedDialogue currDlg =
				LoggedDialogueStoreIO.findLatestOngoingDialogue(userId,
				dialogueName);
		WoolLoggedInteraction lastInteraction = null;
		if (currDlg != null && !currDlg.getInteractionList().isEmpty()) {
			lastInteraction = currDlg.getInteractionList().get(
					currDlg.getInteractionList().size() - 1);
		}
		UserService userService = application.getServiceManager()
				.getActiveUserService(userId);
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

	@RequestMapping(value="/cancel-dialogue", method=RequestMethod.POST)
	public void cancelDialogue(
			HttpServletRequest request,
			HttpServletResponse response,
			@PathVariable("version")
			@ApiIgnore
			String versionName,
			@RequestParam(value="loggedDialogueId")
			String loggedDialogueId) throws HttpException, Exception {
		logger.info("POST /cancel-dialogue");
		QueryRunner.runQuery((version, user) ->
				doCancelDialogue(user, loggedDialogueId),
				versionName, request, response);
	}

	private Object doCancelDialogue(String userId, String loggedDialogueId)
			throws HttpException, DatabaseException, IOException {
		application.getServiceManager().getActiveUserService(userId)
				.cancelDialogue(loggedDialogueId);
		return null;
	}

	public static DateTime parseTime(String timeStr, String timezone)
			throws BadRequestException {
		List<HttpFieldError> errors = new ArrayList<>();
		DateTime time = ServiceManager.parseTimeParameters(timeStr, timezone,
				errors);
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
