package eds.model;

import java.util.List;

import controller.IControllerMtoV;
import eds.database.IQueries;
import eds.database.Queries;
import eds.database.Records.StatisticsAndMetricsRecord;
import eds.framework.ArrivalProcess;
import eds.framework.Clock;
import eds.framework.Engine;
import eds.framework.Event;
import eds.framework.ISimulationEntity;
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
	private IMatchEngine matchEngine;
	private OrderBook orderBook;
	private StatisticsCollector.Snapshot latestSnapshot;

	private IQueries queries = new Queries();
	private int runId = -1;

	// Stored for persistence in results()
	private final long seed;
	private final double meanValidation;
	private final double meanMarketMatching;
	private final double meanLimitMatching;
	private final double meanExecution;
	private final double arrivalMean;
	private final String runTitle;

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
			double tickSize,
			String runTitle) {
		super(controller);
		// Store parameters needed by results()
		this.seed = seed;
		this.meanValidation = meanValidation;
		this.meanMarketMatching = meanMarketMatching;
		this.meanLimitMatching = meanLimitMatching;
		this.meanExecution = meanExecution;
		this.arrivalMean = arrivalMean;
		this.runTitle = runTitle;

		// Luodaan palvelupisteet
		servicePoints = new ServicePoint[4];

		/*
		 * Each generator receives a unique seed offset to ensure statistically
		 * independent random streams.
		 * Same base seed always produces identical simulation output —
		 * reproducibility is fully preserved.
		 */
		servicePoints[0] = new ServicePoint(new Negexp(meanValidation, seed + 88), eventList,
				EventType.VALIDATION_COMPLETE);
		servicePoints[1] = new ServicePoint(new Negexp(meanMarketMatching, seed + 19), eventList,
				EventType.MARKET_MATCHING_COMPLETE);
		servicePoints[2] = new ServicePoint(new Negexp(meanLimitMatching, seed + 22), eventList,
				EventType.LIMIT_MATCHING_COMPLETE);
		servicePoints[3] = new ServicePoint(new Negexp(meanExecution, seed + 69), eventList,
				EventType.EXECUTION_COMPLETE);

		DiscreteGenerator sideGenerator = new Bernoulli(0.5, seed + 42);
		DiscreteGenerator typeGenerator = new Bernoulli(0.8, seed + 55);
		/* Normal random walk. */
		ContinuousGenerator priceGenerator = new Normal(0.0, priceVolatility * priceVolatility, seed + 62);

		/*
		 * Order size variables and generation. We can access these later from the GUI
		 */
		final double orderSizeLogNormalMean = 3.7;
		final double orderSizeLogNormalVariance = 1.0;
		final long orderSizeLotSize = 10L;
		ContinuousGenerator rawSizeGenerator = new LogNormal(orderSizeLogNormalMean, orderSizeLogNormalVariance,
				seed + 8);
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
				tickSize);

		// Tilastot
		stats = new StatisticsCollector(servicePoints.length);
		matchEngine = new MatchEngine();
		orderBook = new OrderBook();
	}

	@Override
	protected void initialization() {
		// Ensimmäinen toimeksianto
		arrivalProcess.generateNext();
	}

	@Override
	protected void afterCycle() {
		stats.observeServicePoints(servicePoints);
		stats.observeOrderBook(orderBook.getSnapshot());
	}

	@Override
	protected void runEvent(Event t) { // B phase events
		// Käsitellään toimeksianto

		switch ((EventType) t.getType()) {
			case ARRIVAL -> {
				ISimulationEntity arrivedEntity = t.getEntity();

				// Arrival flow expects Order, so we check the concrete type
				if (arrivedEntity instanceof Order arrivedOrder) {
					stats.arrival(arrivedOrder);
					servicePoints[0].add(arrivedOrder);
					arrivalProcess.generateNext();
					controller.visualiseEntity();
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

				// We can only match orders against the order book, so we check the concrete
				// type
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

				// We can only match orders against the order book, so we check the concrete
				// type
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
					stats.trade(finishedTrade);
				}
			}
		}
	}

	@Override
	protected void results() {
		double endTime = Clock.getInstance().getTime();
		latestSnapshot = stats.buildSnapshot(Clock.getInstance().getTime());
		controller.showEndTime(Clock.getInstance().getTime());

		StatisticsAndMetricsRecord record = queries.buildRecord(
				latestSnapshot,
				runTitle,
				seed,
				meanValidation,
				meanMarketMatching,
				meanLimitMatching,
				meanExecution,
				arrivalMean,
				endTime);

		runId = queries.saveStatisticsAndMetrics(record);
		queries.saveAllTrades(latestSnapshot.trades(), runId);

		System.out.println("-----Simulation Statistics-----");
		System.out.println(latestSnapshot);
	}

	public StatisticsCollector.Snapshot getStatisticsSnapshot() {
		return latestSnapshot;
	}
}
