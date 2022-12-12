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

package eu.woolplatform.web.varservice.controller.schema;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import nl.rrd.utils.json.JsonObject;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.IOException;

/**
 * A {@link LoginParametersPayload} object models the information that is sent in the request body
 * of a call to the /auth/login end-point as handled by the
 * {@link eu.woolplatform.web.varservice.controller.AuthController}, which can be serialized /
 * deserialized to the following JSON Format:
 *
 * <pre>
 * {
 *   "user": "string",
 *   "password": "string",
 *   "tokenExpiration": 0
 * }</pre>
 *
 * Note that the "tokenExpiration" parameter can either be an integer value of 0 or greater,
 * indicating the expiration time in minutes, or it can be the string "never" which means (similar
 * to an expiration time of 0 minutes) that the token will not expire.
 *
 * @author Harm op den Akker
 */
public class LoginParametersPayload extends JsonObject {

	@Schema(description = "Username of the person or entity logging in",
			example = "user", required = true)
	private String user = null;

	@Schema(description = "Password for the given user",
			example = "password", required = true)
	private String password = null;

	@Schema(description = "Number of minutes (>=0) after which the authentication token should " +
			"expire, or 'never'",
			example = "0", required = true)
	private Integer tokenExpiration = 0;

	// --------------------------------------------------------
	// -------------------- Constructor(s) --------------------
	// --------------------------------------------------------

	/**
	 * Creates an instance of an empty {@link LoginParametersPayload}.
	 */
	public LoginParametersPayload() { }

	/**
	 * Creates an instance of a {@link LoginParametersPayload} with the given {@code user}, {@code
	 * password}, and {@code tokenExpiration}.
	 * @param user the user who is trying to perform a login.
	 * @param password the password provided by the user performing a login.
	 * @param tokenExpiration the time in minutes after which the token should expire, or
	 *                        {@code null}.
	 */
	public LoginParametersPayload(String user, String password, Integer tokenExpiration) {
		this.user = user;
		this.password = password;
		this.tokenExpiration = tokenExpiration;
	}

	// -----------------------------------------------------------
	// -------------------- Getters & Setters --------------------
	// -----------------------------------------------------------

	/**
	 * Returns the user who is trying to perform a login.
	 * @return the user who is trying to perform a login.
	 */
	public String getUser() {
		return user;
	}

	/**
	 * Sets the user who is trying to perform a login.
	 * @param user the user who is trying to perform a login.
	 */
	public void setUser(String user) {
		this.user = user;
	}

	/**
	 * Returns the password provided by the user performing a login.
	 * @return the password provided by the user performing a login.
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Sets the password provided by the user performing a login.
	 * @param password the password provided by the user performing a login.
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * Returns the time (in minutes) after which the auth token should expire. When set to
	 * {@code null} this means that the token should never expire.
	 * @return the time (in minutes) after which the auth token should expire.
	 */
	@JsonSerialize(using = TokenExpirationSerializer.class)
	public Integer getTokenExpiration() {
		return tokenExpiration;
	}

	/**
	 * Sets the time (in minutes) after which the auth token should expire. When set to {@code null}
	 * this means that the token should never expire.
	 * @param tokenExpiration the time (in minutes) after which the auth token should expire.
	 */
	@JsonDeserialize(using = TokenExpirationDeserializer.class)
	public void setTokenExpiration(Integer tokenExpiration) {
		this.tokenExpiration = tokenExpiration;
	}

	// -------------------------------------------------------------------------
	// -------------------- Serialization / Deserialization --------------------
	// -------------------------------------------------------------------------

	/**
	 * Inner class used to convert the {@code tokenExpiration} to JSON string format, as either a
	 * number, or the String "never".
	 */
	public static class TokenExpirationSerializer extends
			JsonSerializer<Integer> {
		@Override
		public void serialize(Integer value, JsonGenerator gen,
				SerializerProvider serializers) throws IOException {
			if (value == null)
				gen.writeString("never");
			else
				gen.writeNumber(value);
		}
	}

	/**
	 * Inner class used to convert the {@code tokenExpiration} from JSON string format, as either
	 * a number, or the String "never". The number 0 will be treated as never. Any other string
	 * besides "never" will generate an error.
	 */
	public static class TokenExpirationDeserializer extends
			JsonDeserializer<Integer> {
		@Override
		public Integer deserialize(JsonParser p, DeserializationContext context)
				throws IOException {
			String s = p.getValueAsString();
			if (s.equalsIgnoreCase("never"))
				return null;
			try {
				Integer value = Integer.parseInt(s);
				if(value == 0) return null;
				else return Integer.parseInt(s);
			} catch (NumberFormatException ex) {
				throw new JsonParseException(p, "Invalid int value: " + s,
						p.getCurrentLocation(), ex);
			}
		}
	}
}