package eds.framework;

// DONE (by Ks)
// TODO Order needs to be added when it's ready

public class Event implements Comparable<Event> {

	// Tapahtuman tyyppi (katso enum EventType)
	private final IEventType type;

	// Simulaatioaika, jolloin tapahtuma suoritetaan
	private final double time;

	// Tähän tapahtumaan liittyvä tilaus (Order)
	private final Order order;

	// Luo uuden tapahtuman annetulla tyypillä, ajalla ja tilauksella
	public Event(IEventType type, double time, Order order) {
		this.type = type;
		this.time = time;
		this.order = order;
	}

	// Palauttaa tapahtuman tyypin
	public IEventType getType() {
		return type;
	}

	// Palauttaa tapahtuman ajan
	public double getTime() {
		return time;
	}

	// Palauttaa tapahtumaan liittyvän tilauksen
	public Order getOrder() {
		return order;
	}

	// Vertaa tapahtumia ajan perusteella (käytetään prioriteettijonossa)
	@Override
	public int compareTo(Event other) {
		return Double.compare(this.time, other.time);
	}
}
