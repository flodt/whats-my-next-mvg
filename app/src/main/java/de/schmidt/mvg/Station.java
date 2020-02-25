package de.schmidt.mvg;

import java.util.Objects;

public class Station {
	private final String id;
	private final String name;

	public Station(String id, String name) {
		this.id = id;
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return "Station{" +
				"id='" + id + '\'' +
				", name='" + name + '\'' +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Station station = (Station) o;
		return Objects.equals(id, station.id) &&
				Objects.equals(name, station.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name);
	}
}
