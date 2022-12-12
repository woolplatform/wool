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

package eu.woolplatform.web.service.storage;

import nl.rrd.utils.AppComponents;
import eu.woolplatform.web.service.Configuration;
import eu.woolplatform.web.service.execution.UserServiceManager;
import eu.woolplatform.wool.execution.WoolVariable;
import eu.woolplatform.wool.execution.WoolVariableStore;
import eu.woolplatform.wool.execution.WoolVariableStoreChange;
import eu.woolplatform.wool.execution.WoolVariableStoreOnChangeListener;
import org.slf4j.Logger;
import org.springframework.http.*;
import org.springframework.util.ClassUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;

public class ExternalVariableServiceUpdater implements WoolVariableStoreOnChangeListener {

	private final Logger logger =
			AppComponents.getLogger(ClassUtils.getUserClass(getClass()).getSimpleName());
	private final Configuration config = AppComponents.get(Configuration.class);
	private final UserServiceManager userServiceManager;

	public ExternalVariableServiceUpdater(UserServiceManager userServiceManager) {
		this.userServiceManager = userServiceManager;
	}

	@Override
	public void onChange(WoolVariableStore woolVariableStore,
						 List<WoolVariableStoreChange> changes) {

		String userId = woolVariableStore.getWoolUser().getId();
		String userTimeZoneString
				= woolVariableStore.getWoolUser().getTimeZone().toString();

		List<WoolVariable> variablesToUpdate = new ArrayList<>();

		for(WoolVariableStoreChange change : changes) {
			WoolVariableStoreChange.Source source = change.getSource();

			if(!source.equals(WoolVariableStoreChange.Source.EXTERNAL_VARIABLE_SERVICE)) {
				// This change to the variable store did not come from an update through the
				// external variable service, so the external variable service should be notified

				if(config.getExternalVariableServiceEnabled()) {
					logger.info("An external WOOL Variable Service is configured to be enabled, " +
						"with parameters:");
					logger.info("URL: " + config.getExternalVariableServiceURL());
					logger.info("API Version: " + config.getExternalVariableServiceAPIVersion());

					if(change instanceof WoolVariableStoreChange.Clear) {
						RestTemplate restTemplate = new RestTemplate();
						HttpHeaders requestHeaders = new HttpHeaders();
						requestHeaders.set("X-Auth-Token",
							userServiceManager.getExternalVariableServiceAPIToken());

						String notifyClearedUrl = config.getExternalVariableServiceURL()
							+ "/v" + config.getExternalVariableServiceAPIVersion()
							+ "/variables/notify-cleared";

						LinkedMultiValueMap<String, String> allRequestParams =
							new LinkedMultiValueMap<>();
						allRequestParams.put("userId", Arrays.asList(userId));
						allRequestParams.put("timeZone", Arrays.asList(userTimeZoneString));

						HttpEntity<?> entity = new HttpEntity<>(requestHeaders);
						UriComponentsBuilder builder =
							UriComponentsBuilder.fromUriString(notifyClearedUrl)
								.queryParams(
									(LinkedMultiValueMap<String, String>) allRequestParams);
						UriComponents uriComponents = builder.build().encode();

						// Todo: check if we need to do something with the 200 OK response
						ResponseEntity<Object> response = restTemplate.exchange(
							uriComponents.toUri(),
							HttpMethod.POST,
							entity,
							Object.class);

					} else if (change instanceof WoolVariableStoreChange.Remove) {
						Collection<String> variableNames
							= ((WoolVariableStoreChange.Remove) change).getVariableNames();

						for (String variableName : variableNames) {
							long updatedTime = change.getTime().toEpochSecond() * 1000;

							variablesToUpdate.add(
								new WoolVariable(
									variableName,
									null,
									updatedTime,
									userTimeZoneString));
						}
					} else if (change instanceof WoolVariableStoreChange.Put) {
						Map<String,Object> changedVariables
							= ((WoolVariableStoreChange.Put) change).getVariables();

						long updatedTime = change.getTime().toEpochSecond() * 1000;

						for(String variableName : changedVariables.keySet()) {
							Object variableValue = changedVariables.get(variableName);
							variablesToUpdate.add(
								new WoolVariable(
									variableName,
									null,
									updatedTime,
									userTimeZoneString));
						}
					}
				}
			}
		}

		// Perform the actual REST call
		if(variablesToUpdate.size() > 0) {

			RestTemplate restTemplate = new RestTemplate();
			HttpHeaders requestHeaders = new HttpHeaders();
			requestHeaders.setContentType(MediaType.valueOf("application/json"));
			requestHeaders.set("X-Auth-Token",
					userServiceManager.getExternalVariableServiceAPIToken());

			String notifyUpdatesUrl = config.getExternalVariableServiceURL()
					+ "/v" + config.getExternalVariableServiceAPIVersion()
					+ "/variables/notify-updated";

			LinkedMultiValueMap<String, String> allRequestParams =
					new LinkedMultiValueMap<>();
			allRequestParams.put("userId", Arrays.asList(userId));
			allRequestParams.put("timeZone", Arrays.asList(userTimeZoneString));

			HttpEntity<?> entity = new HttpEntity<>(variablesToUpdate, requestHeaders);
			UriComponentsBuilder builder =
					UriComponentsBuilder.fromUriString(notifyUpdatesUrl)
							.queryParams(
									(LinkedMultiValueMap<String, String>) allRequestParams);
			UriComponents uriComponents = builder.build().encode();

			// Todo: check if we need to do something with the 200 OK response
			ResponseEntity<Object> response = restTemplate.exchange(
					uriComponents.toUri(),
					HttpMethod.POST,
					entity,
					Object.class);
		}
	}
}
