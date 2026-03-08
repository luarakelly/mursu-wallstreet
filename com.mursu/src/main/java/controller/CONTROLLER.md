# Controller Layer

The current controllers design is based on one shared controller class and two interfaces:
- `IViewToModelController` defines what the page controllers (view) can call
- `IModelToViewController` defines what the engine (model) can call
- `Controller.java` implements both interfaces and works as the central connection point

## Files

### `IViewToModelController.java`
Interface for calls from the UI layer to the main controller.

It contains operations for:
- main page initialization and preset loading
- opening the simulation page
- starting the simulation
- simulation controls such as pause and speed changes
- showing order book and results page data
- filling the results page
- returning from results to the main page

This interface is used by the page controllers so they depend on controller behavior, not on a concrete implementation.

### `IModelToViewController.java`
Interface for calls from the simulation engine to the controller.

It contains operations for:
- notifying that the run has ended
- updating time and queue labels
- updating the order book table

This interface is used by `MyEngine`, so the engine sends updates through the controller instead of touching JavaFX views directly.

### `Controller.java`
Central controller class of the application.

`Controller.java` implements both `IViewToModelController` and `IModelToViewController`.
Because of that, it sits between both sides of the program:
- page controllers call it through `IViewToModelController`
- `MyEngine` calls it through `IModelToViewController`

Main responsibilities:
- apply presets to the main page
- read form values and build `SimulationConfig`
- open `simulation_view.fxml` and `results_view.fxml`
- create and start `MyEngine`
- handle pause and speed changes during a run
- receive engine updates and move UI work to the JavaFX thread with `Platform.runLater(...)`
- prepare formatted values for the results page
- convert order book snapshots into table rows

This file contains the main application coordination logic.

### `MainPageController.java`
JavaFX controller for `main_view.fxml`.

Responsibilities:
- receive button actions from the main page
- forward those actions to `IViewToModelController`
- provide helper methods for reading and writing input field values

This class is only a page controller.
It does not create the engine or contain simulation logic.

### `SimulationPageController.java`
JavaFX controller for `simulation_view.fxml`.

Responsibilities:
- initialize the order book table
- forward pause and speed actions to `IViewToModelController`
- provide helper methods for updating timer, queues, and order book rows

This class is also a thin UI layer.
The simulation logic is not implemented here.

### `ResultsPageController.java`
JavaFX controller for `results_view.fxml`.

Responsibilities:
- receive final result data for the page
- forward navigation actions to `IViewToModelController`
- provide helper methods for updating result labels

This class displays values prepared by `Controller.java`.
It does not compute metrics itself.

## Connection flow

### 1. From page to controller
Each page controller creates `new Controller(this)` inside its `initialize()` method.
The page then stores that object through the `IViewToModelController` type.

This means the page can call controller operations such as:
- `initializeMainPage()`
- `openSimulationPageFromMain()`
- `startSimulation(config)`
- `togglePause()`
- `populateResults()`

### 2. From controller to engine
When the simulation starts, `Controller.startSimulation()` creates `MyEngine` and passes `this` into the constructor.

This works because `Controller` implements `IModelToViewController`.
So the engine receives a callback target that it can use for updates.

### 3. From engine back to controller
During the run, `MyEngine` calls methods from `IModelToViewController` such as:
- `updateTimeAndQueues()`
- `updateOrderBook()`
- `showEndTime()`

`Controller.java` receives those calls, prepares UI data if needed, and updates the JavaFX pages on the correct thread.

## Runtime sequence

### Main page
- JavaFX loads `main_view.fxml`
- `MainPageController.initialize()` creates `Controller`
- `Controller.initializeMainPage()` fills the default preset values

### Start simulation
- the user presses Start
- `MainPageController` forwards the action to `Controller.openSimulationPageFromMain()`
- `Controller` reads all input values into `SimulationConfig`
- `Controller` loads `simulation_view.fxml`
- `SimulationPageController.startSimulation(config)` forwards the config back to `Controller.startSimulation(...)`
- `Controller` creates and starts `MyEngine`

### During simulation
- `SimulationPageController` forwards pause and speed actions to `Controller`
- `MyEngine` sends time, queue, and order book updates to `Controller`
- `Controller` updates the simulation page through JavaFX-safe UI calls

### End of simulation
- `MyEngine.results()` builds the final snapshot and record
- `MyEngine` calls `showEndTime()` on `Controller`
- `Controller` opens the results page
- `ResultsPageController.setResults()` forwards final data to `Controller.populateResults()`
- `Controller` formats and writes the final values into the results page labels
