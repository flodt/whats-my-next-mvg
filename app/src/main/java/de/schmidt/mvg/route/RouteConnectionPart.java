package de.schmidt.mvg.route;

import android.content.Context;
import de.schmidt.mvg.traffic.LineColor;
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
import java.util.concurrent.TimeUnit;

public class RouteConnectionPart implements Serializable {
	private static final String TAG = "RouteConnectionPart";
	private final Station from;
	private final Station to;
	private final List<RouteIntermediateStop> stops;
	private final List<RoutePathLocation> path;
	private final List<RoutePathLocation> interchangePath;
	private final Date departure;
	private final Date arrival;
	private final int delay;
	private final String line;
	private final String direction;
	private final String departurePlatform;
	private final String arrivalPlatform;
	private final LineColor color;
	private final String departureId;

	public RouteConnectionPart(Station from, Station to, List<RouteIntermediateStop> stops, List<RoutePathLocation> path, List<RoutePathLocation> interchangePath, Date departure, Date arrival, int delay, String line, String direction, String departurePlatform, String arrivalPlatform, String departureId) {
		this.from = from;
		this.to = to;
		this.stops = stops;
		this.path = path;
		this.interchangePath = interchangePath;
		this.departure = departure;
		this.arrival = arrival;
		this.delay = delay;
		this.line = line;
		this.direction = direction;
		this.departurePlatform = departurePlatform;
		this.arrivalPlatform = arrivalPlatform;
		this.color = LineColor.getForLine(line);
		this.departureId = departureId;
	}

	public static RouteConnectionPart fromJSON(JSONObject json) throws JSONException {
		//parse from station
		JSONObject jFrom = json.getJSONObject("from");
		Station from = new Station(
				jFrom.getString("id"),
				jFrom.getString("name"),
				jFrom.getDouble("latitude"),
				jFrom.getDouble("longitude")
		);

		//parse to station
		JSONObject jTo = json.getJSONObject("to");
		Station to = new Station(
				jTo.getString("id"),
				jTo.getString("name"),
				jTo.getDouble("latitude"),
				jTo.getDouble("longitude")
		);

		//parse the path sent by the API (a list of coordinate pairs representing the path taken by the transport)
		List<RoutePathLocation> path = new ArrayList<>();
		JSONArray jPath = json.getJSONArray("path");
		for (int i = 0; i < jPath.length(); i++) {
			path.add(i, RoutePathLocation.fromJSON(jPath.getJSONObject(i)));
		}

		//parse the interchange path sent by the API (a list of coordinate pairs representing the path during interchange)
		List<RoutePathLocation> interchangePath = new ArrayList<>();
		JSONArray jInterPath = json.getJSONArray("interchangePath");
		for (int i = 0; i < jInterPath.length(); i++) {
			interchangePath.add(i, RoutePathLocation.fromJSON(jInterPath.getJSONObject(i)));
		}

		//parse the list of intermediate stops for that part of the connection (station and date pairs)
		List<RouteIntermediateStop> stops = new ArrayList<>();
		try {
			JSONArray jStops = json.getJSONArray("stops");
			for (int i = 0; i < jStops.length(); i++) {
				stops.add(i, RouteIntermediateStop.fromJSON(jStops.getJSONObject(i)));
			}
		} catch (JSONException e) {
			//no stops object found means footway, so we leave the list of stops empty
		}

		//parse departure/arrival times
		long departure = json.getLong("departure");
		long arrival = json.getLong("arrival");

		//delay field might not be present (again for the footway type)
		int delay;
		try {
			delay = json.getInt("delay");
		} catch (JSONException e) {
			delay = 0;
		}

		//handle footway: no value for line
		String line;
		try {
			line = json.getString("label");
		} catch (JSONException e) {
			line = "Walking";
		}

		//handle footway for direction
		String direction;
		try {
			direction = json.getString("destination");
		} catch (JSONException e) {
			direction = "";
		}

		//handle footway for platforms
		String departurePlatform;
		String arrivalPlatform;
		try {
			departurePlatform = json.getString("departurePlatform");
			arrivalPlatform = json.getString("arrivalPlatform");
		} catch (JSONException e) {
			departurePlatform = "";
			arrivalPlatform = "";
		}

		String departureId;
		try {
			departureId = json.getString("departureId");
		} catch (JSONException e) {
			departureId = "";
		}

		return new RouteConnectionPart(
				from,
				to,
				Collections.unmodifiableList(stops),
				Collections.unmodifiableList(path),
				Collections.unmodifiableList(interchangePath),
				new Date(departure),
				new Date(arrival),
				delay,
				line,
				direction,
				departurePlatform,
				arrivalPlatform,
				departureId
		);
	}

	public Station getFrom() {
		return from;
	}

	public Station getTo() {
		return to;
	}

	public List<RouteIntermediateStop> getStops() {
		return stops;
	}

	public List<RoutePathLocation> getPath() {
		return path;
	}

	public List<RoutePathLocation> getInterchangePath() {
		return interchangePath;
	}

	public Date getDepartureTime() {
		return departure;
	}

	public Date getArrivalTime() {
		return arrival;
	}

	public long getDurationInMinutes() {
		long duration = getArrivalTime().getTime() - getDepartureTime().getTime();
		return TimeUnit.MILLISECONDS.toMinutes(duration);
	}

	public int getDelay() {
		return delay;
	}

	public String getLine() {
		return line;
	}

	public String getDirection() {
		return direction;
	}

	public String getDeparturePlatform() {
		return departurePlatform;
	}

	public String getArrivalPlatform() {
		return arrivalPlatform;
	}

	public LineColor getColor() {
		return color;
	}

	public String getDepartureId() {
		return departureId;
	}

	@Override
	public String toString() {
		return "RouteConnectionPart{" +
				"from=" + from +
				", to=" + to +
				", stops=" + stops +
				", path=" + path +
				", interchangePath=" + interchangePath +
				", departure=" + departure +
				", arrival=" + arrival +
				", delay=" + delay +
				", line='" + line + '\'' +
				", direction='" + direction + '\'' +
				", departurePlatform='" + departurePlatform + '\'' +
				", arrivalPlatform='" + arrivalPlatform + '\'' +
				", color=" + color +
				", departureId='" + departureId + '\'' +
				'}';
	}
}
