package eu.woolplatform.webservice.controller.model;

public class LoginResult {
	private String user;
	private String token;

	public LoginResult(String user, String token) {
		this.user = user;
		this.token = token;
	}

	public String getUser() {
		return user;
	}

	public String getToken() {
		return token;
	}
}
