package eu.woolplatform.web.varservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class JacksonConfiguration {

	@Bean
	@Primary
	public ObjectMapper defaultMapper() {
		ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper;
	}

}

