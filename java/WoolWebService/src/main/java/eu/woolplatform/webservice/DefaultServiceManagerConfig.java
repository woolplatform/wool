package eu.woolplatform.webservice;

import eu.woolplatform.utils.AppComponents;
import eu.woolplatform.utils.exception.DatabaseException;
import eu.woolplatform.utils.exception.ParseException;
import eu.woolplatform.webservice.dialogue.ServiceManager;
import eu.woolplatform.webservice.dialogue.ServiceManagerConfig;
import eu.woolplatform.webservice.dialogue.UserService;
import eu.woolplatform.webservice.model.VariableStoreIO;
import eu.woolplatform.wool.execution.WoolVariableStore;
import org.slf4j.Logger;

import java.io.IOException;

public class DefaultServiceManagerConfig extends ServiceManagerConfig {

	public DefaultServiceManagerConfig() {
	}

	@Override
	public UserService createUserService(String userId,
			ServiceManager serviceManager)
			throws DatabaseException, IOException {
		return new UserService(userId, serviceManager,
				(varStore, changes) -> onVariableStoreChanges(userId,
						varStore));
	}

	private void onVariableStoreChanges(String userId,
			WoolVariableStore varStore) {
		Logger logger = AppComponents.getLogger(getClass().getSimpleName());
		try {
			VariableStoreIO.writeVariables(userId, varStore.getModifiableMap(
					false, null));
		} catch (ParseException | IOException ex) {
			logger.error("Failed to write variable store changes: " +
					ex.getMessage(), ex);
		}
	}
}
