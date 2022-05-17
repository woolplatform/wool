package eu.woolplatform.webservice;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.tomcat.util.codec.binary.Base64;

/**
 * This class can create or parse a signed Base64 JWT token string. This is
 * a stateless authentication because no tokens or session keys need to be
 * saved in the database. When users log in, they will get a token string.
 * At every request they should authenticate with the token string. The token
 * encodes an instance of {@link AuthDetails AuthDetails}, which defines the
 * user identity and token validity.
 * 
 * @author Dennis Hofs (RRD)
 */
public class AuthToken {
	/**
	 * Creates the signed Base64 JWT token string for the specified
	 * authentication details.
	 * 
	 * @param details the authentication details
	 * @return the token string
	 */
	public static String createToken(AuthDetails details) {
		Claims claims = Jwts.claims().setSubject(details.getSubject())
				.setIssuedAt(details.getIssuedAt())
				.setExpiration(details.getExpiration());
		return Jwts.builder().setClaims(claims)
			.signWith(SignatureAlgorithm.HS512, getSecretKey())
			.compact();
	}
	
	/**
	 * Parses the specified signed Base64 JWT token string and returns the
	 * authentication details. If the token can't be parsed, this method
	 * throws an exception.
	 * 
	 * @param token the token
	 * @return the authentication details
	 * @throws JwtException if the token can't be parsed
	 */
	public static AuthDetails parseToken(String token) throws JwtException {
		Claims claims = Jwts.parser().setSigningKey(getSecretKey())
				.parseClaimsJws(token).getBody();
		return new AuthDetails(claims.getSubject(), claims.getIssuedAt(),
				claims.getExpiration());
	}

	/**
	 * Gets the secret key by parsing the Base64 string in property
	 * jwtSecretKey in the configuration.
	 * 
	 * @return the secret key
	 */
	private static byte[] getSecretKey() {
		String base64Key = Configuration.getInstance().get(
				Configuration.JWT_SECRET_KEY);
		return Base64.decodeBase64(base64Key);
	}
}
