package eds.model;

import controller.IControllerMtoV;
import eds.framework.ArrivalProcess;
import eds.framework.Clock;
import eds.framework.Engine;
import eds.framework.Event;
import eds.framework.ServicePoint;
import eduni.distributions.Bernoulli;
import eduni.distributions.ContinuousGenerator;
import eduni.distributions.DiscreteGenerator;
import eduni.distributions.LogNormal;
import eduni.distributions.Negexp;
import eduni.distributions.Normal;



public class MyEngine extends Engine {
	// Toimeksiannon saapuminen
	private ArrivalProcess arrivalProcess;
	private StatisticsCollector stats;

	public MyEngine(
			IControllerMtoV controller,
			long seed,
			double meanValidation,
			double meanMarketMatching,
			double meanLimitMatching,
			double meanExecution,
			double arrivalMean,
			double initialMidPrice,
			double priceVolatility,
			double tickSize
	) {
		super(controller);
		
		// Luodaan palvelupisteet
		servicePoints = new ServicePoint[4];
	
		/* Creating servicePoints with seed and a random number to ensure desync. 
		Not sure if that's needed though */
		servicePoints[0]=new ServicePoint(new Negexp(meanValidation, seed + 88), eventList, EventType.VALIDATION_COMPLETE);
		servicePoints[1]=new ServicePoint(new Negexp(meanMarketMatching, seed + 19), eventList, EventType.MARKET_MATCHING_COMPLETE);
		servicePoints[2]=new ServicePoint(new Negexp(meanLimitMatching, seed + 22), eventList, EventType.LIMIT_MATCHING_COMPLETE);
		servicePoints[3]=new ServicePoint(new Negexp(meanExecution, seed + 69), eventList, EventType.EXECUTION_COMPLETE);

		DiscreteGenerator sideGenerator = new Bernoulli(0.5, seed + 42);
		DiscreteGenerator typeGenerator = new Bernoulli(0.8, seed + 55);
		/* Normal random walk.*/
		ContinuousGenerator priceGenerator = new Normal(0.0, priceVolatility * priceVolatility, seed + 62);

		/* Order size variables and generation. We can access these later from the GUI */
		final double orderSizeLogNormalMean = 3.7;
		final double orderSizeLogNormalVariance = 1.0;
		final long orderSizeLotSize = 10L;
		ContinuousGenerator rawSizeGenerator = new LogNormal(orderSizeLogNormalMean, orderSizeLogNormalVariance, seed + 8);
		DiscreteGenerator sizeGenerator = new DiscreteGenerator() {
			@Override
			public long sample() {
				double rawSize = rawSizeGenerator.sample();
				long roundedToLot = Math.round(rawSize / orderSizeLotSize) * orderSizeLotSize;
				return Math.max(orderSizeLotSize, roundedToLot);
			}

			@Override
			public void setSeed(long seed) {
				rawSizeGenerator.setSeed(seed);
			}

			@Override
			public long getSeed() {
				return rawSizeGenerator.getSeed();
			}

			@Override
			public void reseed() {
				rawSizeGenerator.reseed();
			}
		};
		
		// Toimeksiantojen saapuminen 💼
		arrivalProcess = new ArrivalProcess(
				new Negexp(arrivalMean, seed + 4),
				eventList,
				EventType.ARRIVAL,
				sideGenerator,
				typeGenerator,
				priceGenerator,
				sizeGenerator,
				initialMidPrice,
				tickSize
		);
		
		// Tilastot
		stats = new StatisticsCollector();
	}

	@Override
	protected void initialization() {
		// Ensimmäinen toimeksianto
		arrivalProcess.generateNext();
	}

	@Override
	protected void runEvent(Event t) {  // B phase events
		// Käsitellään toimeksianto
		// Also satisfying Ksenia's wish for better naming. ☝️
		Order order;

		switch ((EventType)t.getType()){
			case ARRIVAL -> {
				Order arrivedOrder = t.getOrder();
				stats.arrival(arrivedOrder);
				servicePoints[0].add(arrivedOrder);
				arrivalProcess.generateNext();
				controller.visualiseCustomer();
			}
			case VALIDATION_COMPLETE -> {
				order = servicePoints[0].finishService();
				if (order != null) {
					if (order.getType() == Order.Type.MARKET) {
						servicePoints[1].add(order);
					} else {
						servicePoints[2].add(order);
					}
				}
			}
			case MARKET_MATCHING_COMPLETE -> {
				order = servicePoints[1].finishService();
				// TODO: match order here
				if (order != null) servicePoints[3].add(order);
			}
			case LIMIT_MATCHING_COMPLETE -> {
				order = servicePoints[2].finishService();
				// TODO: match order here
				if (order != null) servicePoints[3].add(order);
			}
			case EXECUTION_COMPLETE -> {
				Order finishedOrder = servicePoints[3].finishService();
				if (finishedOrder != null) {
					stats.completion(finishedOrder, Clock.getInstance().getTime());
				}
			}
		}
	}

	@Override
	protected void results() {
		controller.showEndTime(Clock.getInstance().getTime());
		System.out.println("-----Simulation Statistics-----");
		System.out.println(stats);
	}
}
