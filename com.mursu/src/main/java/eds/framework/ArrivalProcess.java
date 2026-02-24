package eds.framework;
import eds.model.EventType;
import eduni.distributions.ContinuousGenerator;

public class ArrivalProcess {
	private ContinuousGenerator generator;
	private EventList eventList;
	private EventType type;

	public ArrivalProcess(ContinuousGenerator g, EventList tl, EventType type) {
		this.generator = g;
		this.eventList = tl;
		this.type = type;
	}

	public void generateNext() {
		Event t = new Event(type, Clock.getInstance().getTime() + generator.sample());
		eventList.add(t);
	}

}
