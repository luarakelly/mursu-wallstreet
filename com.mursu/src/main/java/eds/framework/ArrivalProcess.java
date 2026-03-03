package eds.framework;

import eds.model.EventType;
import eds.model.Order;
import eduni.distributions.ContinuousGenerator;
import eduni.distributions.DiscreteGenerator;

public class ArrivalProcess {
	// Generators and event states to model arrival process
	private ContinuousGenerator generator;
	private EventList eventList;
	private EventType type;
	private DiscreteGenerator sideGenerator;
	private DiscreteGenerator typeGenerator;
	private ContinuousGenerator priceGenerator;
	private DiscreteGenerator sizeGenerator;
    private double midPrice;
    private double tickSize;
    private double halfSpread;

	public ArrivalProcess(
		// Alustus näille
			ContinuousGenerator arrivalGenerator,
			EventList eventList,
			EventType type,
			DiscreteGenerator sideGenerator,
			DiscreteGenerator typeGenerator,
			ContinuousGenerator priceGenerator,
			DiscreteGenerator sizeGenerator
	) {
		this(arrivalGenerator, eventList, type, sideGenerator, typeGenerator, priceGenerator, sizeGenerator, 100.0, 0.01);
	}

	public ArrivalProcess(
			ContinuousGenerator arrivalGenerator,
			EventList eventList,
			EventType type,
			DiscreteGenerator sideGenerator,
			DiscreteGenerator typeGenerator,
			ContinuousGenerator priceGenerator,
			DiscreteGenerator sizeGenerator,
			double initialMidPrice,
			double tickSize
	) {
		this.generator = arrivalGenerator;
		this.eventList = eventList;
		this.type = type;
		this.sideGenerator = sideGenerator;
		this.typeGenerator = typeGenerator;
		this.priceGenerator = priceGenerator;
		this.sizeGenerator = sizeGenerator;
		this.midPrice = initialMidPrice;
		this.tickSize = tickSize;
		this.halfSpread = tickSize;
	}

	public void generateNext() {
		// Generoi uusi toimeksianto
		double now = Clock.getInstance().getTime();
		Order.Side side = sideGenerator.sample() == 1 ? Order.Side.BUY : Order.Side.SELL;
		Order.Type orderType = typeGenerator.sample() == 1 ? Order.Type.LIMIT : Order.Type.MARKET;
		midPrice = Math.max(tickSize, midPrice + priceGenerator.sample());
		double rawPrice = side == Order.Side.BUY ? midPrice - halfSpread : midPrice + halfSpread;
		double roundedPrice = Math.round(rawPrice / tickSize) * tickSize;
		double price = orderType == Order.Type.LIMIT ? Math.max(tickSize, roundedPrice) : 0.0;
		int size = (int) Math.max(1, sizeGenerator.sample());
		Order order = new Order(side, orderType, price, size, now);

		// Uusi tapahtuma
		Event event = new Event(type, now + generator.sample(), order);
		eventList.add(event);
	}

}