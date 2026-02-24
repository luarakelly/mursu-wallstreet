package simulation;

import eds.framework.IEventType;

public class Event implements Comparable<Event> {
	private IEventType type;
	private double time;
	
	public Event(IEventType type, double time) {
		this.type = type;
		this.time = time;
	}
	
	public void setType(IEventType type) {
		this.type = type;
	}
	public IEventType getType() {
		return type;
	}
	public void setTime(double time) {
		this.time = time;
	}
	public double getTime() {
		return time;
	}

	@Override
	public int compareTo(Event arg) {
		if (this.time < arg.time) return -1;
		else if (this.time > arg.time) return 1;
		return 0;
	}
}
