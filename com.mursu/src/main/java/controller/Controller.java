package controller;

import eds.framework.IEngine;
import eds.model.MyEngine;
import javafx.application.Platform;
import view.ISimulatorUI;

public class Controller implements IControllerVtoM, IControllerMtoV {   // NEW
	private IEngine engine;
	private ISimulatorUI ui;
	
	public Controller(ISimulatorUI ui) {
		this.ui = ui;
	}

	/* Engine control: */
	@Override
	public void startSimulation() {
		engine = new MyEngine(this); // new Engine thread is created for every simulation
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
