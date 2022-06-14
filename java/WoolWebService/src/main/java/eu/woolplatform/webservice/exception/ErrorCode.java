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
package eu.woolplatform.webservice.exception;

/**
 * Possible error codes that may be returned by the WOOL Web Service.
 * 
 * @author Dennis Hofs (RRD)
 */
public class ErrorCode {
	public static final String AUTH_TOKEN_NOT_FOUND = "AUTH_TOKEN_NOT_FOUND";
	public static final String AUTH_TOKEN_INVALID = "AUTH_TOKEN_INVALID";
	public static final String AUTH_TOKEN_EXPIRED = "AUTH_TOKEN_EXPIRED";
	public static final String INVALID_INPUT = "INVALID_INPUT";
	public static final String INVALID_CREDENTIALS = "INVALID_CREDENTIALS";
}
