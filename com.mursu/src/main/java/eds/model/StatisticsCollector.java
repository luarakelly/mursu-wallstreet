package eds.model;

/* Minimal MVP statistics collector. Tracks order arrivals, completions, and basic market metrics.*/
public class StatisticsCollector {

	// Counters
	private long totalArrivals = 0;
	private long totalCompletions = 0;
	private long totalFilled = 0;
	private long totalCancelled = 0;
	private long totalVolume = 0;
	
	// Timing
	private double totalResponseTime = 0.0;

	/*Track new order arrival.*/
	public void arrival(Order order) {
		totalArrivals++;
		totalVolume += order.getOriginalShareSize();
	}

	/*Track order completion.*/
	public void completion(Order order, double completionTime) {
		totalCompletions++;
		
		if (order.isFilled()) {
			totalFilled++;
		} else {
			totalCancelled++;
		}
		
		double responseTime = completionTime - order.getArrivalTime();
		totalResponseTime += responseTime;
	}

	@Override
	public String toString() {
		double avgResponseTime = totalCompletions > 0 ? totalResponseTime / totalCompletions : 0.0;
		return String.format(
				"Orders: %d arrived, %d completed (%d filled, %d cancelled) Volume: %d Avg time: %.2f",
				totalArrivals, totalCompletions, totalFilled, totalCancelled, totalVolume, avgResponseTime);
	}
}
