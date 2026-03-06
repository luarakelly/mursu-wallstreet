package eds.model;

import java.util.ArrayList;
import java.util.List;

import eds.framework.ServicePoint;

/* Aggregates simulation metrics and provides a structured snapshot for persistence/reporting. */
public class StatisticsCollector {
	public record Snapshot(
			long totalArrivedOrders,
			long totalExecutedOrders,
			int remainingOrdersInBook,
			double averageMidPrice,
			double averageSpread,
			double throughput,
			double averageWaitingTime,
			double fillRate,
			double averageServicePointUtilization,
			List<Double> servicePointUtilization,
			List<Double> averageQueueLengthPerServicePoint,
			boolean bottleneckDetected,
			int bottleneckServicePointIndex,
			double bottleneckAverageQueueLength
	) {
	}

	// Counters
	private long totalArrivals = 0;
	private long totalExecuted = 0;
	private long totalFilled = 0;
	private long totalCancelled = 0;

	// Timing
	private double totalWaitingTime = 0.0;
	private double firstArrivalTime = Double.NaN;

	// Order book sampling
	private int remainingOrdersInBook = 0;
	private double totalMidPrice = 0.0;
	private long midPriceSamples = 0;
	private double totalSpread = 0.0;
	private long spreadSamples = 0;

	// Service point sampling
	private final int servicePointCount;
	private final long[] servicePointSamples;
	private final long[] servicePointBusySamples;
	private final double[] totalQueueLength;

	public StatisticsCollector() {
		this(4);
	}

	public StatisticsCollector(int servicePointCount) {
		this.servicePointCount = Math.max(0, servicePointCount);
		this.servicePointSamples = new long[this.servicePointCount];
		this.servicePointBusySamples = new long[this.servicePointCount];
		this.totalQueueLength = new double[this.servicePointCount];
	}

	/*Track new order arrival.*/
	public void arrival(Order order) {
		totalArrivals++;
		if (Double.isNaN(firstArrivalTime)) {
			firstArrivalTime = order.getArrivalTime();
		}
	}

	/*Track order completion.*/
	public void completion(Order order, double completionTime) {
		totalExecuted++;

		if (order.isFilled()) {
			totalFilled++;
		} else if (order.getStatus() == Order.Status.CANCELLED) {
			totalCancelled++;
		}

		double waitingTime = Math.max(0.0, completionTime - order.getArrivalTime());
		totalWaitingTime += waitingTime;
	}

	/*Capture order book dependent metrics.*/
	public void observeOrderBook(OrderBook.OrderBookSnapshot snapshot) {
		remainingOrdersInBook = snapshot.bids().stream().mapToInt(OrderBook.PriceLevel::orderCount).sum()
				+ snapshot.asks().stream().mapToInt(OrderBook.PriceLevel::orderCount).sum();

		if (snapshot.midPrice().isPresent()) {
			totalMidPrice += snapshot.midPrice().getAsDouble();
			midPriceSamples++;
		}

		if (snapshot.spread().isPresent()) {
			totalSpread += snapshot.spread().getAsDouble();
			spreadSamples++;
		}
	}

	/*Capture service point utilization and queue accumulation.*/
	public void observeServicePoints(ServicePoint[] servicePoints) {
		if (servicePoints == null) {
			return;
		}

		int observed = Math.min(servicePoints.length, servicePointCount);
		for (int i = 0; i < observed; i++) {
			ServicePoint point = servicePoints[i];
			if (point == null) {
				continue;
			}

			servicePointSamples[i]++;
			if (point.isBusy()) {
				servicePointBusySamples[i]++;
			}
			totalQueueLength[i] += point.getQueueLength();
		}
	}

	/*Create an immutable snapshot that can be saved into a database.*/
	public Snapshot buildSnapshot(double simulationEndTime, OrderBook.OrderBookSnapshot finalOrderBookSnapshot) {
		observeOrderBook(finalOrderBookSnapshot);

		double avgMidPrice = midPriceSamples > 0 ? totalMidPrice / midPriceSamples : 0.0;
		double avgSpread = spreadSamples > 0 ? totalSpread / spreadSamples : 0.0;
		double avgWaitingTime = totalExecuted > 0 ? totalWaitingTime / totalExecuted : 0.0;

		double effectiveStart = Double.isNaN(firstArrivalTime) ? 0.0 : firstArrivalTime;
		double elapsed = Math.max(0.0, simulationEndTime - effectiveStart);
		double throughput = elapsed > 0.0 ? totalExecuted / elapsed : 0.0;
		double fillRate = totalArrivals > 0 ? (double) totalFilled / totalArrivals : 0.0;

		List<Double> utilizationPerServicePoint = new ArrayList<>(servicePointCount);
		List<Double> averageQueuePerServicePoint = new ArrayList<>(servicePointCount);
		double utilizationSum = 0.0;
		double bottleneckQueue = 0.0;
		int bottleneckIndex = -1;

		for (int i = 0; i < servicePointCount; i++) {
			double utilization = servicePointSamples[i] > 0
					? (double) servicePointBusySamples[i] / servicePointSamples[i]
					: 0.0;
			double avgQueue = servicePointSamples[i] > 0
					? totalQueueLength[i] / servicePointSamples[i]
					: 0.0;

			utilizationPerServicePoint.add(utilization);
			averageQueuePerServicePoint.add(avgQueue);
			utilizationSum += utilization;

			if (avgQueue > bottleneckQueue) {
				bottleneckQueue = avgQueue;
				bottleneckIndex = i;
			}
		}

		double averageUtilization = servicePointCount > 0 ? utilizationSum / servicePointCount : 0.0;
		boolean bottleneckDetected = bottleneckQueue > 0.0;

		return new Snapshot(
				totalArrivals,
				totalExecuted,
				remainingOrdersInBook,
				avgMidPrice,
				avgSpread,
				throughput,
				avgWaitingTime,
				fillRate,
				averageUtilization,
				List.copyOf(utilizationPerServicePoint),
				List.copyOf(averageQueuePerServicePoint),
				bottleneckDetected,
				bottleneckIndex,
				bottleneckQueue
		);
	}

	@Override
	public String toString() {
		double avgWaitingTime = totalExecuted > 0 ? totalWaitingTime / totalExecuted : 0.0;
		double fillRate = totalArrivals > 0 ? (double) totalFilled / totalArrivals : 0.0;
		return String.format(
				"Orders: %d arrived, %d executed (%d filled, %d cancelled), waiting=%.4f, fillRate=%.2f",
				totalArrivals,
				totalExecuted,
				totalFilled,
				totalCancelled,
				avgWaitingTime,
				fillRate);
	}
}
