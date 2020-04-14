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

package eu.woolplatform.utils.datetime;

import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

/**
 * The virtual clock can be used for simulations. It can run in system mode,
 * where it just returns the same time as the system clock, or it can run in
 * virtual mode, where the clock can run at a different speed and a different
 * anchor time.
 * 
 * <p>By default the virtual clock runs in system mode (virtual time equals
 * system time, speed is 1). When you set the virtual anchor time or speed to
 * another value, the clock will run in virtual mode.</p>
 * 
 * <p>The virtual anchor time is a point where a specified virtual time is
 * anchored to the current system time at that point. If the speed is 1, this
 * just defines a time shift.</p>
 * 
 * <p>Example with speed 1:<br>
 * We may set the virtual anchor time to 10:00 when the system time is 9:30.
 * This defines a time shift of 30 minutes in advance. When the system time
 * is 10:30, the virtual time is 11:00.</p>
 * 
 * <p>Example with speed 2:<br>
 * Again we set the virtual anchor time to 10:00 when the system time is 9:30.
 * When the system time is 10:30, one hour in system time has elapsed since the
 * anchor time. Because of speed 2, that is 2 hours in virtual time, which
 * we add to the virtual anchor time. So at system time 10:30, the virtual time
 * is 12:00.</p>
 * 
 * @author Dennis Hofs (RRD)
 */
public class VirtualClock {
	private final Object lock = new Object();
	private boolean systemMode = true;
	private long systemAnchorTime;
	private long virtualAnchorTime;
	private float speed;
	
	public VirtualClock() {
		systemAnchorTime = System.currentTimeMillis();
		virtualAnchorTime = systemAnchorTime;
		speed = 1;
	}
	
	/**
	 * Returns whether the clock is currently in virtual mode.
	 * 
	 * @return true if the clock is in virtual mode, false otherwise
	 */
	public boolean isVirtualMode() {
		synchronized (lock) {
			return !systemMode;
		}
	}
	
	/**
	 * Sets the virtual start time. This will anchor the specified virtual time
	 * to the current system time. If you set this to null, the current virtual
	 * time will be the same as the current system time. This still serves as
	 * an anchor when the speed is not 1.
	 * 
	 * @param time the virtual start time or null
	 */
	public void setVirtualAnchorTime(Date time) {
		synchronized (lock) {
			systemAnchorTime = System.currentTimeMillis();
			if (time != null)
				virtualAnchorTime = time.getTime();
			else
				virtualAnchorTime = systemAnchorTime;
			systemMode = virtualAnchorTime == systemAnchorTime && speed == 1;
		}
	}
	
	/**
	 * Sets the speed of the virtual clock. This will redefine an anchor at the
	 * current point in time. It will anchor the current virtual time to the
	 * current system time.
	 * 
	 * @param speed the speed
	 */
	public void setSpeed(float speed) {
		synchronized (lock) {
			long now = System.currentTimeMillis();
			long systemElapsed = now - systemAnchorTime;
			long virtualElapsed = this.speed == 1 ?
					systemElapsed :
					Math.round(systemElapsed * (double)this.speed);
			systemAnchorTime = now;
			virtualAnchorTime += virtualElapsed;
			this.speed = speed;
			systemMode = virtualAnchorTime == systemAnchorTime && speed == 1;
		}
	}
	
	/**
	 * Returns the current time in milliseconds since the epoch. This is a
	 * virtual time or the system time, depending on the current mode of the
	 * virtual clock.
	 * 
	 * @return the current time
	 */
	public long currentTimeMillis() {
		synchronized (lock) {
			long now = System.currentTimeMillis();
			if (systemMode)
				return now;
			long systemElapsed = now - systemAnchorTime;
			long virtualElapsed = speed == 1 ?
					systemElapsed :
					Math.round(systemElapsed * (double)speed);
			return virtualAnchorTime + virtualElapsed;
		}
	}
	
	/**
	 * Returns the current date.
	 * 
	 * @return the current date
	 */
	public LocalDate getDate() {
		return getTime().toLocalDate();
	}
	
	/**
	 * Returns the current time as a {@link DateTime DateTime}.
	 * 
	 * @return the current time
	 */
	public DateTime getTime() {
		return new DateTime(currentTimeMillis());
	}
}
