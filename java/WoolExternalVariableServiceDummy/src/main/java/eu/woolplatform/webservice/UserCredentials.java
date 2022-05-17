package eu.woolplatform.webservice;

public class UserCredentials {

	public static final String USER_ROLE_USER = "user";
	public static final String USER_ROLE_ADMIN = "admin";

	private String username;
	private String password;
	private String role;

	public UserCredentials(String username, String password, String role) {
		this.username = username;
		this.password = password;
		this.role = role;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public String getRole() {
		return role;
	}
}
