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
package eu.woolplatform.webservice.controller;

import eu.woolplatform.webservice.controller.model.WoolVariableParam;
import eu.woolplatform.webservice.exception.HttpException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@RestController
@RequestMapping("/v{version}/variables")
public class VariablesController {

	// ----- END-POINT: "retrieve-updates"

	@RequestMapping(value="/retrieve-updates/{userId}", method= RequestMethod.POST, consumes={
			MediaType.APPLICATION_JSON_VALUE })
	public List<WoolVariableParam> retrieveUpdates(
			HttpServletRequest request,
			HttpServletResponse response,
			@PathVariable("version")
			@ApiIgnore
					String versionName,
			@PathVariable String userId,
			@RequestBody
					List<WoolVariableParam> woolVariables) throws HttpException, Exception {
		return executeRetrieveUpdates(userId, woolVariables);
	}

	/**
	 * This method performs the "dummy" updating of the requested list of WOOL Variables. For a real-world
	 * implementation you should replace this method and have it do something useful.
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
	private List<WoolVariableParam> executeRetrieveUpdates(String userId, List<WoolVariableParam> params) {

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

	@RequestMapping(value="/notify-updated/{userId}", method= RequestMethod.POST, consumes={
			MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<?> notifyUpdated(
			HttpServletRequest request,
			HttpServletResponse response,
			@PathVariable("version")
			@ApiIgnore
					String versionName,
			@PathVariable String userId,
			@RequestBody
					List<WoolVariableParam> woolVariables) throws HttpException, Exception {
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
	 * TODO: Maybe this method could (randomly) return different responses? Not sure if that's useful.
	 */
	private ResponseEntity<?> executeNotifyUpdated(String userId, List<WoolVariableParam> params) {
		return new ResponseEntity(HttpStatus.OK);
	}
}
