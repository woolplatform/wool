package eu.woolplatform.webservice;

import eu.woolplatform.utils.AppComponents;
import eu.woolplatform.webservice.dialogue.ServiceManager;
import eu.woolplatform.webservice.dialogue.ServiceManagerConfig;
import eu.woolplatform.wool.parser.WoolResourceFileLoader;
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
 * The main entry point for the WOOL Web Service as a Spring Boot Application.
 * 
 * @author Dennis Hofs (RRD)
 */
@SpringBootApplication(
		exclude={MongoAutoConfiguration.class}
)
@EnableScheduling
public class Application extends SpringBootServletInitializer implements
ApplicationListener<ContextClosedEvent> {
	private ServiceManager serviceManager;

	/**
	 * Constructs a new application. It reads service.properties and
	 * initializes the {@link Configuration Configuration} and the {@link
	 * AppComponents AppComponents}.
	 * 
	 * @throws Exception if the application can't be initialised
	 */
	public Application() throws Exception {
		URL propsUrl = getClass().getClassLoader().getResource(
				"service.properties");
		if (propsUrl == null) {
			throw new Exception("Can't find resource service.properties. " +
					"Did you run gradlew updateConfig?");
		}
		Configuration config = AppComponents.get(Configuration.class);
		config.loadProperties(propsUrl);
		propsUrl = getClass().getClassLoader().getResource(
				"deployment.properties");
		config.loadProperties(propsUrl);
		final Logger logger = AppComponents.getLogger(
				getClass().getSimpleName());
		Thread.setDefaultUncaughtExceptionHandler((t, e) ->
				logger.error("Uncaught exception: " + e.getMessage(), e)
		);

		ServiceManagerConfig serviceManagerConfig =
				new DefaultServiceManagerConfig();
		ServiceManagerConfig.setInstance(serviceManagerConfig);
		serviceManager = new ServiceManager(new WoolResourceFileLoader(
				"dialogues"));

		logger.info("WOOL Web Service version: " + config.get(
				Configuration.VERSION));
	}

	public ServiceManager getServiceManager() {
		return serviceManager;
	}

	@Override
	public void onApplicationEvent(ContextClosedEvent event) {
		Logger logger = AppComponents.getLogger(getClass().getSimpleName());
		logger.info("Shutdown web service");
	}
	
	@Override
	protected SpringApplicationBuilder configure(
			SpringApplicationBuilder builder) {
		return builder.sources(Application.class);
	}

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
