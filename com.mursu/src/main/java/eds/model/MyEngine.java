package eds.model;

import controller.IControllerMtoV;
import eds.framework.*;
import eduni.distributions.Negexp;
import eduni.distributions.Normal;


public class MyEngine extends Engine {
	private ArrivalProcess arrivalProcess;

	public MyEngine(IControllerMtoV controller){ // NEW
		super(controller); // NEW
		
		servicePoints = new ServicePoint[3];
	
		servicePoints[0]=new ServicePoint(new Normal(10,6), eventList, EventType.DEP1);
		servicePoints[1]=new ServicePoint(new Normal(10,10), eventList, EventType.DEP2);
		servicePoints[2]=new ServicePoint(new Normal(5,3), eventList, EventType.DEP3);
		
		arrivalProcess = new ArrivalProcess(new Negexp(15,5), eventList, EventType.ARR1);
	}

	@Override
	protected void initialization() {
		arrivalProcess.generateNext();	 // First arrival in the system
	}

	@Override
	protected void runEvent(Event t) {  // B phase events
		Customer a;

		switch ((EventType)t.getType()){
		case ARR1:
			servicePoints[0].addQueue(new Customer());
			arrivalProcess.generateNext();
			controller.visualiseCustomer(); // NEW
			break;

		case DEP1:
			a = servicePoints[0].removeQueue();
			 servicePoints[1].addQueue(a);
			break;

		case DEP2:
			a = servicePoints[1].removeQueue();
			servicePoints[2].addQueue(a);
			break;

		case DEP3:
			a = servicePoints[2].removeQueue();
			a.setRemovalTime(Clock.getInstance().getTime());
			a.reportResults();
			break;
		}	
	}

	@Override
	protected void results() {
		// OLD text UI
		//System.out.println("Simulation ended at " + Clock.getInstance().getClock());
		//System.out.println("Results ... are currently missing");

		// NEW GUI
		controller.showEndTime(Clock.getInstance().getTime());
	}
}
