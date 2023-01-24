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

package eu.woolplatform.web.logservice;

import eu.woolplatform.web.logservice.exception.HttpException;

/**
 * Implementations of this interface can be passed to {@link QueryRunner
 * QueryRunner} to run a query using the authentication database.
 * 
 * @author Dennis Hofs (RRD)
 *
 * @param <T> the type of the query result
 */
public interface AuthQuery<T> {
	
	/**
	 * Runs the query. If a token was passed to the {@link QueryRunner
	 * QueryRunner}, then the token has been validated and the parameter "user"
	 * is specified. If the token was invalid, this method will never be
	 * called. If no token was specified, the user will be null.
	 * 
	 * @param version the protocol version
	 * @param user the user or null
	 * @return the query result
	 * @throws HttpException if the query should return an HTTP error status
	 * @throws Exception if an unexpected error occurs. This results in HTTP
	 * error status 500 Internal Server Error.
	 */
	T runQuery(ProtocolVersion version, String user)
			throws HttpException, Exception;
}
