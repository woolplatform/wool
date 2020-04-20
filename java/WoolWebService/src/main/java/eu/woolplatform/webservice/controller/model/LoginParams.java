package eu.woolplatform.webservice.controller.model;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import eu.woolplatform.utils.json.JsonObject;

import java.io.IOException;

public class LoginParams extends JsonObject {
	private String user = null;
	private String password = null;
	private Integer tokenExpiration = 1440;

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@JsonSerialize(using = TokenExpirationSerializer.class)
	public Integer getTokenExpiration() {
		return tokenExpiration;
	}

	@JsonDeserialize(using = TokenExpirationDeserializer.class)
	public void setTokenExpiration(Integer tokenExpiration) {
		this.tokenExpiration = tokenExpiration;
	}

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

	public static class TokenExpirationDeserializer extends
			JsonDeserializer<Integer> {
		@Override
		public Integer deserialize(JsonParser p, DeserializationContext ctxt)
				throws IOException, JsonProcessingException {
			String s = p.getValueAsString();
			if (s.toLowerCase().equals("never"))
				return null;
			try {
				return Integer.parseInt(s);
			} catch (NumberFormatException ex) {
				throw new JsonParseException(p, "Invalid int value: " + s,
						p.getCurrentLocation(), ex);
			}
		}
	}
}
