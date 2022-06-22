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
package eu.woolplatform.web.service.execution;

import eu.woolplatform.utils.exception.DatabaseException;
import eu.woolplatform.wool.model.WoolProject;

import java.io.IOException;

public abstract class UserServiceFactory {

	private WoolProject woolProject;
	private static UserServiceFactory instance = null;

	// ----- Constructors

	public UserServiceFactory() {
	}

	// ----- Methods

	public static UserServiceFactory getInstance() {
		return instance;
	}
	
	public static void setInstance(UserServiceFactory instance) {
		UserServiceFactory.instance = instance;
	}

	// ----- Getters & Setters

	public void setWoolProject(WoolProject woolProject) {
		this.woolProject = woolProject;
	}

	public WoolProject getWoolProject() {
		return woolProject;
	}

	// ----- Abstracts

	public abstract UserService createUserService(String userId,
			UserServiceManager userServiceManager)
			throws DatabaseException, IOException;
}
