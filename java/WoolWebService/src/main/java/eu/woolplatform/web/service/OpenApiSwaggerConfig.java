package eu.woolplatform.web.service;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


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
}