package eds.framework;

import eds.model.EventType;
import eds.model.Order;
import eduni.distributions.Bernoulli;
import eduni.distributions.ContinuousGenerator;
import eduni.distributions.DiscreteGenerator;
import eduni.distributions.Poisson;
import eduni.distributions.Uniform;

public class ArrivalProcess {
	// Typing
	private ContinuousGenerator generator;
	private EventList eventList;
	private EventType type;
	private DiscreteGenerator sideGenerator;
	private DiscreteGenerator typeGenerator;
	private ContinuousGenerator priceGenerator;
	private DiscreteGenerator sizeGenerator;

	public ArrivalProcess(ContinuousGenerator g, EventList tl, EventType type) {
		// Alustus näille
		this.generator = g;
		this.eventList = tl;
		this.type = type;
		this.sideGenerator = new Bernoulli(0.5);
		this.typeGenerator = new Bernoulli(0.8);
		this.priceGenerator = new Uniform(95.0, 105.0);
		this.sizeGenerator = new Poisson(50.0);
	}

	public void generateNext() {
		// Generoi uusi toimeksianto
		double now = Clock.getInstance().getTime();
		Order.Side side = sideGenerator.sample() == 1 ? Order.Side.BUY : Order.Side.SELL;
		Order.Type orderType = typeGenerator.sample() == 1 ? Order.Type.LIMIT : Order.Type.MARKET;
		double price = orderType == Order.Type.LIMIT ? priceGenerator.sample() : 0.0;
		int size = (int) Math.max(1, sizeGenerator.sample());
		Order order = new Order(side, orderType, price, size, now);
		// Uusi tapahtuma
		Event t = new Event(type, now + generator.sample(), order);
		eventList.add(t);
	}

}
