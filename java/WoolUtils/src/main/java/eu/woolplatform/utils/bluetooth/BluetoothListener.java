package eu.woolplatform.utils.bluetooth;

/**
 * This listener can be specified when you try to enable or disable Bluetooth.
 * It will be notified when the process is finished.
 * 
 * @author Dennis Hofs
 */
public interface BluetoothListener {
	
	/**
	 * Called when the enable or disable process is finished.
	 * 
	 * @param btAdapter the Bluetooth adapter
	 * @param enabled true if Bluetooth is enabled after the process, false if
	 * it's disabled after the process
	 */
	public void bluetoothEnabled(BluetoothAdapter btAdapter, boolean enabled);
}
