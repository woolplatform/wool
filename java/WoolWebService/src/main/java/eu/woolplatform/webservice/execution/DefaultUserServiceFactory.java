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
