package eds.framework;

import eds.model.SimulationEntity;

/**
 * Represents a simulation event.
 *
 * An Event contains a type of event, the simulation time at which it occurs,
 * and the related simulation entity. Events are comparable by time and are
 * stored in a priority queue.
 */
public class Event implements Comparable<Event> {

	// event type (see IEventType)
	private final IEventType type;

	// simulation time when the event is occured
	private final double time;

	// entity associated with this event
	private final SimulationEntity entity;

	/**
	 * Creates a new Event.
	 *
	 * @param type   the type of the event
	 * @param time   the simulation time when the event occurs
	 * @param entity the simulation entity associated with the event
	 */
	public Event(IEventType type, double time, SimulationEntity entity) {
		this.type = type;
		this.time = time;
		this.entity = entity;
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
	 * Returns the simulation entity associated with this event.
	 *
	 * @return the related simulation entity
	 */
	public SimulationEntity getEntity() {
		return entity;
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