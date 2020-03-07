package de.schmidt.mvg.route;

import com.google.android.gms.maps.model.LatLng;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class RoutePathLocation implements Serializable {
	private final double latitude;
	private final double longitude;

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

	public LatLng getLatLongForMaps() {
		return new LatLng(latitude, longitude);
	}

	public static RoutePathLocation fromJSON(JSONObject loc) throws JSONException {
		return new RoutePathLocation(
				loc.getDouble("latitude"),
				loc.getDouble("longitude")
		);
	}

	@Override
	public String toString() {
		return "RoutePathLocation{" +
				"latitude=" + latitude +
				", longitude=" + longitude +
				'}';
	}
}
