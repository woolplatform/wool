package eu.woolplatform.webservice;

import java.util.Date;

/**
 * The authentication details that are included in a JWT token. It contains the
 * username of the authenticated user, the date/time when the JWT token was
 * issued, and the date/time when the JWT token expires.
 * 
 * @author Dennis Hofs (RRD)
 */
public class AuthDetails {
	private final String subject;
	private final Date issuedAt;
	private final Date expiration;

	/**
	 * Constructs a new instance.
	 * 
	 * @param subject the username of the authenticated user
	 * @param issuedAt the date/time when the JWT token was issued, with
	 * precision of seconds. Any milliseconds are discarded.
	 * @param expiration the date/time when the JWT token expires, with
	 * precision of seconds. Any milliseconds are discarded.
	 */
	public AuthDetails(String subject, Date issuedAt, Date expiration) {
		this.subject = subject;
		long seconds = issuedAt.getTime() / 1000;
		this.issuedAt = new Date(seconds * 1000);
		if (expiration == null) {
			this.expiration = null;
		} else {
			seconds = expiration.getTime() / 1000;
			this.expiration = new Date(seconds * 1000);
		}
	}

	/**
	 * Returns the username of the authenticated user.
	 * 
	 * @return the username of the authenticated user
	 */
	public String getSubject() {
		return subject;
	}
	
	/**
	 * The date/time when the JWT token was issued, with precision of seconds.
	 * 
	 * @return the date/time when the JWT token was issued, with precision of
	 * seconds
	 */
	public Date getIssuedAt() {
		return issuedAt;
	}

	/**
	 * Returns the date/time when the JWT token expires, with precision of
	 * seconds.
	 * 
	 * @return the date/time when the JWT token expires, with precision of
	 * seconds
	 */
	public Date getExpiration() {
		return expiration;
	}
}
