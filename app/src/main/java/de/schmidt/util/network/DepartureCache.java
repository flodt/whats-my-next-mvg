package de.schmidt.util.network;

import de.schmidt.mvg.Departure;

import java.util.Collections;
import java.util.List;

public class DepartureCache {
	private static final DepartureCache instance = new DepartureCache();
	private List<Departure> departures;

	private DepartureCache() {
		departures = Collections.emptyList();
	}

	public static DepartureCache getInstance() {
		return instance;
	}

	public List<Departure> getCache() {
		return departures;
	}

	public void setCache(List<Departure> departures) {
		this.departures = departures;
	}
}
