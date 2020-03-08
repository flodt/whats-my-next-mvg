package de.schmidt.mvg.route;

import de.schmidt.mvg.traffic.Station;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class RouteOptions implements Serializable {
	private final Map<String, String> properties;

	private RouteOptions(Map<String, String> properties) {
		this.properties = properties;
	}

	private RouteOptions() {
		this.properties = Collections.unmodifiableMap(Collections.emptyMap());
	}

	private Map<String, String> getProperties() {
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
		return withKeyValueAdded("fromStation", start.getId());
	}

	public RouteOptions withDestination(Station destination) {
		return withKeyValueAdded("toStation", destination.getId());
	}

	public RouteOptions withTime(Date date, boolean departure) {
		return withKeyValueAdded("time", String.valueOf(date.getTime()))
				.withKeyValueAdded("arrival", String.valueOf(!departure));
	}

	public String getParameterString() {
		//returns the string used in the request url
		return getProperties()
				.entrySet()
				.stream()
				.map(e -> e.getKey() + "=" + e.getValue())
				.collect(Collectors.joining("&"));
	}

	@Override
	public String toString() {
		return getParameterString();
	}
}
