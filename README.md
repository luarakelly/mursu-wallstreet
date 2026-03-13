# Mursu Wallstreet
*Mursu Wallstreet* is a Java-based electronic exchange simulator with a JavaFX user interface. The application models order arrivals, validation, market and limit order matching, trade execution, and order book state updates during a simulated trading session.

<p align="center">
  <img src="https://github.com/user-attachments/assets/c10a0e62-e09c-4f8c-bb3e-48a18ed199c9" alt="Mursu Wallstreet UI" width="700">
</p>

The project combines three main concepts:

- a discrete-event simulation engine
- an order book and matching engine
- a JavaFX UI for running simulations and viewing results

Simulation results are also persisted to a local SQLite database, which makes it possible to inspect historical runs after the simulation ends.

## Project Structure
- `com.mursu/src/main/java/controller`  
  JavaFX page controllers and application flow.
- `com.mursu/src/main/java/eds/framework`  
  Generic simulation framework classes such as the clock, event list, engine, and service points.
- `com.mursu/src/main/java/eds/model`  
  Exchange domain logic: orders, trades, order book, matching engine, and simulation engine.
- `com.mursu/src/main/java/eds/database`  
  Database access, records, and result descriptions.
- `com.mursu/src/main/java/view`  
  JavaFX application and graph rendering.
- `com.mursu/src/main/resources`  
  FXML views and UI assets.

## Requirements
- Java 17
- Maven 3.9+ recommended

## How To Run
All Maven commands should be executed inside the `com.mursu` directory.

```bash
cd com.mursu
mvn clean javafx:run
```

This starts the JavaFX application and opens the simulator UI.

## How To Run Tests
```bash
cd com.mursu
mvn test
```

## Documentation
Project documentation is available here:

`(https://github.com/luarakelly/mursu-wallstreet-docs)`

## What The Simulator Does
During a run, the engine:

- generates incoming buy and sell, market and limit orders
- routes them through validation and matching stages
- updates the order book after each simulation cycle
- records executed trades and aggregated metrics
- saves final statistics into `com.mursu/simulation.db`

The simulation UI includes:

- queue length indicators for each service stage
- a live order book table
- a mid-price graph
- a results page with final performance metrics

## Technologies used
- Java 17
- JavaFX
- Maven
- JUnit 5
- SQLite
