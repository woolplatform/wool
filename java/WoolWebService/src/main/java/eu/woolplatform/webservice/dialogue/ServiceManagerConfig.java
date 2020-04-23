package eu.woolplatform.webservice.dialogue;

import eu.woolplatform.utils.exception.DatabaseException;
import eu.woolplatform.wool.model.WoolProject;

import java.io.IOException;

public abstract class ServiceManagerConfig {
	private WoolProject woolProject;
	
	private static ServiceManagerConfig instance = null;
	
	public static ServiceManagerConfig getInstance() {
		return instance;
	}
	
	public static void setInstance(ServiceManagerConfig instance) {
		ServiceManagerConfig.instance = instance;
	}

	/**
	 * Constructs a new configuration.
	 */
	public ServiceManagerConfig() {
	}

	public void setWoolProject(WoolProject woolProject) {
		this.woolProject = woolProject;
	}

	public WoolProject getWoolProject() {
		return woolProject;
	}

	public abstract UserService createUserService(String userId,
			ServiceManager serviceManager)
			throws DatabaseException, IOException;
}
