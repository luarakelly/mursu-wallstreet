package eds.model;

import java.util.List;

import controller.Controller;
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
	// Creates new arrival events.
	private ArrivalProcess arrivalProcess;
	private StatisticsCollector stats;
	private IMatchEngine matchEngine;
	private OrderBook orderBook;
	private StatisticsCollector.Snapshot latestSnapshot;
	private StatisticsAndMetricsRecord latestRecord;
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
			Controller controller,
			long seed,
			double meanValidation,
			double meanMarketMatching,
			double meanLimitMatching,
			double meanExecution,
			double arrivalMean,
			double marketOrderRatio,
			double buyOrderRatio,
			double initialMidPrice,
			double priceVolatility,
			double tickSize,
			String runTitle
	) {
		super(controller);

		// Store parameters needed by results()
		this.seed = seed;
		this.meanValidation = meanValidation;
		this.meanMarketMatching = meanMarketMatching;
		this.meanLimitMatching = meanLimitMatching;
		this.meanExecution = meanExecution;
		this.arrivalMean = arrivalMean;
		this.runTitle = runTitle;
		
		// Create service points.
		servicePoints = new ServicePoint[4];

		// Different seed offsets keep random streams separate.
		servicePoints[0]=new ServicePoint(new Negexp(meanValidation, seed + 88), eventList, EventType.VALIDATION_COMPLETE);
		servicePoints[1]=new ServicePoint(new Negexp(meanMarketMatching, seed + 19), eventList, EventType.MARKET_MATCHING_COMPLETE);
		servicePoints[2]=new ServicePoint(new Negexp(meanLimitMatching, seed + 22), eventList, EventType.LIMIT_MATCHING_COMPLETE);
		servicePoints[3]=new ServicePoint(new Negexp(meanExecution, seed + 69), eventList, EventType.EXECUTION_COMPLETE);

		// These ratios can be entered as 0..1 or 0..100.
		double buyRatio = normalizeRatio(buyOrderRatio);
		double marketRatio = normalizeRatio(marketOrderRatio);
		double limitRatio = 1.0 - marketRatio;

		DiscreteGenerator sideGenerator = new Bernoulli(buyRatio, seed + 42);
		DiscreteGenerator typeGenerator = new Bernoulli(limitRatio, seed + 55);

		// Price moves around the mid price with random noise.
		ContinuousGenerator priceGenerator = new Normal(0.0, priceVolatility * priceVolatility, seed + 62);

		// Order sizes come from a log-normal distribution and are rounded to lots.
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

		// Create the arrival process for new orders.
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
		
		// Create model helpers.
		stats = new StatisticsCollector(servicePoints.length);
		matchEngine = new MatchEngine();
		orderBook = new OrderBook();
	}

	// Converts ratio input to a value between 0 and 1.
	private double normalizeRatio(double value) {
		if (value < 0.0) {
			return 0.0;
		}
		if (value > 1.0) {
			return Math.min(1.0, value / 100.0);
		}
		return value;
	}

	@Override
	protected void initialization() {
		// Schedule the first order arrival.
		arrivalProcess.generateNext();
	}

	@Override
	protected void afterCycle() {
		// Sample metrics and refresh the order book table.
		stats.observeServicePoints(servicePoints);
		OrderBook.OrderBookSnapshot snapshot = orderBook.getSnapshot();
		stats.observeOrderBook(snapshot);
		controller.updateOrderBook(snapshot);
	}

	@Override
	protected void runEvent(Event t) {
		// Handle one simulation event.

			switch ((EventType)t.getType()){
				case ARRIVAL -> {
				ISimulationEntity arrivedEntity = t.getEntity();

					// Only orders move through the arrival flow.
					if (arrivedEntity instanceof Order arrivedOrder) {
						stats.arrival(arrivedOrder);
						servicePoints[0].add(arrivedOrder);
						arrivalProcess.generateNext();
						controller.updateTimeAndQueues();
					}
				}
				case VALIDATION_COMPLETE -> {
				ISimulationEntity validatedEntity = servicePoints[0].finishService();

					// Send the order to the correct matching stage.
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

				// Match the market order against the book.
				if (matchedEntity instanceof Order matchedOrder) {
					double now = Clock.getInstance().getTime();

					IMatchEngine.MatchResult matchResult = matchEngine.match(matchedOrder, orderBook, now);
					List<Trade> trades = matchResult.trades();

					// Resting orders that were filled in the book are completed here.
					for (Order completedOrder : matchResult.completedOrders()) {
						stats.completion(completedOrder, now);
					}

					// Trades and completed incoming orders go to execution.
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

				// Match the limit order against the book.
				if (matchedEntity instanceof Order matchedOrder) {
					double now = Clock.getInstance().getTime();

					IMatchEngine.MatchResult matchResult = matchEngine.match(matchedOrder, orderBook, now);
					List<Trade> trades = matchResult.trades();

					// Resting orders that were filled in the book are completed here.
					for (Order completedOrder : matchResult.completedOrders()) {
						stats.completion(completedOrder, now);
					}

					// Trades and completed incoming orders go to execution.
					for (Trade trade : trades) {
						servicePoints[3].add(trade);
					}

					if (!matchedOrder.isActive()) {
						servicePoints[3].add(matchedOrder);
					}
				}
			}
			case EXECUTION_COMPLETE -> {
				ISimulationEntity finishedEntity = servicePoints[3].finishService();

				// Execution stage can finish both orders and trades.
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
		// Build final snapshot, show it in UI, and save it to the database.
		double endTime = Clock.getInstance().getTime();
		latestSnapshot = stats.buildSnapshot(endTime);

		latestRecord = queries.buildRecord(
				latestSnapshot,
				runTitle,
				seed,
				meanValidation,
				meanMarketMatching,
				meanLimitMatching,
				meanExecution,
				arrivalMean,
				endTime);

		runId = queries.saveStatisticsAndMetrics(latestRecord);
		if (runId >= 0) {
			queries.saveAllTrades(latestSnapshot.trades(), runId);
		}
		controller.showEndTime(endTime);
		System.out.println("-----Simulation Statistics-----");
		System.out.println(latestSnapshot);
	}

	public StatisticsCollector.Snapshot getStatisticsSnapshot() {
		return latestSnapshot;
	}

	public StatisticsAndMetricsRecord getStatisticsRecord() {
		return latestRecord;
	}
}
