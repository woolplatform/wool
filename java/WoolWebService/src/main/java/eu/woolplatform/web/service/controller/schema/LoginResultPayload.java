/*
 * Copyright 2019-2022 WOOL Foundation - Licensed under the MIT License:
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package eu.woolplatform.web.service.controller.schema;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * A {@link LoginResultPayload} is returned by the /auth/login end-point as handled by the
 * {@link eu.woolplatform.web.service.controller.AuthController} in case of a successful
 * login and contains the username and JSON Web Token, which can be serialized /
 * deserialized to the following JSON Format:
 * <pre>
 * {
 *   "user": "user",
 *   "token": "See <a href="https://jwt.io/">jwt.io</a>"
 * }
 * </pre>
 *
 * @author Harm op den Akker
 */
public class LoginResultPayload {

	@Schema(description = "Username associated with this authentication token",
			example = "user")
	private String user;

	@Schema(description = "The JSON Web Token that was generated for the user",
			example = "See https://jwt.io/")
	private String token;

	// --------------------------------------------------------
	// -------------------- Constructor(s) --------------------
	// --------------------------------------------------------

	/**
	 * Creates an empty instance of a {@link LoginResultPayload}.
	 */
	public LoginResultPayload() { }

	/**
	 * Creates an instance of a {@link LoginResultPayload} with a given {@code user} and
	 * {@code token}.
	 * @param user the username of the user that performed a successful login.
	 * @param token the JSON Web Token generated for the user as a {@link String}.
	 */
	public LoginResultPayload(String user, String token) {
		this.user = user;
		this.token = token;
	}

	// -------------------------------------------------------------
	// -------------------- Getters and Setters --------------------
	// -------------------------------------------------------------

	/**
	 * Returns the username of the user that performed a successful login.
	 * @return the username of the user that performed a successful login.
	 */
	public String getUser() {
		return user;
	}

	/**
	 * Sets the username of the user that performed a successful login.
	 * @param user the username of the user that performed a successful login.
	 */
	public void setUser(String user) {
		this.user = user;
	}

	/**
	 * Returns the JSON Web Token generated for the user as a {@link String}.
	 * @return the JSON Web Token generated for the user as a {@link String}.
	 */
	public String getToken() {
		return token;
	}

	/**
	 * Sets the JSON Web Token generated for the user as a {@link String}.
	 * @param token the JSON Web Token generated for the user as a {@link String}.
	 */
	public void setToken(String token) {
		this.token = token;
	}

}
