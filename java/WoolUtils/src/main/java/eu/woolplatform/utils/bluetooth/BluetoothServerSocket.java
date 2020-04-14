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
