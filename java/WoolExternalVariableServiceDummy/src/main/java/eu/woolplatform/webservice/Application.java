package eu.woolplatform.webservice;

import eu.woolplatform.utils.AppComponents;
import org.slf4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.net.URL;

/**
 * The main entry point for the External Variable Service Dummy as a Spring Boot Application.
 * 
 * @author Harm op den Akker
 */
@SpringBootApplication(
		exclude={MongoAutoConfiguration.class} // TODO: Check why this is excluded.
)
@EnableScheduling
public class Application extends SpringBootServletInitializer implements
ApplicationListener<ContextClosedEvent> {

	/**
	 * Constructs a new application. It reads service.properties and
	 * initializes the {@link Configuration Configuration} and the {@link
	 * AppComponents AppComponents}.
	 * 
	 * @throws Exception if the application can't be initialised
	 */
	public Application() throws Exception {

		// Initialize a Configuration object
		Configuration config = AppComponents.get(Configuration.class);

		// Load the values from service.properties into the Configuration
		URL propertiesUrl = getClass().getClassLoader().getResource(
				"service.properties");
		if (propertiesUrl == null) {
			throw new Exception("Can't find resource service.properties. " +
					"Did you run gradlew updateConfig?");
		}
		config.loadProperties(propertiesUrl);

		// Load the values from deployment.properties into the Configuration (app version number)
		propertiesUrl = getClass().getClassLoader().getResource(
				"deployment.properties");
		config.loadProperties(propertiesUrl);

		// Initialize the Logger
		final Logger logger = AppComponents.getLogger(
				getClass().getSimpleName());

		// By default, log uncaught exceptions to this logger
		Thread.setDefaultUncaughtExceptionHandler((t, e) ->
				logger.error("Uncaught exception: " + e.getMessage(), e)
		);

		logger.info("WOOL External Variable Service Dummy version '"
				+ config.get(Configuration.VERSION)+"' started.");
	}

	@Override
	public void onApplicationEvent(ContextClosedEvent event) {
		Logger logger = AppComponents.getLogger(getClass().getSimpleName());
		logger.info("Shutdown WOOL External Variable Service Dummy.");
	}
	
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		return builder.sources(Application.class);
	}

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}