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
import eu.woolplatform.web.service.ServiceContext;
import eu.woolplatform.web.service.Configuration;
import eu.woolplatform.web.service.controller.model.ServiceInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "4. Information", description = "End-points that provide information about the running service")
@RequestMapping("/v{version}/info")
public class InfoController {

	private final Logger logger = AppComponents.getLogger(getClass().getSimpleName());

	@Operation(summary = "Retrieve a set of metadata parameters about the running service",
			description = "This end-point may be called without authentication and will return 3 variables" +
					" that describe the current version of the service:" +
					" <ul><li>build - Date & Time when the service was built</li>" +
					" <li>protocolVersion - latest supported API Protocol version</li>" +
					" <li>serviceVersion - software version of the service</li></ul>")
	@GetMapping("/all")
	public ServiceInfo all(
			@Parameter(hidden = true)
			@PathVariable(value = "version")
			String versionName
	) {

		// If no versionName is provided, or versionName is empty, assume the latest version
		if (versionName == null || versionName.equals("")) {
			versionName = ProtocolVersion.getLatestVersion().versionName();
		}

		// Log this call to the service log
		String logInfo = "GET /v"+versionName+"/info/all";
		logger.info(logInfo);

		return new ServiceInfo(Configuration.getInstance().get(Configuration.BUILD_TIME),
				ServiceContext.getCurrentVersion(),
				Configuration.getInstance().get(Configuration.VERSION));
	}

}
