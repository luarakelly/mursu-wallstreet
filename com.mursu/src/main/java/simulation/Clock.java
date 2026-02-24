package simulation;

public class Clock {
	private double time;
	private static Clock instance;
	
	private Clock(){
		time = 0;
	}
	
	public static Clock getInstance(){
		if (instance == null){
			instance = new Clock();
		}
		return instance;
	}
	
	public void setTime(double time){
		this.time = time;
	}

	public double getTime(){
		return time;
	}
}
