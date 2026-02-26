package eds.framework;

/**
 * Singleton clock.
 *
 * The Clock class maintains the current simulation time.
 * Only one instance of this class exists during execution.
 * The time value is represented as a double.
 */
public class Clock {

	// the only instance of the class
	private static Clock instance;

	// current simulation time
	private double time;

	/**
	 * Private constructor to prevent external instantiation.
	 * Initializes simulation time to 0.
	 */
	private Clock() {
		this.time = 0;
	}

	/**
	 * Returns the single instance of the Clock.
	 * If the instance does not exist yet, it is created.
	 *
	 * @return the singleton Clock instance
	 */
	public static Clock getInstance() {
		if (instance == null) {
			instance = new Clock();
		}
		return instance;
	}

	/**
	 * Sets the current simulation time.
	 *
	 * @param time the new simulation time
	 */
	public void setTime(double time) {
		this.time = time;
	}

	/**
	 * Returns the current simulation time.
	 *
	 * @return current simulation time
	 */
	public double getTime() {
		return time;
	}
}
