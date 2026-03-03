package eds.model;

import controller.IControllerMtoV;
import eds.framework.ArrivalProcess;
import eds.framework.Clock;
import eds.framework.Engine;
import eds.framework.Event;
import eds.framework.ServicePoint;
import eds.framework.ISimulationEntity;
import eduni.distributions.Bernoulli;
import eduni.distributions.ContinuousGenerator;
import eduni.distributions.DiscreteGenerator;
import eduni.distributions.LogNormal;
import eduni.distributions.Negexp;
import eduni.distributions.Normal;

import java.util.List;



public class MyEngine extends Engine {
	// Toimeksiannon saapuminen
	private ArrivalProcess arrivalProcess;
	private StatisticsCollector stats;
	private IMatchEngine matchEngine;
	private OrderBook orderBook;

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

		/*
			Each generator receives a unique seed offset to ensure statistically
			independent random streams. Same base seed always produces identical
			simulation output — reproducibility is fully preserved.
		 */
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
		matchEngine = new MatchEngine();
		orderBook = new OrderBook();
	}

	@Override
	protected void initialization() {
		// Ensimmäinen toimeksianto
		arrivalProcess.generateNext();
	}

	@Override
	protected void runEvent(Event t) {  // B phase events
		// Käsitellään toimeksianto

			switch ((EventType)t.getType()){
				case ARRIVAL -> {
				ISimulationEntity arrivedEntity = t.getEntity();

					// Arrival flow expects Order, so we check the concrete type
					if (arrivedEntity instanceof Order arrivedOrder) {
						stats.arrival(arrivedOrder);
						servicePoints[0].add(arrivedOrder);
						arrivalProcess.generateNext();
						controller.visualiseCustomer();
					}
				}
				case VALIDATION_COMPLETE -> {
				ISimulationEntity validatedEntity = servicePoints[0].finishService();

					// Validation flow expects an Order, so we check the concrete type
					if (validatedEntity instanceof Order validatedOrder) {
						if (validatedOrder.getType() == Order.Type.MARKET) {
							servicePoints[1].add(validatedOrder);
						} else {
							servicePoints[2].add(validatedOrder);
						}
					}
				}
			case MARKET_MATCHING_COMPLETE -> {
				ISimulationEntity matchedEntity = servicePoints[1].finishService();

				// We can only match orders against the order book, so we check the concrete type
				if (matchedEntity instanceof Order matchedOrder) {
					// Use current simulation time for timestamp
					double now = Clock.getInstance().getTime();

					// Matching returns all trades produced by this order
					List<Trade> trades = matchEngine.match(matchedOrder, orderBook, now);

					// Send each trade to EXECUTION service point
					for (Trade trade : trades) {
						servicePoints[3].add(trade);
					}

					// If order is done, send it too
					if (!matchedOrder.isActive()) {
						servicePoints[3].add(matchedOrder);
					}
				}
			}
			case LIMIT_MATCHING_COMPLETE -> {
				ISimulationEntity matchedEntity = servicePoints[2].finishService();

				// We can only match orders against the order book, so we check the concrete type
				if (matchedEntity instanceof Order matchedOrder) {
					// Use current simulation time for timestamp
					double now = Clock.getInstance().getTime();

					// Matching returns all trades produced by this order
					List<Trade> trades = matchEngine.match(matchedOrder, orderBook, now);

					// Send each trade to EXECUTION service point
					for (Trade trade : trades) {
						servicePoints[3].add(trade);
					}

					// If order is done, send it too
					if (!matchedOrder.isActive()) {
						servicePoints[3].add(matchedOrder);
					}
				}
			}
			case EXECUTION_COMPLETE -> {
				ISimulationEntity finishedEntity = servicePoints[3].finishService();

				// EXECUTION service point can contain Trade and Order
				if (finishedEntity instanceof Order finishedOrder) {
					stats.completion(finishedOrder, Clock.getInstance().getTime());
				}
				if (finishedEntity instanceof Trade finishedTrade) {
					// TODO stats for finishedTrade
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
