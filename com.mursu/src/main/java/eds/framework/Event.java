package eds.framework;

import eds.model.Order;

/**
 * Represents a simulation event.
 *
 * An Event contains a type of event, the simulation time at which it occurs,
 * and the related Order. Events are comparable by time and are
 * stored in a priority queue.
 */
public class Event implements Comparable<Event> {

	// event type (see IEventType)
	private final IEventType type;

	// simulation time when the event is occured
	private final double time;

	// order associated with this event
	private final Order order;

	/**
	 * Creates a new Event.
	 *
	 * @param type  the type of the event
	 * @param time  the simulation time when the event occurs
	 * @param order the order associated with the event
	 */
	public Event(IEventType type, double time, Order order) {
		this.type = type;
		this.time = time;
		this.order = order;
	}

	/**
	 * Returns the event type.
	 *
	 * @return the event type
	 */
	public IEventType getType() {
		return type;
	}

	/**
	 * Returns the simulation time of the event.
	 *
	 * @return event execution time
	 */
	public double getTime() {
		return time;
	}

	/**
	 * Returns the order associated with this event.
	 *
	 * @return the related order
	 */
	public Order getOrder() {
		return order;
	}

	/**
	 * Compares this event with another event based on time.
	 * Used for ordering events in a priority queue.
	 *
	 * @param other the event to compare with
	 * @return a negative integer, zero, or a positive integer
	 *         as this event time is less than, equal to,
	 *         or greater than the specified event time
	 */
	@Override
	public int compareTo(Event other) {
		return Double.compare(this.time, other.time);
	}
}