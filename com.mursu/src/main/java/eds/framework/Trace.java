package eds.framework;

/**
 * Provides simple logging functionality for the simulation framework.
 *
 * Trace allows filtering log output based on severity level.
 * Only messages with a level greater than or equal to the
 * currently set trace level are printed to standard output.
 */
public class Trace {

	/**
	 * Logging levels used to control output verbosity.
	 */
	public enum Level {
		INFO, WAR, ERR
	}

	// current logging threshold
	private static Level traceLevel = Level.INFO;

	/**
	 * Sets the current trace level.
	 * Only messages at this level or higher will be printed.
	 *
	 * @param lvl the new trace level
	 */
	public static void setTraceLevel(Level lvl) {
		traceLevel = lvl;
	}

	/**
	 * Prints a message if its level is greater than or equal
	 * to the current trace level.
	 *
	 * @param lvl the level of the message
	 * @param txt the message text
	 */
	public static void out(Level lvl, String txt) {
		if (traceLevel == null) return;

		if (lvl.ordinal() >= traceLevel.ordinal()) {
			System.out.println(txt);
		}
	}
}