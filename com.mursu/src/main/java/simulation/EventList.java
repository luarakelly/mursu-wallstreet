package simulation;

import eds.framework.Event;

import java.util.PriorityQueue;

public class EventList {
	private PriorityQueue<eds.framework.Event> lista = new PriorityQueue<eds.framework.Event>();
	
	public EventList() {
	}
	
	public eds.framework.Event remove(){
		return lista.remove();
	}
	
	public void add(Event t){
		lista.add(t);
	}
	
	public double getNextTime(){
		return lista.peek().getTime();
	}
	
	
}
