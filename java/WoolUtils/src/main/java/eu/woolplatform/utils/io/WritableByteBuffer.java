package eu.woolplatform.utils.io;

/**
 * A byte buffer provides convenient methods to manipulate a byte array.
 * 
 * @author Dennis Hofs (RRD)
 */
public class WritableByteBuffer {
	private byte[] buffer;
	private int start;
	private int size;
	
	/**
	 * Constructs a new byte buffer. It will use a byte array with the
	 * specified capacity. The capacity will automatically grow if more space
	 * is needed.
	 * 
	 * @param capacity the initial capacity
	 */
	public WritableByteBuffer(int capacity) {
		buffer = new byte[capacity];
		start = 0;
		size = 0;
	}
	
	/**
	 * Constructs a new byte buffer and copies the content from another byte
	 * buffer.
	 * 
	 * @param other the other byte buffer
	 */
	public WritableByteBuffer(WritableByteBuffer other) {
		buffer = new byte[other.buffer.length];
		for (int i = 0; i < buffer.length; i++) {
			buffer[i] = other.buffer[i];
		}
		start = other.start;
		size = other.size;
	}
	
	/**
	 * Returns the byte at the specified index.
	 * 
	 * @param index the index
	 * @return the byte
	 */
	public byte get(int index) {
		if (index >= size) {
			throw new IndexOutOfBoundsException("Invalid index " + index +
					" in byte buffer with size " + size);
		}
		int bufferIndex = (start + index) % buffer.length;
		return buffer[bufferIndex];
	}
	
	/**
	 * Returns the size of this buffer. This is not the capacity.
	 * 
	 * @return the size
	 */
	public int size() {
		return size;
	}
	
	/**
	 * Removes all bytes from the buffer.
	 */
	public void clear() {
		start = 0;
		size = 0;
	}
	
	/**
	 * Appends the specified byte to the end of the buffer.
	 * 
	 * @param b the byte
	 */
	public void append(byte b) {
		if (size == buffer.length)
			increaseBuffer();
		int index = (start + size) % buffer.length;
		buffer[index] = b;
		size++;
	}
	
	/**
	 * Appends bytes from the specified byte array to the end of this buffer.
	 * 
	 * @param bs the byte array
	 * @param off the index of the first byte to append
	 * @param len the number of bytes to append
	 */
	public void append(byte[] bs, int off, int len) {
		int newSize = size + len;
		while (buffer.length < newSize) {
			increaseBuffer();
		}
		int index = (start + size) % buffer.length;
		for (int i = 0; i < len; i++) {
			buffer[index] = bs[off + i];
			index++;
			if (index == buffer.length)
				index = 0;
		}
		size = newSize;
	}
	
	/**
	 * Appends bytes from the specified byte buffer to the end of this buffer.
	 * 
	 * @param other the byte buffer
	 * @param off the index of the first byte to append
	 * @param len the number of bytes to append
	 */
	public void append(WritableByteBuffer other, int off, int len) {
		int newSize = size + len;
		while (buffer.length < newSize) {
			increaseBuffer();
		}
		int index = (start + size) % buffer.length;
		int otherIndex = (other.start + off) % other.buffer.length;
		for (int i = 0; i < len; i++) {
			buffer[index] = other.buffer[otherIndex];
			index++;
			if (index == buffer.length)
				index = 0;
			otherIndex++;
			if (otherIndex == other.buffer.length)
				otherIndex = 0;
		}
		size = newSize;
	}
	
	/**
	 * Removes the first <i>n</i> bytes from this buffer.
	 * 
	 * @param n the number of bytes to remove
	 */
	public void removeHead(int n) {
		if (n > size) {
			throw new IndexOutOfBoundsException("Can't remove " + n +
					" bytes from byte buffer with size " + size);
		}
		size -= n;
		if (size == 0)
			start = 0;
		else
			start = (start + n) % buffer.length;
	}
	
	/**
	 * Removes the first byte from this buffer and returns it.
	 * 
	 * @return the removed byte
	 */
	public byte removeFirst() {
		byte b = get(0);
		removeHead(1);
		return b;
	}
	
	/**
	 * Doubles the capacity of the buffer.
	 */
	private void increaseBuffer() {
		byte[] newBuffer = new byte[2 * buffer.length];
		int oldIndex = start;
		for (int i = 0; i < size; i++) {
			newBuffer[i] = buffer[oldIndex];
			oldIndex++;
			if (oldIndex == buffer.length)
				oldIndex = 0;
		}
		buffer = newBuffer;
		start = 0;
	}
	
	/**
	 * Returns a byte array with the contents of this buffer.
	 * 
	 * @return the byte array
	 */
	public byte[] toByteArray() {
		byte[] result = new byte[size];
		int index = start;
		for (int i = 0; i < size; i++) {
			result[i] = buffer[index];
			index++;
			if (index == buffer.length)
				index = 0;
		}
		return result;
	}
	
	@Override
	public Object clone() {
		return new WritableByteBuffer(this);
	}
}
