package de.schmidt.mvg.route;

import android.graphics.Color;
import de.schmidt.mvg.traffic.Station;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RouteConnection implements Serializable {
	private final Station from;
	private final Station to;
	private final Date departure;
	private final Date arrival;
	private final List<RouteConnectionPart> connectionParts;

	private RouteConnection(Station from, Station to, Date departure, Date arrival, List<RouteConnectionPart> connectionParts) {
		this.from = from;
		this.to = to;
		this.departure = departure;
		this.arrival = arrival;
		this.connectionParts = connectionParts;
	}

	public Station getFrom() {
		return from;
	}

	public Station getTo() {
		return to;
	}

	public Date getDepartureTime() {
		return departure;
	}

	public Date getArrivalTime() {
		return arrival;
	}

	public int getFirstColor() {
		return Color.parseColor(connectionParts.get(0).getColor().getPrimary());
	}

	public int getLastColor() {
		return Color.parseColor(connectionParts.get(connectionParts.size() - 1).getColor().getPrimary());
	}

	public long getDeltaToDepartureInMinutes() {
		long duration = getDepartureTime().getTime() - System.currentTimeMillis();
		long diff = TimeUnit.MILLISECONDS.toMinutes(duration);
		return Math.max(diff, 0);
	}

	public List<RouteConnectionPart> getConnectionParts() {
		return connectionParts;
	}

	public static RouteConnection fromJSON(JSONObject json) throws JSONException {
		//get from station
		JSONObject jFrom = json.getJSONObject("from");
		Station from = new Station(
				jFrom.getString("id"),
				jFrom.getString("name"),
				jFrom.getDouble("latitude"),
				jFrom.getDouble("longitude")
		);

		//get to station
		JSONObject jTo = json.getJSONObject("to");
		Station to = new Station(
				jTo.getString("id"),
				jTo.getString("name"),
				jTo.getDouble("latitude"),
				jTo.getDouble("longitude")
		);

		//parse departure and arrival times
		long departure = json.getLong("departure");
		long arrival = json.getLong("arrival");

		//now get the individual connection parts (between interchanges)
		List<RouteConnectionPart> partList = new ArrayList<>();
		JSONArray connectionPartList = json.getJSONArray("connectionPartList");
		for (int i = 0; i < connectionPartList.length(); i++) {
			//parse and add each part to the list
			JSONObject part = connectionPartList.getJSONObject(i);
			partList.add(RouteConnectionPart.fromJSON(part));
		}

		//construct the single route connection (one itinerary)
		return new RouteConnection(
				from,
				to,
				new Date(departure),
				new Date(arrival),
				Collections.unmodifiableList(partList)
		);
	}

	@Override
	public String toString() {
		return "RouteConnection{" +
				"from=" + from +
				", to=" + to +
				", departure=" + departure +
				", arrival=" + arrival +
				", connectionParts=" + connectionParts +
				'}';
	}

	public long getDurationInMinutes() {
		long duration = getArrivalTime().getTime() - getDepartureTime().getTime();
		return TimeUnit.MILLISECONDS.toMinutes(duration);
	}
}
