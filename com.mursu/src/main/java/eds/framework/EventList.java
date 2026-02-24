package eds.framework;

import java.util.PriorityQueue;

public class EventList {
	private PriorityQueue<Event> lista = new PriorityQueue<Event>();
	
	public EventList() {
	}
	
	public Event remove(){
		return lista.remove();
	}
	
	public void add(Event t){
		lista.add(t);
	}
	
	public double getNextTime(){
		return lista.peek().getTime();
	}
	
	
}
