# Controllers

This layer contains the controllers used by the engine and the UI of the application.

## Files

### `MainPageController.java`
Controls the main page from `main_view.fxml`.

- reads values from the input fields
- applies preset values to the form
- creates `SimulationConfig` from the current form values
- opens the simulation page at the end

### `SimulationPageController.java`
Controls the simulation page from `simulation_view.fxml`.

- handles simulation page buttons
- shows timer values
- shows queue lengths
- shows the order book table
- opens the results page when the run ends

This class is the UI controller for the simulation page.
It does not run the simulation logic directly, it uses `Controller.java` for that.

### `Controller.java`
This is the logic controller (mostly for the simulation page, so the simulation page does not touch MyEngine on its own).

Responsibilities:
- creates `MyEngine`
- starts the simulation
- changes simulation speed
- pauses and resumes the simulation
- receives updates from `MyEngine`
- sends those updates to `SimulationPageController`

This class is placed between the simulation UI and the engine.

### `ResultsPageController.java`
Controls the results page from `results_view.fxml`.

Responsibilities:
- shows final metrics from the simulation snapshot
- shows bottleneck text
- shows human-readable insight text from `PerformanceDescriber`
- returns user to the main page

## Application workflow

### 1. Application start
`SimulatorGUI.java` starts JavaFX and loads `main_view.fxml`.
At that moment JavaFX creates `MainPageController`.

### 2. Main page
`MainPageController` fills the input fields with the balanced preset in `initialize()`.
When the user clicks the Start button, the method `handleStartSimulation()`:
- reads all input values
- builds `SimulationConfig`
- loads `simulation_view.fxml`
- gets `SimulationPageController`
- calls `startSimulation(config)`

### 3. Simulation page
When `simulation_view.fxml` is loaded, JavaFX creates `SimulationPageController`.
In its `initialize()` method it:
- prepares the order book table
- creates a new `Controller` object from `Controller.java`

Then `SimulationPageController.startSimulation(config)` passes the config to `Controller`.

### 4. Logic controller and engine
`Controller.startSimulation(config)`:
- creates `MyEngine`
- copies values from `SimulationConfig` into the engine
- starts the engine thread

While the simulation is running:
- `SimulationPageController` sends button actions to `Controller`
- `Controller` changes speed or pause state in `MyEngine`
- `MyEngine` sends updates back through `Controller`
- `Controller` updates `SimulationPageController`

### 5. Results page
When the engine finishes, `Controller.showEndTime()` tells `SimulationPageController`
to open the results page.

`SimulationPageController.showResultsPage()`:
- loads `results_view.fxml`
- gets `ResultsPageController`
- sends final snapshot and record there

`ResultsPageController.setResults()` then fills the final labels and insights.