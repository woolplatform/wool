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
 * This class represents a remote Bluetooth device.
 * 
 * @author Dennis Hofs
 */
public interface BluetoothDevice {
	/**
	 * Returns the address of the Bluetooth device. The address is given in the
	 * format 00:00:00:00:00:00. That is 12 hexadecimal characters in groups of
	 * two.
	 * 
	 * @return the address
	 */
	public String getAddress();
	
	/**
	 * Returns the friendly name of the Bluetooth device. This method does not
	 * communicate with the device, but only returns the name that was
	 * retrieved earlier by the Bluetooth system. If no friendly name is known,
	 * this method returns null.
	 * 
	 * @return the friendly name or null
	 */
	public String getFriendlyName();
	
	/**
	 * Communicates with the Bluetooth device to get its friendly name. If the
	 * device doesn't report a friendly name, this method returns null. If an
	 * error occurs during the communication, this method throws an exception.
	 * 
	 * @return the friendly name or null
	 * @throws IOException if an error occurs during the communication with the
	 * device
	 */
	public String readFriendlyName() throws IOException;
	
	/**
	 * Connects to the specified service on this remote Bluetooth device. The
	 * UUID should be given in the format 00000000-0000-0000-0000-000000000000.
	 * That is 32 hexadecimal characters in four groups with lengths
	 * 8 - 4 - 4 - 4 - 12.
	 * 
	 * <p>The common serial port service has this UUID:
	 * 00001101-0000-1000-8000-00805F9B34FB</p>
	 * 
	 * @param uuid the UUID of the service
	 * @return the Bluetooth socket
	 * @throws IOException if an error occurs while trying to connect
	 */
	public BluetoothSocket connectToService(String uuid) throws IOException;
}
