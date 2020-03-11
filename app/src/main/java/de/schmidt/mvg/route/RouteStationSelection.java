package de.schmidt.mvg.route;

import de.schmidt.mvg.traffic.Station;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

public class RouteStationSelection {
	private Station start;
	private Station destination;

	private RouteStationSelection(Station start, Station destination) {
		this.start = start;
		this.destination = destination;
	}

	public Station getStart() {
		return start;
	}

	public Station getDestination() {
		return destination;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		RouteStationSelection that = (RouteStationSelection) o;
		return Objects.equals(start, that.start) &&
				Objects.equals(destination, that.destination);
	}

	@Override
	public int hashCode() {
		return Objects.hash(start, destination);
	}

	public static RouteStationSelection fromRoute(Station from, Station to) {
		return new RouteStationSelection(from, to);
	}

	@Override
	public String toString() {
		return "RouteStationSelection{" +
				"start=" + start +
				", destination=" + destination +
				'}';
	}

	public String wrapToString() {
		return start.getId() + ";" + start.getName() + ";" + start.getLatitude() + ";" + start.getLongitude() + ";" +
				destination.getId() + ";" + destination.getName() + ";" + destination.getLatitude() + ";" + destination.getLongitude();
	}

	public static RouteStationSelection unwrapFromString(String str) {
		String[] split = str.split(";");
		return new RouteStationSelection(
				new Station(
						split[0], split[1], Double.parseDouble(split[2]), Double.parseDouble(split[3])
				),
				new Station(
						split[4], split[5], Double.parseDouble(split[6]), Double.parseDouble(split[7])
				)
		);
	}
}
