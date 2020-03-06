package de.schmidt.mvg;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class RouteConnection {
	private Station from;
	private Station to;
	private Date departure;
	private Date arrival;
	private List<RouteConnectionPart> connectionParts;

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

	public Date getDeparture() {
		return departure;
	}

	public Date getArrival() {
		return arrival;
	}

	public List<RouteConnectionPart> getConnectionParts() {
		return connectionParts;
	}

	public static RouteConnection fromJSON(JSONObject json) throws JSONException {
		JSONObject jFrom = json.getJSONObject("from");
		Station from = new Station(
				jFrom.getString("id"),
				jFrom.getString("name"),
				jFrom.getDouble("latitude"),
				jFrom.getDouble("longitude")
		);

		JSONObject jTo = json.getJSONObject("to");
		Station to = new Station(
				jTo.getString("id"),
				jTo.getString("name"),
				jTo.getDouble("latitude"),
				jTo.getDouble("longitude")
		);

		long departure = json.getLong("departure");
		long arrival = json.getLong("arrival");

		List<RouteConnectionPart> partList = new ArrayList<>();
		JSONArray connectionPartList = json.getJSONArray("connectionPartList");
		for (int i = 0; i < connectionPartList.length(); i++) {
			JSONObject part = connectionPartList.getJSONObject(i);
			partList.add(i, RouteConnectionPart.fromJSON(part));
		}

		return new RouteConnection(
				from,
				to,
				new Date(departure),
				new Date(arrival),
				Collections.unmodifiableList(partList)
		);
	}
}
