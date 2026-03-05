package eds.framework;

import eduni.distributions.Negexp;
import java.util.LinkedList;
import eds.framework.ISimulationEntity;

/**
 * Represents a service point.
 *
 * A ServicePoint maintains a queue of simulation entities and processes them
 * one at a time. Service times are generated using an exponential
 * distribution (Negexp) by default. When service is completed, a new event
 * is scheduled into the EventList.
 */
public class ServicePoint {

	// queue of entities waiting for service
	private final LinkedList<ISimulationEntity> queue = new LinkedList<>();

	// reference to the simulation event list
	private final EventList eventList;

	// event type generated when service completes
	private final IEventType completionType;

	// default service time generator (exponential distribution)
	private final Negexp generator;

	// indicates whether the service point is currently busy
	private boolean busy = false;

	/**
	 * Creates a ServicePoint with an externally provided
	 * service time generator.
	 *
	 * @param generator       service time generator
	 * @param eventList       reference to the event list
	 * @param completionType  event type triggered at service completion
	 */
	public ServicePoint(Negexp generator, EventList eventList, IEventType completionType) {
		this.generator = generator;
		this.eventList = eventList;
		this.completionType = completionType;
	}

	/**
	 * Creates a ServicePoint using an exponential distribution
	 * with the given mean service time.
	 *
	 * @param meanServiceTime mean service time for Negexp distribution
	 * @param eventList       reference to the event list
	 * @param completionType  event type triggered at service completion
	 */
	public ServicePoint(double meanServiceTime, EventList eventList, IEventType completionType) {
		this.generator = new Negexp(meanServiceTime);
		this.eventList = eventList;
		this.completionType = completionType;
	}

	/**
	 * Adds a simulation entity to the service queue.
	 *
	 * @param entity the entity to be added
	 */
	public void add(ISimulationEntity entity) {
		queue.add(entity);
	}

	/**
	 * Completes service for the current simulation entity.
	 *
	 * @return the entity that has finished service,
	 *         or null if the queue was empty
	 */
	public ISimulationEntity finishService() {
		ISimulationEntity finished = queue.poll();
		busy = false;
		return finished;
	}

	/**
	 * Returns whether the service point is currently busy.
	 *
	 * @return true if processing an entity, false otherwise
	 */
	public boolean isBusy() {
		return busy;
	}

	/**
	 * Checks whether there are entities waiting in the queue.
	 *
	 * @return true if the queue is not empty
	 */
	public boolean hasOrders() {
		return !queue.isEmpty();
	}

	/**
	 * Returns the current number of entities waiting in the queue.
	 * The entity currently in service (if any) is not counted as waiting.
	 *
	 * @return number of waiting entities
	 */
	public int getQueueLength() {
		return Math.max(0, queue.size() - (busy ? 1 : 0));
	}

	/**
	 * Returns the current internal queue size, including the entity in service.
	 *
	 * @return total number of queued entities
	 */
	public int getQueueSize() {
		return queue.size();
	}

	/**
	 * Starts service for the next entity in the queue.
	 * Generates a service completion event and schedules it
	 * in the EventList.
	 */
	public void startService() {
		if (queue.isEmpty()) {
			return;
		}

		busy = true;

		double serviceTime = generator.sample();
		double completionTime = Clock.getInstance().getTime() + serviceTime;

		Event event = new Event(completionType, completionTime, queue.peek());
		eventList.add(event);
	}
}
