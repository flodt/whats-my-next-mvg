package de.schmidt.mvg.route;

import android.util.Log;
import de.schmidt.mvg.traffic.Station;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

public class RouteStationSelection {
	private final Station start;
	private final Station destination;

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
		String splitter = "%";
		return start.getId() + splitter + start.getName() + splitter + start.getLatitude() + splitter + start.getLongitude() + splitter +
				destination.getId() + splitter + destination.getName() + splitter + destination.getLatitude() + splitter + destination.getLongitude();
	}

	public static RouteStationSelection unwrapFromString(String str) {
		Log.d("PreferenceManager", "unwrapFromString: " + str);

		if (str.length() == 0) return null;

		String[] split = str.split("%");
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
