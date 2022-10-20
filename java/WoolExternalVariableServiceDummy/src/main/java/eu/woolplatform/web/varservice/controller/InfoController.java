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

package eu.woolplatform.web.varservice.controller;

import eu.woolplatform.utils.AppComponents;
import eu.woolplatform.web.varservice.Application;
import eu.woolplatform.web.varservice.Configuration;
import eu.woolplatform.web.varservice.ProtocolVersion;
import eu.woolplatform.web.varservice.ServiceContext;
import eu.woolplatform.web.varservice.controller.schema.ServiceInfoPayload;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/v{version}/info")
@Tag(name = "3. Information",
	 description = "End-points that provide information about the running service")
public class InfoController {

	private final Logger logger = AppComponents.getLogger(getClass().getSimpleName());

	@Autowired
	Application application;

	@Operation(summary = "Retrieve a set of metadata parameters about the running service",
		description = "This end-point may be called without authentication and will return 3 " +
			"parameters that describe the current version of the service:" +
			" <ul><li>build - Date & Time when the service was built</li>" +
			" <li>protocolVersion - latest supported API Protocol version</li>" +
			" <li>serviceVersion - software version of the service</li>" +
			" <li>upTime - String representing how long the service has been running</li></ul>")

	@GetMapping("/all")
	public ServiceInfoPayload all(
		@Parameter(hidden = true, description = "API Version to use, e.g. '1'")
		@PathVariable(value = "version")
		String versionName
	) {

		// If no explicit protocol version is provided, assume the latest version
		if(versionName == null) versionName = ProtocolVersion.getLatestVersion().versionName();

		// Log this call to the service log
		logger.info("/v"+ versionName+"/info/all");

		// Construct the string that indicates the service's uptime
		Long launchedTime = application.getLaunchedTime();
		Long currentTime = Instant.now().toEpochMilli();
		long upTimeMillis = currentTime - launchedTime;
		long days = TimeUnit.MILLISECONDS.toDays(upTimeMillis);
		upTimeMillis = upTimeMillis - (days * 86400000);
		long hours = TimeUnit.MILLISECONDS.toHours(upTimeMillis);
		upTimeMillis = upTimeMillis - (hours * 3600000);
		long minutes = TimeUnit.MILLISECONDS.toMinutes(upTimeMillis);
		String upTimeString = "" + days + "d " + hours + "h " + minutes + "m";

		return new ServiceInfoPayload(
				Configuration.getInstance().get(Configuration.BUILD_TIME),
				ServiceContext.getCurrentVersion(),
				Configuration.getInstance().get(Configuration.VERSION),
				upTimeString);
	}

}