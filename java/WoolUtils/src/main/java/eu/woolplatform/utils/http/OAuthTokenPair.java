package eu.woolplatform.utils.http;

public class OAuthTokenPair {
	private String token;
	private String tokenSecret;

	public OAuthTokenPair(String token, String tokenSecret) {
		this.token = token;
		this.tokenSecret = tokenSecret;
	}

	public String getToken() {
		return token;
	}

	public String getTokenSecret() {
		return tokenSecret;
	}
}
