package de.schmidt.mvg.route;

import de.schmidt.mvg.traffic.Station;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public class RouteOptions implements Serializable {
	private final Map<String, String> properties;

	private RouteOptions(Map<String, String> properties) {
		this.properties = properties;
	}

	private RouteOptions() {
		this.properties = Collections.unmodifiableMap(Collections.emptyMap());
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	private RouteOptions withKeyValueAdded(String key, String value) {
		//builds up the RouteOptions for the api request (from, to, date/time)
		Map<String, String> map = new HashMap<>(getProperties());
		map.put(key, value);
		return new RouteOptions(Collections.unmodifiableMap(map));
	}

	public static RouteOptions getBase() {
		return new RouteOptions();
	}

	public RouteOptions withStart(Station start) {
		return withStart(start.getId());
	}

	public RouteOptions withStart(String startId) {
		if (startId.equals("")) {
			return this;
		} else {
			return withKeyValueAdded("fromStation", startId);
		}
	}

	public RouteOptions withDestination(Station destination) {
		return withDestination(destination.getId());
	}

	public RouteOptions withDestination(String destinationId) {
		if (destinationId.equals("")) {
			return this;
		} else {
			return withKeyValueAdded("toStation", destinationId);
		}
	}

	public RouteOptions withTime(Date date, boolean departure) {
		return withTime(String.valueOf(date.getTime()), String.valueOf(departure));
	}

	public RouteOptions withTime(String rawDate, String rawDeparture) {
		if (rawDate.equals("") || rawDeparture.equals("")) {
			return this;
		} else {
			return withKeyValueAdded("time", rawDate)
					.withKeyValueAdded("arrival", rawDeparture);
		}
	}

	public String getParameterString() {
		//returns the string used in the request url
		return getProperties()
				.entrySet()
				.stream()
				.map(e -> e.getKey() + "=" + e.getValue())
				.collect(Collectors.joining("&"));
	}

	public static RouteOptions fromParameterString(String params) {
		Map<String, String> raw = Arrays
				.stream(params.split("&"))
				.map(str -> str.split("="))
				.collect(Collectors.toMap(arr -> arr[0], arr -> arr[1]));

		return getBase()
				.withStart(raw.getOrDefault("fromStation", ""))
				.withDestination(raw.getOrDefault("toStation", ""))
				.withTime(raw.getOrDefault("time", ""), raw.getOrDefault("arrival", ""));
	}

	@Override
	public String toString() {
		return getParameterString();
	}
}
