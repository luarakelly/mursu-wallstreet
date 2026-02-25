package eds.framework;

import java.util.PriorityQueue;

// DONE (by Ks)

public class EventList {
	// Prioriteettijono, joka järjestää tapahtumat ajan mukaan
	private final PriorityQueue<Event> events = new PriorityQueue<>();

	// Lisää uusi tapahtuma listaan
	public void add(Event event) {
		events.add(event);
	}

	// Poistaa ja palauttaa aikaisimman tapahtuman
	public Event remove() {
		return events.poll();
	}

	// Palauttaa seuraavan tapahtuman ajan
	public double getNextTime() {
		if (events.isEmpty()) {
			throw new IllegalStateException("EventList is empty");
		}
		return events.peek().getTime();
	}

	// Tarkistaa onko lista tyhjä
	public boolean isEmpty() {
		return events.isEmpty();
	}
}