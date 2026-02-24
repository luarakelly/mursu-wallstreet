package eds.framework;

// DONE (by Ks)
// I don't know yet if we use it

public class Trace {
	public enum Level { INFO, WAR, ERR }

	private static Level traceLevel = Level.INFO;

	public static void setTraceLevel(Level lvl){
		traceLevel = lvl;
	}

	public static void out(Level lvl, String txt){
		if (traceLevel == null) return;

		if (lvl.ordinal() >= traceLevel.ordinal()){
			System.out.println(txt);
		}
	}
}