package de.schmidt.mvg.route;

import de.schmidt.mvg.traffic.Station;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Date;

public class RouteIntermediateStop implements Serializable {
	private final Station station;
	private final Date time;

	private RouteIntermediateStop(Station station, Date time) {
		this.station = station;
		this.time = time;
	}

	public Station getStation() {
		return station;
	}

	public Date getTime() {
		return time;
	}

	public static RouteIntermediateStop fromJSON(JSONObject json) throws JSONException {
		long time = json.getLong("time");
		JSONObject jStation = json.getJSONObject("location");
		Station at = new Station(
				jStation.getString("id"),
				jStation.getString("name"),
				jStation.getDouble("latitude"),
				jStation.getDouble("longitude")
		);

		return new RouteIntermediateStop(at, new Date(time));
	}

	@Override
	public String toString() {
		return "RouteIntermediateStop{" +
				"station=" + station +
				", time=" + time +
				'}';
	}
}
