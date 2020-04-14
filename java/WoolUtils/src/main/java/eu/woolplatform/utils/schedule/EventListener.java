package eu.woolplatform.utils.schedule;

/**
 * General purpose event listener for events.
 * 
 * @author Dennis Hofs (RRD)
 *
 * @param <T> the type of event
 */
public interface EventListener<T> {

	/**
	 * Called when an event occurs.
	 * 
	 * @param event the event
	 */
	void onEvent(T event);
}
