package eds.framework;

import java.util.PriorityQueue;

/**
 * Maintains a list of simulation events ordered by execution time.
 *
 * Events are stored in a PriorityQueue and sorted according to
 * their natural ordering based on Event.compareTo.
 * The earliest event is always processed first.
 */
public class EventList {

	// priority queue ordered by event time
	private final PriorityQueue<Event> events = new PriorityQueue<>();

	/**
	 * Adds a new event to the event list.
	 *
	 * @param event the event to be added
	 */
	public void add(Event event) {
		events.add(event);
	}

	/**
	 * Removes and returns the earliest event in the list.
	 *
	 * @return the next event to be processed,
	 *         or null if the list is empty
	 */
	public Event remove() {
		return events.poll();
	}

	/**
	 * Returns the execution time of the next event.
	 *
	 * @return the time of the next scheduled event
	 * @throws IllegalStateException if the event list is empty
	 */
	public double getNextTime() {
		if (events.isEmpty()) {
			throw new IllegalStateException("EventList is empty");
		}
		return events.peek().getTime();
	}

	/**
	 * Checks whether the event list is empty.
	 *
	 * @return true if there are no events, false otherwise
	 */
	public boolean isEmpty() {
		return events.isEmpty();
	}
}