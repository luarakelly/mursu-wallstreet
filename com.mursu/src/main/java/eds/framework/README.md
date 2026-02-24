# Framework Components

**Clock**

Clock stores the current simulation time. There is only one Clock instance in the system (Singleton). It's updated when the next event goes to processing.

Usage:

``
Clock.getInstance().setTime(event.getTime());
double now = Clock.getInstance().getTime();
``

**Event**

Event represents an action scheduled at a specific time. Each event contains the event type, the scheduled time of happening, and the order related to that event.
Event objects are immutable. When a step in the process finishes, a new event is created instead of modifying an existing one.

Usage:

``
Event event = new Event(EventType.ARRIVAL, 1.2, order); // 1.2 is an event scheduled time
eventList.add(event);
``

**EventList**

EventList stores all future events. It uses a PriorityQueue to always process the earliest event first. This guarantees correct chronological execution.
PriorityQueue is used instead of a regular list because the simulation must always retrieve the event with the smallest time value. The queue automatically keeps events ordered by time.

Usage :

``
Event nextEvent = eventList.remove();
if (!eventList.isEmpty()) {
    double nextEvent = eventList.getNextTime();
}
``

**EventType and IEventType**

EventType defines the possible types of events in the exchange simulation such as

``
ARRIVAL,
VALIDATION_COMPLETE,
MATCHING_COMPLETE,
EXECUTION_COMPLETE,
SIMULATION_END
``
Maybe some of them are not needed. We'll see. IEventType is an interface used by the framework so that the event system does not depend on a specific simulation model.

**ServicePoint**

ServicePoint represents one processing stage in the exchange.
It simulates processing time for an order. It holds the order for a randomly generated duration. When the processing time expires, the order is returned to the simulation logic, which then decides what to do next, for example, determine the order type or start the matching process.
Each ServicePoint contains a queue of orders waiting to be processed. Only one order can be processed at a time. When processing starts, a completion event is scheduled using an exponential distribution.

ServicePoint provides two constructor options:
1.	A constructor that accepts any generator from outside. This allows full control over how the processing time distribution is configured.
2.	A constructor with preset exponential distribution that accepts only the mean processing time from a user. In this case, the ServicePoint creates the Negexp generator internally using the provided mean value.

Usage:

``
double meanServiceTime = 1.0;
ServicePoint sp = new ServicePoint(meanServiceTime, eventList, EventType.VALIDATION_COMPLETE);
sp.add(order);
``
``
// When processing finishes:
Order finishedOrder = sp.finishService();
``

Trace

Trace is a simple logging utility. It allows printing messages based on a selected trace level. It was in the teacher files, I edited it a bit, not sure if we use it.

Usage:

``
Trace.setTraceLevel(Trace.Level.INFO);
Trace.out(Trace.Level.INFO, "Order arrived");
``

How it all runs (how I understand it):

1. The user provides simulation parameters (for these framework elements it's at least meanServiceTime for exponential distribution).
2. We create service point.
3. We add the first arrival event to EventList.
4. The main loop repeatedly removes the earliest event.
5. Clock is updated to the event time.
6. The event is processed and may schedule new future events.
7. The simulation stops when no events remain or when the configured end time is reached.
