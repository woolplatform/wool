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
package eu.woolplatform.web.service;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.apache.commons.lang3.StringUtils;
import org.springdoc.core.customizers.OpenApiCustomiser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Comparator;
import java.util.stream.Collectors;

@Configuration
@OpenAPIDefinition
public class OpenApiSwaggerConfig {

	@Bean
	public OpenAPI woolOpenAPI() {
		OpenAPI openAPI = new OpenAPI();

		// Add the base URL without version
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

	/**
	 * A Bean to set the order of the display of Controllers in Swagger.
	 * @return The {@link OpenApiCustomiser} object.
	 */
	@Bean
	public OpenApiCustomiser sortTagsAlphabetically() {
		return openApi -> openApi.setTags(openApi.getTags()
				.stream()
				.sorted(Comparator.comparing(tag -> StringUtils.stripAccents(tag.getName())))
				.collect(Collectors.toList()));
	}
}