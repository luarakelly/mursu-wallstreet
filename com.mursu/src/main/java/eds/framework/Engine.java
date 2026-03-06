package eds.framework;

import controller.IControllerMtoV;

public abstract class Engine extends Thread implements IEngine {
	// Simulaation kesto
	private double simulationTime = 0;	// time when the simulation will be stopped

	// Viive
	private long delay = 0;
	private final Clock clock;
	
	protected EventList eventList;
	protected ServicePoint[] servicePoints;
	protected IControllerMtoV controller; 

	public Engine(IControllerMtoV controller) {	
		this.controller = controller;  			
		clock = Clock.getInstance();
		eventList = new EventList();
	}

	@Override
	public void setSimulationTime(double time) {
		simulationTime = time;
	}
	
	@Override 
	public void setDelay(long time) {
		this.delay = time;
	}
	
	@Override 
	public long getDelay() {
		return delay;
	}
	
	@Override
	public void run() {
		// Aloitetaas
		initialization(); // creating he first event

		while (simulate()){
			delay(); 
			clock.setTime(currentTime());
			runBEvents();
			tryCEvents();
			afterCycle();
		}

		results();
	}
	
	private void runBEvents() {
		while (eventList.getNextTime() == clock.getTime()){
			runEvent(eventList.remove());
		}
	}

	/* Start service. Thanks Ksenia! ☝️*/
	private void tryCEvents() {
		for (ServicePoint p : servicePoints) {
			if (!p.isBusy() && p.hasOrders()) {
				p.startService();
			}
}
	}

	private double currentTime(){
		return eventList.getNextTime();
	}
	
	private boolean simulate() {
		Trace.out(Trace.Level.INFO, "Time is: " + clock.getTime());
		return clock.getTime() < simulationTime;
	}

	private void delay() {
		Trace.out(Trace.Level.INFO, "Delay " + delay);
		try {
			sleep(delay);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	protected abstract void initialization(); 	
	protected abstract void runEvent(Event t);	
	protected void afterCycle() {}
	protected abstract void results(); 			
}