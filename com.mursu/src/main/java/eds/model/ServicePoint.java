package eds.model;

import eduni.distributions.ContinuousGenerator;
import eds.framework.Clock;
import eds.framework.Event;
import eds.framework.EventList;

import java.util.LinkedList;

// TODO:
// Service Point functionalities & calculations (+ variables needed) and reporting to be implemented
public class ServicePoint {
	private LinkedList<Customer> jono = new LinkedList<Customer>(); // Data Structure used
	private ContinuousGenerator generator;
	private EventList eventList;
	private EventType eventTypeScheduled;
	//Queuestrategy strategy; // option: ordering of the customer
	private boolean reserved = false;

	public ServicePoint(ContinuousGenerator generator, EventList tapahtumalista, EventType tyyppi){
		this.eventList = tapahtumalista;
		this.generator = generator;
		this.eventTypeScheduled = tyyppi;
				
	}

	public void addQueue(Customer a){   // First customer at the queue is always on the service
		jono.add(a);
	}

	public Customer removeQueue(){		// Remove serviced customer
		reserved = false;
		return jono.poll();
	}

	public void beginService() {  		// Begins a new service, customer is on the queue during the service
		reserved = true;
		double serviceTime = generator.sample();
		eventList.add(new Event(eventTypeScheduled, Clock.getInstance().getTime()+serviceTime));
	}

	public boolean isReserved(){
		return reserved;
	}

	public boolean isOnQueue(){
		return jono.size() != 0;
	}
}
