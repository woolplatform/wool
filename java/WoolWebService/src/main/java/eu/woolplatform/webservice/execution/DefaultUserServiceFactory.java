package eu.woolplatform.webservice.execution;

import eu.woolplatform.utils.AppComponents;
import eu.woolplatform.utils.exception.DatabaseException;
import eu.woolplatform.utils.exception.ParseException;
import eu.woolplatform.webservice.model.VariableStoreIO;
import eu.woolplatform.wool.execution.WoolVariableStore;
import org.slf4j.Logger;

import java.io.IOException;

public class DefaultUserServiceFactory extends UserServiceFactory {

	public DefaultUserServiceFactory() {
	}

	@Override
	public UserService createUserService(String userId,
			UserServiceManager userServiceManager)
			throws DatabaseException, IOException {
		return new UserService(userId, userServiceManager,
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
