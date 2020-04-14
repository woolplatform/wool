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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import eu.woolplatform.utils.AppComponents;

/**
 * This class can be used to run jobs sequentially on a separate thread. A job
 * is only started after any previous job has been completed (including
 * notification of the listener) or the previous job has been cancelled. The
 * thread will only run as long as there are jobs in the queue.
 *
 * @author Dennis Hofs (RRD)
 */
public class SerialJobRunner {
	public static final int PRIORITY_NONE = 0;
	public static final int PRIORITY_LOW = 100;
	public static final int PRIORITY_MEDIUM = 200;
	public static final int PRIORITY_HIGH = 300;

	private final Object lock = new Object();
	private final String logtag;
	private Thread thread = null;
	private List<JobDetails> pendingJobs = new ArrayList<>();
	private JobDetails currentJob = null;

	public SerialJobRunner() {
		logtag = getClass().getSimpleName();
	}
	
	/**
	 * Returns all jobs that have been posted and have not yet been completed.
	 * This includes the current job and pending jobs.
	 * 
	 * @return the jobs
	 */
	public List<Job> getJobs() {
		synchronized (lock) {
			List<Job> result = new ArrayList<>();
			if (currentJob != null)
				result.add(currentJob.job);
			for (JobDetails job : pendingJobs) {
				result.add(job.job);
			}
			return result;
		}
	}

	/**
	 * Posts a job with priority {@link #PRIORITY_MEDIUM PRIORITY_MEDIUM}.
	 * 
	 * @param job the job
	 * @param listener the job listener or null
	 */
	public void postJob(Job job, JobListener listener) {
		postJob(job, PRIORITY_MEDIUM, listener);
	}

	/**
	 * Posts a job that will be run after previously posted jobs with the same
	 * or higher priority, but before posted jobs with lower priority.
	 * 
	 * <p>You may use one of the PRIORITY_* constants defined in this class. A
	 * higher number indicates a higher priority.</p>
	 * 
	 * @param job the job
	 * @param priority the priority
	 * @param listener the job listener or null
	 */
	public void postJob(Job job, int priority, JobListener listener) {
		synchronized (lock) {
			Integer index = null;
			for (int i = pendingJobs.size() - 1; index == null && i >= 0; i--) {
				JobDetails other = pendingJobs.get(i);
				if (other.priority >= priority)
					index = i + 1;
			}
			if (index == null)
				index = 0;
			pendingJobs.add(index, new JobDetails(job, priority, listener));
			if (thread == null) {
				thread = new Thread() {
					@Override
					public void run() {
						runThread();
					}
				};
				thread.start();
			}
		}
	}

	/**
	 * Cancels all running and pending jobs.
	 */
	public void cancelJobs() {
		synchronized (lock) {
			final List<JobDetails> cancelledJobs = new ArrayList<>();
			if (currentJob != null) {
				JobDetails details = currentJob;
				currentJob = null;
				details.job.cancel();
				cancelledJobs.add(details);
			}
			for (JobDetails details : pendingJobs) {
				details.job.cancel();
				cancelledJobs.add(details);
			}
			pendingJobs.clear();
			new Thread() {
				@Override
				public void run() {
					notifyJobsCancelled(cancelledJobs);
				}
			}.start();
		}
	}

	/**
	 * Cancels the specified jobs.
	 * 
	 * @param jobs the jobs
	 */
	public void cancelJobs(Job... jobs) {
		cancelJobs(Arrays.asList(jobs));
	}

	/**
	 * Cancels the specified jobs.
	 * 
	 * @param jobs the jobs
	 */
	public void cancelJobs(List<? extends Job> jobs) {
		synchronized (lock) {
			final List<JobDetails> cancelledJobs = new ArrayList<>();
			for (Job job : jobs) {
				JobDetails details = findPendingJob(job);
				if (details != null) {
					pendingJobs.remove(details);
					details.job.cancel();
					cancelledJobs.add(details);
				}
				if (currentJob != null && currentJob.job == job) {
					details = currentJob;
					currentJob = null;
					job.cancel();
					cancelledJobs.add(details);
				}
			}
			new Thread() {
				@Override
				public void run() {
					notifyJobsCancelled(cancelledJobs);
				}
			}.start();
		}
	}

	private JobDetails findPendingJob(Job job) {
		synchronized (lock) {
			for (JobDetails other : pendingJobs) {
				if (other.job == job)
					return other;
			}
			return null;
		}
	}

	/**
	 * Posts the specified runnable to be run on the notify thread. In Android
	 * it will run on the UI thread. This method does not wait until the
	 * runnable is completed.
	 *
	 * @param runnable the runnable
	 */
	protected void postOnNotifyThread(Runnable runnable) {
		runnable.run();
	}

	/**
	 * Runs the specified runnable on the notify thread and waits until it is
	 * completed. In Android it will run on the UI thread. This method should
	 * always be called from a worker thread to avoid a deadlock.
	 *
	 * @param runnable the runnable
	 */
	protected void runOnNotifyThread(Runnable runnable) {
		runnable.run();
	}

	private void notifyJobCompleted(JobDetails job) {
		if (job.listener != null)
			job.listener.jobCompleted(job.job);
	}

	private void notifyJobsCancelled(final List<JobDetails> jobs) {
		postOnNotifyThread(new Runnable() {
			@Override
			public void run() {
				for (JobDetails job : jobs) {
					if (job.listener != null)
						job.listener.jobCancelled(job.job);
				}
			}
		});
	}

	private void runThread() {
		while (true) {
			final JobDetails job;
			synchronized (lock) {
				if (pendingJobs.isEmpty()) {
					thread = null;
					return;
				}
				job = pendingJobs.remove(0);
				currentJob = job;
			}
			try {
				job.job.run();
			} catch (RuntimeException ex) {
				Logger logger = AppComponents.getLogger(logtag);
				logger.error("UNEXPECTED ERROR: " + ex.getMessage(), ex);
				synchronized (lock) {
					if (currentJob == job)
						currentJob = null;
				}
				throw ex;
			} finally {
				runOnNotifyThread(new Runnable() {
					@Override
					public void run() {
						finishCompletedJob(job);
					}
				});
			}
		}
	}

	private void finishCompletedJob(JobDetails job) {
		boolean notify = false;
		synchronized (lock) {
			if (currentJob == job) {
				notify = true;
				currentJob = null;
			}
		}
		if (notify)
			notifyJobCompleted(job);
	}

	private class JobDetails {
		public Job job;
		public int priority;
		public JobListener listener;

		public JobDetails(Job job, int priority, JobListener listener) {
			this.job = job;
			this.priority = priority;
			this.listener = listener;
		}
	}
}
