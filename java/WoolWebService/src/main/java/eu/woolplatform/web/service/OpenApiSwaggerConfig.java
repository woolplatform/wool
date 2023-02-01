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

package eu.woolplatform.web.service;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.apache.commons.lang3.StringUtils;
import org.springdoc.core.models.GroupedOpenApi;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.*;
import java.util.stream.Collectors;

@Configuration
@OpenAPIDefinition
public class OpenApiSwaggerConfig {

	private static final Object LOCK = new Object();
	private static boolean customizedPaths = false;

	@Bean
	public OpenAPI woolOpenAPI() {
		OpenAPI openAPI = new OpenAPI();

		// Add a Server + Version for each supported ProtocolVersion
		// Make sure the latest version is added first, so it is the one automatically selected in
		// the Swagger Server selection drop-down menu
		ProtocolVersion[] allVersions = ProtocolVersion.values();
		for(int i=allVersions.length-1; i>=0; i--) {
			Server server = new Server();
			server.url(ServiceContext.getBaseUrl()+"/v"+allVersions[i].versionName());
			openAPI.addServersItem(server);
		}

		// Finally, add the base server path (without version)
		Server server = new Server();
		server.url(ServiceContext.getBaseUrl());
		openAPI.addServersItem(server);

		openAPI.components(new Components().addSecuritySchemes("X-Auth-Token",
				new SecurityScheme()
						.name("X-Auth-Token")
						.scheme("basic")
						.type(SecurityScheme.Type.APIKEY)
						.in(SecurityScheme.In.HEADER)

		));

		openAPI.info(
				new Info()
						.title("WOOL Web Service API")
						.description("The WOOL Web Service API gives authorized clients the ability to start-, and sequentially execute WOOL dialogues as well to access WOOL Variable data.")
						.version("v1.2.0")
						.contact(new Contact().email("info@woolplatform.eu").name("WOOL Platform Support"))
						.license(new License().name("MIT").url("https://opensource.org/licenses/MIT")));

		return openAPI;
	}

	@Bean
	public GroupedOpenApi withoutVersioning() {
		return GroupedOpenApi.builder().group("API End-Points without Versioning")
			.pathsToExclude("/v{version}/info/*",
					"/v{version}/variables/*",
					"/v{version}/dialogue/*",
					"/v{version}/auth/*")
			//.pathsToExclude("/v{version}/**") <-- This must be a bug, because it doesn't let /variables/ come through
			.build();
	}

	private void customiseApi(OpenAPI api) {
		transformPaths(api.getPaths());
	}

	private void transformPaths(Paths paths) {
		synchronized (LOCK) {
			if (customizedPaths)
				return;
			customizedPaths = true;
			List<String> keys = new ArrayList<>(paths.keySet());
			String versionPath = "/v{version}";
			Map<String, PathItem> unorderedMap = new HashMap<>();
			for (String key : keys) {
				PathItem item = paths.remove(key);
				if (!key.startsWith(versionPath))
					continue;
				String operationPath = key.substring(versionPath.length());
				unorderedMap.put(operationPath, item);
			}
			List<String> orderedKeys = new ArrayList<>(unorderedMap.keySet());
			orderedKeys.sort(null);
			for (String key : orderedKeys) {
				paths.put(key, unorderedMap.get(key));
			}
		}
	}

}