package eu.woolplatform.utils.bluetooth;

/**
 * This listener can receive events during a device discovery process.
 * 
 * @author Dennis Hofs
 */
public interface DiscoverDevicesListener {
	/**
	 * Called when a remote Bluetooth device has been discovered.
	 * 
	 * @param adapter the Bluetooth adapter that is performing the discovery
	 * @param device the remote device that was discovered
	 */
	public void discoveredDevice(BluetoothAdapter adapter,
			BluetoothDevice device);
	
	/**
	 * Called when the discovery process is completed.
	 * 
	 * @param adapter the Bluetooth adapter that performed the discovery
	 */
	public void discoveryComplete(BluetoothAdapter adapter);
}
