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

package eu.woolplatform.utils.schedule;

import org.slf4j.Logger;

import eu.woolplatform.utils.AppComponents;

public class WaitJobRunner {
	private Job waitJob;
	private String logtag;
	private long waitLogDelay;
	private String waitLogMsg;
	private long timeoutDelay;
	private String timeoutLogMsg;
	
	private boolean finished = false;
	private boolean cancelled = false;
	private final Object lock = new Object();
	
	public WaitJobRunner(Job waitJob, String logtag, long waitLogDelay,
			String waitLogMsg, long timeoutDelay, String timeoutLogMsg) {
		this.waitJob = waitJob;
		this.logtag = logtag;
		this.waitLogDelay = waitLogDelay;
		this.waitLogMsg = waitLogMsg;
		this.timeoutDelay = timeoutDelay;
		this.timeoutLogMsg = timeoutLogMsg;
	}
	
	public void cancel() {
		synchronized (lock) {
			cancelled = true;
			lock.notifyAll();
		}
		waitJob.cancel();
	}
	
	public boolean run() {
		long start = System.currentTimeMillis();
		new Thread() {
			@Override
			public void run() {
				waitJob.run();
				synchronized (lock) {
					finished = true;
					lock.notifyAll();
				}
			}
		}.start();
		Logger logger = AppComponents.getLogger(logtag);
		synchronized (lock) {
			if (waitLogDelay < timeoutDelay) {
				long end = start + waitLogDelay;
				long now = start;
				while (!finished && !cancelled && now < end) {
					try {
						lock.wait(end - now);
					} catch (InterruptedException ex) {
						throw new RuntimeException(ex.getMessage(), ex);
					}
					now = System.currentTimeMillis();
				}
				if (cancelled)
					return false;
				if (finished)
					return true;
				logger.info(waitLogMsg);
			}
			long end = start + timeoutDelay;
			long now = System.currentTimeMillis();
			while (!finished && !cancelled && now < end) {
				try {
					lock.wait(end - now);
				} catch (InterruptedException ex) {
					throw new RuntimeException(ex.getMessage(), ex);
				}
				now = System.currentTimeMillis();
			}
			if (cancelled)
				return false;
			if (finished)
				return true;
			logger.info(timeoutLogMsg);
			return false;
		}
	}
}
