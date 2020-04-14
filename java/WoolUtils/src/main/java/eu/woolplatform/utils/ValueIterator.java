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

package eu.woolplatform.utils;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class ValueIterator implements Iterator<Float> {
	private float start;
	private float end;
	private float step;

	private int nextIndex = 0;
	private Float next;

	public ValueIterator(float start, float end, float step) {
		this.start = start;
		this.end = end;
		this.step = step;
		if (step == 0)
			throw new RuntimeException("Step cannot be 0");
		reset();
	}

	public void reset() {
		nextIndex = 0;
		if ((step > 0 && start <= end) || (step < 0 && start >= end))
			next = start;
		else
			next = null;
	}

	@Override
	public boolean hasNext() {
		return next != null;
	}

	@Override
	public Float next() {
		if (next == null)
			throw new NoSuchElementException("End of range");
		float result = next;
		nextIndex++;
		next = start + nextIndex * step;
		if ((step > 0 && next > end) || (step < 0 && next < end))
			next = null;
		return result;
	}
}
