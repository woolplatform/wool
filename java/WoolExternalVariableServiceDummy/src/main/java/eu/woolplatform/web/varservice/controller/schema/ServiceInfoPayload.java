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

package eu.woolplatform.web.varservice.controller.schema;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * A {@link ServiceInfoPayload} object may be used to consolidate certain metadata
 * about this WOOL External Variable Service.
 *
 * @author Harm op den Akker
 */
public class ServiceInfoPayload {

	@Schema(description = "A string describing the date and time when this service was built",
			example = "09/22/2022 16:21:51")
	private String build;

	@Schema(description = "The latest supported API protocol version",
			example = "1")
	private String protocolVersion;

	@Schema(description = "The software version of the service",
			example = "1.0.0")
	private String serviceVersion;

	@Schema(description = "The time how long the service has been running",
			example = "1d 12h 34m")
	private String upTime;

	// --------------------------------------------------------
	// -------------------- Constructor(s) --------------------
	// --------------------------------------------------------

	/**
	 * Creates an instance of an empty {@link ServiceInfoPayload}.
	 */
	public ServiceInfoPayload() { }

	/**
	 * Creates an instance of a {@link ServiceInfoPayload} with a given {@code build}, {@code
	 * protocolVersion}, and {@code serviceVersion} {@link String}s that provide information about
	 * the currently running WOOL External Variable Service.
	 * @param build the date and time when this service was built as a {@link String}.
	 * @param protocolVersion the latest supported API protocol version as a {@link String}.
	 * @param serviceVersion the software version of the service as a {@link String}.
	 * @param upTime the time for how long the service has been running as a String (Xd Yh Zm)
	 */
	public ServiceInfoPayload(String build, String protocolVersion, String serviceVersion,
							  String upTime) {
		this.build = build;
		this.protocolVersion = protocolVersion;
		this.serviceVersion = serviceVersion;
		this.upTime = upTime;
	}

	// -----------------------------------------------------------
	// -------------------- Getters & Setters --------------------
	// -----------------------------------------------------------

	/**
	 * Returns the date and time when this service was built as a {@link String}.
	 * @return the date and time when this service was built as a {@link String}.
	 */
	public String getBuild() {
		return build;
	}

	/**
	 * Sets the date and time when this service was built as a {@link String}.
	 * @param build the date and time when this service was built as a {@link String}.
	 */
	public void setBuild(String build) {
		this.build = build;
	}

	/**
	 * Returns the latest supported API protocol version as a {@link String}.
	 * @return the latest supported API protocol version as a {@link String}.
	 */
	public String getProtocolVersion() {
		return protocolVersion;
	}

	/**
	 * Sets the latest supported API protocol version as a {@link String}.
	 * @param protocolVersion the latest supported API protocol version as a {@link String}.
	 */
	public void setProtocolVersion(String protocolVersion) {
		this.protocolVersion = protocolVersion;
	}

	/**
	 * Returns the software version of the service as a {@link String}.
	 * @return the software version of the service as a {@link String}.
	 */
	public String getServiceVersion() {
		return serviceVersion;
	}

	/**
	 * Sets the software version of the service as a {@link String}.
	 * @param serviceVersion the software version of the service as a {@link String}.
	 */
	public void setServiceVersion(String serviceVersion) {
		this.serviceVersion = serviceVersion;
	}

	/**
	 * Returns a string representation of how long this service has been running (e.g. Xd Yh Zm).
	 * @return a string representation of how long this service has been running (e.g. Xd Yh Zm).
	 */
	public String getUpTime() {
		return upTime;
	}

	/**
	 * Sets a string representation of how long this service has been running (e.g. Xd Yh Zm).
	 * @param upTime a string representation of how long this service has been running
	 *               (e.g. Xd Yh Zm).
	 */
	public void setUpTime(String upTime) {
		this.upTime = upTime;
	}
}
