/*
 * Copyright 2019 Roessingh Research and Development.
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

package eu.woolplatform.utils.bluetooth;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A Bluetooth socket provides a way to communicate with a remote Bluetooth
 * device.
 * 
 * @author Dennis Hofs
 */
public interface BluetoothSocket {
	
	/**
	 * Returns the address of the remote Bluetooth device. The address is
	 * returned in the format 00:00:00:00:00:00. That is 12 hexadecimal
	 * characters in groups of two.
	 * 
	 * @return the address of the remote Bluetooth device
	 */
	public String getRemoteAddress();

	/**
	 * Returns the input stream to read data from the remote device.
	 * 
	 * @return the input stream
	 * @throws IOException if the input stream can't be opened
	 */
	public InputStream getInputStream() throws IOException;
	
	/**
	 * Returns the output stream to write data to the remote device.
	 * 
	 * @return the output stream
	 * @throws IOException if the output stream can't be opened
	 */
	public OutputStream getOutputStream() throws IOException;
	
	/**
	 * Closes the connection with the remote device.
	 */
	public void close();
}
