package eu.woolplatform.utils.schedule;

public interface JobListener {
	void jobCompleted(Job job);
	void jobCancelled(Job job);
}
