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
