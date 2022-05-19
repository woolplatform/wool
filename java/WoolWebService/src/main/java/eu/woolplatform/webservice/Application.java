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
package eu.woolplatform.webservice;

import eu.woolplatform.utils.AppComponents;
import eu.woolplatform.webservice.execution.DefaultUserServiceFactory;
import eu.woolplatform.webservice.execution.UserServiceManager;
import eu.woolplatform.webservice.execution.UserServiceFactory;
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
import org.springframework.util.ClassUtils;

import java.net.URL;

/**
 * The main entry point for the WOOL Web Service as a Spring Boot Application.
 * 
 * @author Dennis Hofs (RRD)
 * @author Harm op den Akker
 */
@SpringBootApplication(
		exclude={MongoAutoConfiguration.class}
)
@EnableScheduling
public class Application extends SpringBootServletInitializer implements
ApplicationListener<ContextClosedEvent> {

	private final Logger logger = AppComponents.getLogger(ClassUtils.getUserClass(getClass()).getSimpleName());
	private final UserServiceManager userServiceManager;

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
		if(propertiesUrl != null)
			config.loadProperties(propertiesUrl);

		// By default, log uncaught exceptions to this logger
		Thread.setDefaultUncaughtExceptionHandler((t, e) ->
				logger.error("Uncaught exception: " + e.getMessage(), e)
		);

		UserServiceFactory userServiceFactory = new DefaultUserServiceFactory();
		UserServiceFactory.setInstance(userServiceFactory);
		userServiceManager = new UserServiceManager(
				new WoolResourceFileLoader("dialogues"));

		// Print out some logging info
		logger.info("Successfully started WOOL Web Service.");
		logger.info("Service Version: " + config.get(Configuration.VERSION));
		logger.info("Build: " + "(in development)"); // TODO: Automatically populate "Build" config parameter
		logger.info("External Variable Service Enabled: "+config.getExternalVariableServiceEnabled());
		if(config.getExternalVariableServiceEnabled()) {
			logger.info("External Variable Service URL: "+config.getExternalVariableServiceURL());
		}

	}

	public UserServiceManager getServiceManager() {
		return userServiceManager;
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
