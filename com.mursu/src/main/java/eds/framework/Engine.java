package eds.framework;

import controller.IControllerMtoV;

public abstract class Engine extends Thread implements IEngine {  // NEW DEFINITIONS
	// Simulaation kesto
	private double simulationTime = 0;	// time when the simulation will be stopped
	// Viive
	private long delay = 0;
	private Clock clock;				// in order to simplify the code (clock.getClock() instead Clock.getInstance().getClock())
	
	protected EventList eventList;
	protected ServicePoint[] servicePoints;
	protected IControllerMtoV controller; // NEW

	public Engine(IControllerMtoV controller) {	// NEW
		this.controller = controller;  			// NEW
		clock = Clock.getInstance();
		eventList = new EventList();
	}

	@Override
	public void setSimulationTime(double time) {
		simulationTime = time;
	}
	
	@Override // NEW
	public void setDelay(long time) {
		this.delay = time;
	}
	
	@Override // NEW
	public long getDelay() {
		return delay;
	}
	
	@Override
	public void run() {
		// Aloitetaas
		initialization(); // creating, e.g., the first event

		while (simulate()){
			delay(); // NEW
			clock.setTime(currentTime());
			runBEvents();
			tryCEvents();
		}

		results();
	}
	
	private void runBEvents() {
		while (eventList.getNextTime() == clock.getTime()){
			runEvent(eventList.remove());
		}
	}

	private void tryCEvents() {    // define protected, if you want to overwrite
		for (ServicePoint p: servicePoints){
			if (!p.isBusy() && p.hasOrders()){
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

	private void delay() { // NEW
		Trace.out(Trace.Level.INFO, "Delay " + delay);
		try {
			sleep(delay);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	protected abstract void initialization(); 	// Defined in simu.model-package's class who is inheriting the Engine class
	protected abstract void runEvent(Event t);	// Defined in simu.model-package's class who is inheriting the Engine class
	protected abstract void results(); 			// Defined in simu.model-package's class who is inheriting the Engine class
}