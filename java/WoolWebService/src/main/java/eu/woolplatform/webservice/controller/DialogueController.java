package eu.woolplatform.webservice.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import eu.woolplatform.utils.AppComponents;
import eu.woolplatform.utils.exception.DatabaseException;
import eu.woolplatform.utils.exception.ParseException;
import eu.woolplatform.utils.io.FileUtils;
import eu.woolplatform.utils.json.JsonMapper;
import eu.woolplatform.webservice.Application;
import eu.woolplatform.webservice.QueryRunner;
import eu.woolplatform.webservice.exception.BadRequestException;
import eu.woolplatform.webservice.exception.HttpException;
import eu.woolplatform.webservice.exception.NotFoundException;
import eu.woolplatform.wool.exception.WoolException;
import eu.woolplatform.wool.model.WoolNode;
import eu.woolplatform.wool.model.protocol.DialogueMessage;
import eu.woolplatform.wool.model.protocol.DialogueMessageFactory;
import eu.woolplatform.wool.model.protocol.NullableResponse;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/v{version}/dialogue")
public class DialogueController {
	@Autowired
	Application application;

	private Logger logger = AppComponents.getLogger(getClass().getSimpleName());

	@RequestMapping(value="/start-dialogue", method= RequestMethod.POST)
	public ResponseEntity<DialogueMessage> startDialogue(
			HttpServletRequest request,
			HttpServletResponse response,
			@PathVariable("version")
			@ApiIgnore
			String versionName,
			@RequestParam(value="dialogueName")
			String dialogueName,
			@RequestParam(value="language")
			String language) throws HttpException, Exception {
		logger.info("Post /start-dialogue?dialogueName=" + dialogueName +
				"&language=" + language);
		return QueryRunner.runQuery(
				(version, user) -> doStartDialogue(user, dialogueName,
						language),
				versionName, request, response);
	}

	private ResponseEntity<DialogueMessage> doStartDialogue(
			String userId, String dialogueName, String language)
			throws HttpException, DatabaseException, IOException {
		ResponseEntity<DialogueMessage> httpReply;
		try {
			DialogueMessage reply = DialogueMessageFactory.generateDialogueMessage(
					application.getServiceManager().getActiveUserService(userId)
							.startDialogue(dialogueName, null, language));
			httpReply = new ResponseEntity<>(reply, HttpStatus.OK);
		} catch (WoolException e) {
			throw createHttpException(e);
		}
		return httpReply;
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
			@RequestParam(value="replyId")
			int replyId) throws HttpException, Exception {
		logger.info("POST /progress-dialogue?replyId=" + replyId);
		return QueryRunner.runQuery(
				(version, user) -> doProgressDialogue(user, request, replyId),
				versionName, request, response);
	}

	private NullableResponse<DialogueMessage> doProgressDialogue(String userId,
			HttpServletRequest request, int replyId) throws HttpException,
			DatabaseException, IOException {
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
			if (!variables.isEmpty()) {
				application.getServiceManager().getActiveUserService(userId)
						.storeReplyInput(variables);
			}
			WoolNode nextNode = application.getServiceManager()
					.getActiveUserService(userId).progressDialogue(replyId);
			if (nextNode == null)
				return new NullableResponse<>(null);
			DialogueMessage reply = DialogueMessageFactory.generateDialogueMessage(
					nextNode);
			return new NullableResponse<>(reply);
		} catch (WoolException e) {
			throw createHttpException(e);
		}
	}

	@RequestMapping(value="/cancel-dialogue", method=RequestMethod.POST)
	public void cancelDialogue(
			HttpServletRequest request,
			HttpServletResponse response,
			@PathVariable("version")
			@ApiIgnore
			String versionName) throws HttpException, Exception {
		logger.info("POST /cancel-dialogue");
		QueryRunner.runQuery((version, user) -> doCancelDialogue(user),
				versionName, request, response);
	}

	private Object doCancelDialogue(String userId) throws HttpException,
			DatabaseException, IOException {
		application.getServiceManager().getActiveUserService(userId)
				.cancelDialogue();
		return null;
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
