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

package eu.woolplatform.web.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.apache.tomcat.util.codec.binary.Base64;

import java.security.Key;

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
			.signWith(getSecretKey(),SignatureAlgorithm.HS512)
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

		Claims claims = Jwts.parserBuilder().
				setSigningKey(getSecretKey()).build().parseClaimsJws(token).getBody();

		return new AuthDetails(claims.getSubject(), claims.getIssuedAt(),
				claims.getExpiration());
	}

	/**
	 * Gets the secret key by parsing the Base64 string in property
	 * jwtSecretKey in the configuration.
	 * 
	 * @return the secret key
	 */
	private static Key getSecretKey() {
		String base64Key = Configuration.getInstance().get(
				Configuration.JWT_SECRET_KEY);
		return Keys.hmacShaKeyFor(Base64.decodeBase64(base64Key));
	}
}
