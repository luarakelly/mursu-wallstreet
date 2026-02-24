package eds.framework;

/* Controller uses this interface */
public interface IEngine { // NEW
	public void setSimulationTime(double time);
	public void setDelay(long time);
	public long getDelay();
}
