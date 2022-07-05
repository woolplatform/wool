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
import eu.woolplatform.utils.exception.ParseException;
import eu.woolplatform.utils.io.FileUtils;
import eu.woolplatform.utils.json.JsonMapper;
import eu.woolplatform.web.service.Application;
import eu.woolplatform.web.service.QueryRunner;
import eu.woolplatform.web.service.exception.BadRequestException;
import eu.woolplatform.web.service.exception.ErrorCode;
import eu.woolplatform.web.service.exception.HttpError;
import eu.woolplatform.web.service.exception.HttpFieldError;
import eu.woolplatform.web.service.execution.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.util.*;

@RestController
@SecurityRequirement(name = "X-Auth-Token")
@RequestMapping("/v{version}")
public class DataController {
	@Autowired
	Application application;

	private final Logger logger = AppComponents.getLogger(getClass().getSimpleName());

	@RequestMapping(value="/variables", method=RequestMethod.GET)
	public Map<String,Object> getVariables(
			HttpServletRequest request,
			HttpServletResponse response,
			@PathVariable("version")
			String versionName,
			@RequestParam(value="names", required=false, defaultValue="")
			String names,
			@RequestParam(value="woolUserId", required=false, defaultValue="")
			String woolUserId) throws Exception {
		if(woolUserId.equals("")) {
			logger.info("Get /variables?names=" + names);
			return QueryRunner.runQuery(
				(version, user) -> doGetVariables(user, names),
				versionName, request, response, woolUserId, application);
		} else {
			logger.info("Get /variables?names=" + names+"&woolUserId="+woolUserId);
			return QueryRunner.runQuery(
				(version, user) -> doGetVariables(woolUserId, names),
				versionName, request, response, woolUserId, application);
		}
	}

	private Map<String,Object> doGetVariables(String woolUserId, String names)
			throws Exception {
		UserService userService = application.getServiceManager()
				.getActiveUserService(woolUserId);
		Map<String,?> varStore = userService.variableStore.getModifiableMap(
				false, null);
		names = names.trim();
		List<String> nameList;
		if (names.length() == 0) {
			nameList = new ArrayList<>(varStore.keySet());
			Collections.sort(nameList);
		} else {
			List<String> invalidNames = new ArrayList<>();
			String[] nameArray = names.split("\\s+");
			for (String name : nameArray) {
				if (!name.matches("[A-Za-z][A-Za-z0-9_]*"))
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
			result.put(name, varStore.get(name));
		}
		return result;
	}

	@RequestMapping(value="/variable", method=RequestMethod.POST)
	public void setVariable(
			HttpServletRequest request,
			HttpServletResponse response,
			@PathVariable("version")
			String versionName,
			@RequestParam(value="name")
			String name,
			@RequestParam(value="value", required=false, defaultValue="")
			String value,
			@RequestParam(value="woolUserId",required=false,defaultValue="")
			String woolUserId) throws Exception {
		if(woolUserId.equals("")) {
			logger.info("Post /variable?name=" + name + "&value=" + value);
			QueryRunner.runQuery((version, user) ->
				doSetVariable(request, user, name, value),
				versionName, request, response, woolUserId, application);
		} else {
			logger.info("Post /variable?name=" + name + "&value=" + value + "&woolUserId=" + woolUserId);
			QueryRunner.runQuery((version, user) ->
				doSetVariable(request, woolUserId, name, value),
				versionName, request, response, woolUserId, application);
		}
	}

	private Object doSetVariable(HttpServletRequest request, String woolUserId,
			String name, String value) throws Exception {
		List<HttpFieldError> errors = new ArrayList<>();
		if (!name.matches("[A-Za-z][A-Za-z0-9_]*")) {
			errors.add(new HttpFieldError("name",
					"Invalid variable name: " + name));
		}
		if (!errors.isEmpty())
			throw BadRequestException.withInvalidInput(errors);
		String bodyString;
		try (InputStream input = request.getInputStream()) {
			bodyString = FileUtils.readFileString(input).trim();
		}
		Object setValue = value;
		if (!bodyString.isEmpty()) {
			Map<?,?> body;
			try {
				body = JsonMapper.parse(bodyString, Map.class);
			} catch (ParseException ex) {
				throw new BadRequestException("Invalid JSON content");
			}
			if (!body.containsKey("value")) {
				HttpFieldError error = new HttpFieldError("value",
						"Property \"value\" not found in content");
				throw BadRequestException.withInvalidInput(error);
			}
			setValue = body.get("value");
		}
		UserService userService = application.getServiceManager()
				.getActiveUserService(woolUserId);
		userService.variableStore.setValue(name, setValue, true, null);
		return null;
	}

	@RequestMapping(value="/variables", method=RequestMethod.POST)
	public void setVariables(
			HttpServletRequest request,
			HttpServletResponse response,
			@PathVariable("version")
			String versionName,
			@RequestParam(value="woolUserId",required=false,defaultValue="")
			String woolUserId,
			@RequestBody
			Map<String,Object> woolVariables) throws Exception {
		if(woolUserId.equals("")) {
			logger.info("Post /variables");
			QueryRunner.runQuery((version, user) -> doSetVariables(request, user, woolVariables),
				versionName, request, response, woolUserId, application);
		} else {
			logger.info("Post /variables?woolUserId="+woolUserId);
			QueryRunner.runQuery((version, user) -> doSetVariables(request, woolUserId, woolVariables),
				versionName, request, response, woolUserId, application);
		}
	}

	private Object doSetVariables(HttpServletRequest request, String woolUserId, Map<String,Object> woolVariables)
			throws Exception {

		List<String> invalidNames = new ArrayList<>();
		for (String name : woolVariables.keySet()) {
			if (!name.matches("[A-Za-z][A-Za-z0-9_]*"))
				invalidNames.add(name);
		}
		if (!invalidNames.isEmpty()) {
			HttpError error = new HttpError(ErrorCode.INVALID_INPUT,
					"Invalid variable names: " +
					String.join(", ", invalidNames));
			throw new BadRequestException(error);
		}
		UserService userService = application.getServiceManager()
				.getActiveUserService(woolUserId);
		Map<String,Object> varStore = userService.variableStore
				.getModifiableMap(true, null);
		varStore.putAll(woolVariables);
		return null;
	}
}
