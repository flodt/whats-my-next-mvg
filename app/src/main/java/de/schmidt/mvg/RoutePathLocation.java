package de.schmidt.mvg;

import org.json.JSONException;
import org.json.JSONObject;

public class RoutePathLocation {
	private double latitude;
	private double longitude;

	private RoutePathLocation(double latitude, double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public static RoutePathLocation fromJSON(JSONObject loc) throws JSONException {
		return new RoutePathLocation(
				loc.getDouble("latitude"),
				loc.getDouble("longitude")
		);
	}
}
