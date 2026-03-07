package eds.config;

/**
 * Stores one full set of simulation input values.
 */
public record SimulationConfig(
		String title,
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
		double simulationTime,
		long delay
) {
	/**
	 * Returns the balanced market preset.
	 *
	 * @return configuration for a balanced market run
	 */
	public static SimulationConfig balanced() {
		return new SimulationConfig(
				"Balanced market",
				(long) 42,
				0.5,
				0.6,
				0.7,
				0.4,
				1.0,
				0.2,
				0.5,
				100.0,
				0.2,
				0.01,
				60.0,
				(long) 10
		);
	}

	/**
	 * Returns the slow stable market preset.
	 *
	 * @return configuration for a slow stable market run
	 */
	public static SimulationConfig slowStable() {
		return new SimulationConfig(
				"Slow stable market",
				(long) 42,
				0.8,
				0.9,
				1.0,
				0.8,
				1.5,
				0.2,
				0.5,
				100.0,
				0.1,
				0.01,
				60.0,
				(long) 20
		);
	}

	/**
	 * Returns the high frequency market preset.
	 *
	 * @return configuration for a high frequency market run
	 */
	public static SimulationConfig highFrequency() {
		return new SimulationConfig(
				"High frequency scenario",
				(long) 42,
				0.25,
				0.2,
				0.25,
				0.15,
				0.2,
				0.2,
				0.5,
				100.0,
				0.25,
				0.01,
				60.0,
				(long) 2
		);
	}

	/**
	 * Returns the volatile market preset.
	 *
	 * @return configuration for a volatile market run
	 */
	public static SimulationConfig volatileMarket() {
		return new SimulationConfig(
				"Volatile market",
				(long) 42,
				0.5,
				0.6,
				0.7,
				0.4,
				0.8,
				0.2,
				0.5,
				100.0,
				1.2,
				0.01,
				60.0,
				(long) 8
		);
	}
}
