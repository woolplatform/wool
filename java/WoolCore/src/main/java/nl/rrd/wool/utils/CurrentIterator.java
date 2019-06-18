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

package nl.rrd.wool.utils;

import java.util.Iterator;

/**
 * This iterator can return the current element rather than the next element as
 * a normal iterator does. At construction it is positioned before the first
 * element. You can move to the first element with {@link #moveNext()
 * moveNext()}. Then you can access the current element as many times as needed
 * with {@link #getCurrent() getCurrent()}, and you can move to the next
 * element with {@link #moveNext() moveNext()}.
 * 
 * @author Dennis Hofs (RRD)
 *
 * @param <T> the type of elements in the iterator
 */
public class CurrentIterator<T> {
	private Iterator<? extends T> iterator;
	private T current = null;

	/**
	 * Constructs a new instance. The iterator will be positioned before the
	 * first element.
	 * 
	 * @param it the underlying iterator
	 */
	public CurrentIterator(Iterator<? extends T> it) {
		this.iterator = it;
	}
	
	/**
	 * Returns the current element. If the iterator is positioned before the
	 * first element or after the last element, this method returns null. An
	 * element itself may also be null. Use {@link #moveNext() moveNext()} to
	 * know the position.
	 * 
	 * @return the current element (can be null)
	 */
	public T getCurrent() {
		return current;
	}

	/**
	 * Moves to the next element. If there is no more element, this method
	 * returns false.
	 * 
	 * @return true if the iterator is at the next element, false if there is
	 * no more element
	 */
	public boolean moveNext() {
		if (!iterator.hasNext()) {
			current = null;
			return false;
		} else {
			current = iterator.next();
			return true;
		}
	}
	
	/**
	 * Removes the current element and moves to the next element. If there is
	 * no more element, this method returns false.
	 * 
	 * @return true if the iterator is at the next element, false if there is no
	 * more element
	 */
	public boolean removeMoveNext() {
		iterator.remove();
		return moveNext();
	}
}
