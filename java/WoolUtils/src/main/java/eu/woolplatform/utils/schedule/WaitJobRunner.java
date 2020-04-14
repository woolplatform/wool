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
