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
