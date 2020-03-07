package de.schmidt.mvg.traffic;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.util.Objects;

public class Station implements Serializable {
	private final String id;
	private final String name;
	private final double latitude;
	private final double longitude;

	public Station(String id, String name, double latitude, double longitude) {
		this.id = id;
		this.name = name;
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
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

	@Override
	public String toString() {
		return "Station{" +
				"id='" + id + '\'' +
				", name='" + name + '\'' +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Station station = (Station) o;
		return Objects.equals(id, station.id) &&
				Objects.equals(name, station.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name);
	}
}
