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

/**
 * A Bluetooth server socket is used to provide a service on the local
 * Bluetooth device to other devices.
 * 
 * @author Dennis Hofs
 */
public interface BluetoothServerSocket {
	/**
	 * Waits for a remote Bluetooth device to connect to this server socket.
	 * This method blocks until a remote device connects. Then it returns
	 * a Bluetooth socket that can be used to communicate with the remote
	 * device. This method waits for indefinite time.
	 * 
	 * @return the Bluetooth socket
	 * @throws IOException if an error occurs while waiting
	 */
	public BluetoothSocket accept() throws IOException;

	/**
	 * Waits for a remote Bluetooth device to connect to this server socket.
	 * This method blocks until a remote device connects. Then it returns
	 * a Bluetooth socket that can be used to communicate with the remote
	 * device. This method waits no longer than the specified timeout.
	 * 
	 * @param timeout the timeout in seconds
	 * @return the Bluetooth socket
	 * @throws IOException if an error occurs while waiting
	 */
	public BluetoothSocket accept(int timeout) throws IOException;
	
	
	/**
	 * Closes this server socket.
	 */
	public void close();
}
