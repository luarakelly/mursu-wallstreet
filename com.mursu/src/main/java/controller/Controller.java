package controller;

import eds.framework.IEngine;
import eds.model.MyEngine;
import javafx.application.Platform;
import view.ISimulatorUI;

public class Controller implements IControllerVtoM, IControllerMtoV {   // NEW
	private IEngine engine;
	private ISimulatorUI ui;

	// TODO temporary values for test purposes
	// I'll get those values from the UI
	private static final long DEFAULT_SEED = 42L;
	private static final double DEFAULT_MEAN_VALIDATION = 0.5;
	private static final double DEFAULT_MEAN_MARKET_MATCHING = 0.6;
	private static final double DEFAULT_MEAN_LIMIT_MATCHING = 0.7;
	private static final double DEFAULT_MEAN_EXECUTION = 0.4;
	private static final double DEFAULT_ARRIVAL_MEAN = 1.0;
	private static final double DEFAULT_INITIAL_MID_PRICE = 100.0;
	private static final double DEFAULT_PRICE_VOLATILITY = 0.2;
	private static final double DEFAULT_TICK_SIZE = 0.01;
	
	public Controller(ISimulatorUI ui) {
		this.ui = ui;
	}

	/* Engine control: */
	@Override
	public void startSimulation() {
		engine = new MyEngine(
				this,
				DEFAULT_SEED,
				DEFAULT_MEAN_VALIDATION,
				DEFAULT_MEAN_MARKET_MATCHING,
				DEFAULT_MEAN_LIMIT_MATCHING,
				DEFAULT_MEAN_EXECUTION,
				DEFAULT_ARRIVAL_MEAN,
				DEFAULT_INITIAL_MID_PRICE,
				DEFAULT_PRICE_VOLATILITY,
				DEFAULT_TICK_SIZE
		);
		engine.setSimulationTime(ui.getTime());
		engine.setDelay(ui.getDelay());
		ui.getVisualisation().clearDisplay();
		((Thread) engine).start();
		//((Thread)engine).run(); // Never like this, why?
	}
	
	@Override
	public void decreaseSpeed() { // hidastetaan moottorisäiettä
		engine.setDelay((long)(engine.getDelay()*1.10));
	}

	@Override
	public void increaseSpeed() { // nopeutetaan moottorisäiettä
		engine.setDelay((long)(engine.getDelay()*0.9));
	}


	/* Simulation results passing to the UI
	 * Because FX-UI updates come from engine thread, they need to be directed to the JavaFX thread
	 */
	@Override
	public void showEndTime(double time) {
		Platform.runLater(()->ui.setEndingTime(time));
	}

	@Override
	public void visualiseCustomer() {
		Platform.runLater(() -> ui.getVisualisation().newCustomer());
	}
}
