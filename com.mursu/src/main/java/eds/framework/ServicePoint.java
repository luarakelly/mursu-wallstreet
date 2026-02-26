package eds.framework;

import eduni.distributions.Negexp;
import java.util.LinkedList;
import eds.model.Order;

/**
 * Represents a service point.
 *
 * A ServicePoint maintains a queue of orders and processes them
 * one at a time. Service times are generated using an exponential
 * distribution (Negexp) by default. When service is completed, a new event
 * is scheduled into the EventList.
 */
public class ServicePoint {

	// queue of orders waiting for service
	private final LinkedList<Order> queue = new LinkedList<>();

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
	 * Adds an order to the service queue.
	 * If the service point is idle, processing starts immediately.
	 *
	 * @param order the order to be added
	 */
	public void add(Order order) {
		queue.add(order);
		if (!isBusy()) {
			startService();
		}
	}

	/**
	 * Completes service for the current order.
	 * If there are remaining orders in the queue,
	 * processing of the next order begins automatically.
	 *
	 * @return the order that has finished service,
	 *         or null if the queue was empty
	 */
	public Order finishService() {
		Order finished = queue.poll();
		busy = false;

		if (hasOrders()) {
			startService();
		}

		return finished;
	}

	/**
	 * Returns whether the service point is currently busy.
	 *
	 * @return true if processing an order, false otherwise
	 */
	public boolean isBusy() {
		return busy;
	}

	/**
	 * Checks whether there are orders waiting in the queue.
	 *
	 * @return true if the queue is not empty
	 */
	public boolean hasOrders() {
		return !queue.isEmpty();
	}

	/**
	 * Starts service for the next order in the queue.
	 * Generates a service completion event and schedules it
	 * in the EventList.
	 */
	private void startService() {
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