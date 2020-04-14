package eu.woolplatform.utils.bluetooth;

import java.io.IOException;

/**
 * This class provides a common interface to the Bluetooth system. Normally an
 * application sets an implementation with {@link
 * #setInstance(BluetoothAdapter) setInstance()}. Any software components that
 * need the Bluetooth system can then call {@link #getInstance() getInstance()}
 * to get access. There are two known implementations: for standard Java and
 * for Android. Before using the adapter, you may need to call {@link
 * #prepareThread() prepareThread()} and you must call {@link
 * #initDefaultAdapter() initDefaultAdapter()}.
 * 
 * @author Dennis Hofs
 */
public abstract class BluetoothAdapter {
	private static BluetoothAdapter instance = null;
	
	/**
	 * Returns a Bluetooth adapter implementation. It returns the adapter that
	 * was set with {@link #setInstance(BluetoothAdapter) setInstance()}. If no
	 * adapter was set, this method returns null.
	 *  
	 * @return the Bluetooth adapter
	 */
	public static BluetoothAdapter getInstance() {
		return instance;
	}
	
	/**
	 * Sets a Bluetooth adapater implementation. This adapter will be returned
	 * by {@link #getInstance() getInstance()}.
	 * 
	 * @param instance the Bluetooth adapter
	 */
	public static void setInstance(BluetoothAdapter instance) {
		BluetoothAdapter.instance = instance;
	}
	
	/**
	 * The Android implementation requires that <code>Looper.prepare()</code>
	 * is called on a thread before the Bluetooth adapter is used. If you
	 * create a new thread specifically for Bluetooth operations and you want
	 * that code to be independent of Android, you can call this method, so
	 * that the Android implementation will call <code>Looper.prepare()</code>.
	 * If you need this, you must call this method before {@link
	 * #initDefaultAdapter() initDefaultAdapter()}.
	 */
	public abstract void prepareThread();
	
	/**
	 * Initialises the Bluetooth adapter. This method must be called before
	 * using the adapter. The Android implementation requires that
	 * <code>Looper.prepare()</code> is called on a thread before the adapter
	 * is used. See {@link #prepareThread() prepareThread()} for more
	 * information. If you need this, you must call {@link #prepareThread()
	 * prepareThread()} before calling this method.
	 */
	public abstract void initDefaultAdapter();
	
	/**
	 * Enables the Bluetooth system. This method returns immediately and will
	 * notify the specified listener when the process has finished.
	 * 
	 * @param listener the listener 
	 */
	public abstract void enable(BluetoothListener listener);
	
	/**
	 * Disables the Bluetooth system. This method returns immediately and will
	 * notify the specified listener when the process has finished.
	 * 
	 * @param listener the listener 
	 */
	public abstract void disable(BluetoothListener listener);
	
	/**
	 * Returns the remote Bluetooth devices that have been paired with this
	 * local Bluetooth device.
	 * 
	 * @return the paired devices
	 */
	public abstract BluetoothDevice[] getPairedDevices();
	
	/**
	 * Starts the discovery of remote Bluetooth devices in the vicinity of this
	 * local Bluetooth device. Events in the discovery process are sent to the
	 * specified listener.
	 * 
	 * @param listener the listener
	 * @throws Exception if the discovery can't be started
	 */
	public abstract void discoverDevices(DiscoverDevicesListener listener)
		throws Exception;
	
	/**
	 * Cancels the discovery of remote Bluetooth devices.
	 */
	public abstract void cancelDiscoverDevices();
	
	/**
	 * Opens a Bluetooth server socket for the specified service on this local
	 * Bluetooth device. The UUID should be given in the format
	 * 00000000-0000-0000-0000-000000000000. That is 32 hexadecimal characters
	 * in four groups with lengths 8 - 4 - 4 - 4 - 12.
	 * 
	 * <p>The common serial port service has this UUID:
	 * 00001101-0000-1000-8000-00805F9B34FB</p>
	 * 
	 * @param uuid the UUID of the service
	 * @return the Bluetooth server socket
	 * @throws IOException if the socket can't be opened
	 */
	public abstract BluetoothServerSocket openServerSocket(String uuid)
		throws IOException;
	
	/**
	 * Returns true if the Bluetooth system is enabled.
	 * 
	 * @return true if the Bluetooth system is enabled, false otherwise
	 */
	public abstract boolean isEnabled();
	
	/**
	 * Returns the remote Bluetooth device with the specified address. The
	 * address should be given in the format 00:00:00:00:00:00. That is 12
	 * hexadecimal characters in groups of two.
	 * 
	 * @param address the address
	 * @return the remote device
	 * @throws Exception if the remote device can't be created (for example if
	 * Bluetooth is not available)
	 */
	public abstract BluetoothDevice getRemoteDevice(String address)
	throws Exception;
	
	/**
	 * Releases any resources used by this adapter.
	 */
	public abstract void close();
}
