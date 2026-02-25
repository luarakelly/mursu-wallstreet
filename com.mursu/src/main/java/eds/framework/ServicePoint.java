package eds.framework;

import eduni.distributions.Negexp;
import java.util.LinkedList;

// DONE (by Ks)
import eds.model.Order;

public class ServicePoint {
	// Jonorakenne tilauksille
	private final LinkedList<Order> queue = new LinkedList<>();

	// Viittaus tapahtumalistaan
	private final EventList eventList;

	// Tapahtumatyyppi, joka luodaan palvelun päättyessä
	private final IEventType completionType;

	// Palveluajan generaattori
	private final Negexp generator;

	// Onko palvelupiste varattu
	private boolean busy = false;


	// Tässä kostruktorissa generaattori annetaan valmiina ulkopuolelta jos on tarve
	public ServicePoint(Negexp generator, EventList eventList, IEventType completionType) {
		this.generator = generator;
		this.eventList = eventList;
		this.completionType = completionType;
	}

	// Tämä kostruktori on default versio: luodaan palvelupisteen käyttäen suoraan eksponentiaalijakaumaa, se sopii tilanteseen hyvin
	public ServicePoint(double meanServiceTime, EventList eventList, IEventType completionType) {
		this.generator = new Negexp(meanServiceTime);
		this.eventList = eventList;
		this.completionType = completionType;
	}

	// Lisää tilaus jonoon
	public void add(Order order) {
		// Lisää tilaus palvelupisteen jonoon odottamaan käsittelyä
		queue.add(order);

		// Jos ei varattu, aloitetaan palvelu
		if (!isBusy()) {
			startService();
		}
	}

	// Aloittaa palvelun ensimmäiselle jonossa olevalle
	private void startService() {
		if (queue.isEmpty()) {
			return;
		}

		busy = true;

		double serviceTime = generator.sample();
		double completionTime = Clock.getInstance().getTime() + serviceTime;

		// Lisätään palvelun päättymistapahtuma
		Event event = new Event(completionType, completionTime, queue.peek());
		eventList.add(event);
	}

	// Poistaa palvellun tilauksen jonosta
	public Order finishService() {
		Order finished = queue.poll();
		busy = false;

		if (hasOrders()) {
			startService();
		}

		return finished;
	}

	// Tarkistaa onko palvelupiste varattu
	public boolean isBusy() {
		return busy;
	}

	// Tarkistaa onko jonossa tilauksia
	public boolean hasOrders() {
		return !queue.isEmpty();
	}
}