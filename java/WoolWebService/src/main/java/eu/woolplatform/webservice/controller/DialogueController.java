package eu.woolplatform.webservice.controller;

import eu.woolplatform.utils.AppComponents;
import eu.woolplatform.utils.exception.DatabaseException;
import eu.woolplatform.webservice.Application;
import eu.woolplatform.webservice.QueryRunner;
import eu.woolplatform.webservice.exception.HttpException;
import eu.woolplatform.webservice.exception.NotFoundException;
import eu.woolplatform.wool.exception.WoolException;
import eu.woolplatform.wool.model.protocol.DialogueMessage;
import eu.woolplatform.wool.model.protocol.DialogueMessageFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

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
