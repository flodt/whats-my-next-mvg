package de.schmidt.mvg.route;

import android.graphics.Color;
import de.schmidt.mvg.traffic.Station;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

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

	public Date getDeparture() {
		return departure;
	}

	public Date getArrival() {
		return arrival;
	}

	public int getFirstColor() {
		return Color.parseColor(connectionParts.get(0).getColor().getPrimary());
	}

	public int getLastColor() {
		return Color.parseColor(connectionParts.get(connectionParts.size() - 1).getColor().getPrimary());
	}

	public long getDeltaInMinutes() {
		Duration diff = Duration.between(
				LocalDateTime.now(),
				LocalDateTime.ofInstant(getDeparture().toInstant(), ZoneId.systemDefault())
		);

		return Math.max(diff.toMinutes(), 0);
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

			//ignore the FOOTWAY types
			//if (!part.getString("connectionPartType").equals("TRANSPORTATION")) continue;

			partList.add(RouteConnectionPart.fromJSON(part));
		}

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

	public long getDuration() {
		Duration diff = Duration.between(
				LocalDateTime.ofInstant(getDeparture().toInstant(), ZoneId.systemDefault()),
				LocalDateTime.ofInstant(getArrival().toInstant(), ZoneId.systemDefault())
		);

		return diff.toMinutes();
	}
}
