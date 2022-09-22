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
package eu.woolplatform.web.varservice.controller.model;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import eu.woolplatform.utils.json.JsonObject;

import java.io.IOException;

/**
 * A {@link LoginParams} object models the information that is sent in the request body
 * of a call to the /auth/login end-point as handled by the {@link eu.woolplatform.web.varservice.controller.AuthController},
 * which can be serialized/deserialized to the following JSON Format:
 *
 * <pre>
 * {
 *   "user": "string",
 *   "password": "string",
 *   "tokenExpiration": 0
 * }</pre>
 *
 * Note that "tokenExpiration" can be an integer value of 0 or greater, indicating the expiration time in minutes, or "never".
 *
 * @author Harm op den Akker
 */
public class LoginParams extends JsonObject {
	private String user = null;
	private String password = null;
	private Integer tokenExpiration = 1440;

	/**
	 * Returns the username part of this {@link LoginParams}.
	 * @return the username part of this {@link LoginParams}.
	 */
	public String getUser() {
		return user;
	}

	/**
	 * Sets the username part of this {@link LoginParams}.
	 * @param user the username part of this {@link LoginParams}.
	 */
	public void setUser(String user) {
		this.user = user;
	}

	/**
	 * Returns the password part of this {@link LoginParams}.
	 * @return the password part of this {@link LoginParams}.
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Sets the password part of this {@link LoginParams}.
	 * @param password the password part of this {@link LoginParams}.
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * Returns the time (in minutes) after which the auth token should expire. When set to {@code null}
	 * this means that the token should never expire.
	 * @return the time (in minutes) after which the auth token should expire.
	 */
	@JsonSerialize(using = TokenExpirationSerializer.class)
	public Integer getTokenExpiration() {
		return tokenExpiration;
	}

	/**
	 * Sets the time (in minutes) after which the auth token should expire. When set to {@code null}
	 * 	 * this means that the token should never expire.
	 * @param tokenExpiration the time (in minutes) after which the auth token should expire.
	 */
	@JsonDeserialize(using = TokenExpirationDeserializer.class)
	public void setTokenExpiration(Integer tokenExpiration) {
		this.tokenExpiration = tokenExpiration;
	}

	/**
	 * Inner class used to convert the {@code tokenExpiration} to JSON string format, as either
	 * a number, or the String "never".
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
	 * a number, or the String "never". The number 0 will be treated as never. Any other string besides
	 * "never" will generate an error.
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
