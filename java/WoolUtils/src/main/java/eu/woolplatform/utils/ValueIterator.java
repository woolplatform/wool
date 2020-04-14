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
