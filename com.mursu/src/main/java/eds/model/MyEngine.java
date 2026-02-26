package eds.model;

import controller.IControllerMtoV;
import eds.framework.ArrivalProcess;
import eds.framework.Clock;
import eds.framework.Engine;
import eds.framework.Event;
import eds.framework.ServicePoint;
import eduni.distributions.Negexp;



public class MyEngine extends Engine {
	// Toimeksiannon saapuminen
	private ArrivalProcess arrivalProcess;

	public MyEngine(IControllerMtoV controller){
		super(controller);
		
		// Luodaan palvelupisteet
		servicePoints = new ServicePoint[3];
	
		servicePoints[0]=new ServicePoint(new Negexp(10,6), eventList, EventType.VALIDATION_COMPLETE);
		servicePoints[1]=new ServicePoint(new Negexp(10,10), eventList, EventType.MATCHING_COMPLETE);
		servicePoints[2]=new ServicePoint(new Negexp(5,3), eventList, EventType.EXECUTION_COMPLETE);
		
		// Toimeksiantojen saapuminen 💼
		arrivalProcess = new ArrivalProcess(new Negexp(15,5), eventList, EventType.ARRIVAL);
	}

	@Override
	protected void initialization() {
		// Ensimmäinen toimeksianto
		arrivalProcess.generateNext();	 // First arrival in the system
	}

	@Override
	protected void runEvent(Event t) {  // B phase events
		// KKäsitellään toimeksianto
		Order a;

		switch ((EventType)t.getType()){
		case ARRIVAL:
			servicePoints[0].add(t.getOrder());
			arrivalProcess.generateNext();
			controller.visualiseCustomer();
			break;

		case VALIDATION_COMPLETE:
			a = servicePoints[0].finishService();
			if (a != null) servicePoints[1].add(a);
			break;

		case MATCHING_COMPLETE:
			a = servicePoints[1].finishService();
			if (a != null) servicePoints[2].add(a);
			break;

		case EXECUTION_COMPLETE:
			servicePoints[2].finishService();
			break;

		case SIMULATION_END:
			break;
		}	
	}

	@Override
	protected void results() {
		controller.showEndTime(Clock.getInstance().getTime());
	}
}
