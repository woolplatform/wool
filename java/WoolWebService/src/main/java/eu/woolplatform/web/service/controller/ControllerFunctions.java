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

import eu.woolplatform.web.service.exception.BadRequestException;
import eu.woolplatform.web.service.exception.HttpException;
import eu.woolplatform.web.service.exception.HttpFieldError;
import eu.woolplatform.web.service.exception.NotFoundException;
import eu.woolplatform.wool.exception.WoolException;

import java.time.DateTimeException;
import java.time.ZoneId;
import java.time.zone.ZoneRulesException;
import java.util.ArrayList;
import java.util.List;

/**
 * The {@link ControllerFunctions} class offers a set of public, static methods
 * that can be used by the service's various REST controllers.
 */
public class ControllerFunctions {

	// -------------------------------------- //
	// ---------- Helper Functions ---------- //
	// -------------------------------------- //

	/**
	 * Parses a given String into a {@link ZoneId} time zone object. The given {@code timeZone}
	 * String should be formatted according to the rules defined in {@link ZoneId#of(String)}}.
	 * When given an empty string, or {@code null}, this method returns the default time zone as
	 * given by {@link ZoneId#systemDefault()}.
 	 * @param timeZone a String representation of a time zone.
	 * @return the time zone as a {@link ZoneId}
	 * @throws BadRequestException in case of a wrongly formatted {@code timeZone} string.
	 */
	public static ZoneId parseTimeZone(String timeZone) throws BadRequestException {

		if (timeZone == null || timeZone.length() == 0) {
			return ZoneId.systemDefault();
		}

		List<HttpFieldError> errors = new ArrayList<>();

		ZoneId result = null;

		try {
			result = ZoneId.of(timeZone);
		} catch (ZoneRulesException zoneRulesException) {
			errors.add(new HttpFieldError("timeZone",
					"Invalid value for field \"timeZone\": "
							+ timeZone + " (zone not recognized)."));
		} catch(DateTimeException dateTimeException) {
			errors.add(new HttpFieldError("timeZone",
					"Invalid value for field \"timeZone\": "
							+ timeZone + " (format incorrect)."));
		}

		if(!errors.isEmpty()) {
			throw BadRequestException.withInvalidInput(errors);
		} else {
			return result;
		}

	}

	/**
	 * Generates a {@link HttpException} with a valid HTTP Status Code from the given
	 * {@link WoolException}.
	 * @param exception the {@link WoolException} that should be "wrapped" into an
	 * 					{@link HttpException}.
	 * @return the {@link HttpException} object representing the error including a valid status
	 * 		   code.
	 */
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
