package de.schmidt.mvg;

import android.location.Location;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class Requests {
	private static final Requests singleton = new Requests();

	private static final String URL_STATION_BY_NAME = "https://www.mvg.de/api/fahrinfo/location/queryWeb?q={name}";
	private static final String URL_STATION_BY_ID = "https://www.mvg.de/api/fahrinfo/location/query?q={id}";
	private static final String URL_DEPARTURE_BY_ID = "https://www.mvg.de/api/fahrinfo/departure/{id}?footway=0";
	private static final String URL_STATIONS_BY_LOCATION = "https://www.mvg.de/api/fahrinfo/location/nearby?latitude={lat}&longitude={lon}";
	private static final String URL_ROUTING = "https://www.mvg.de/api/fahrinfo/routing/?";
	private static final String URL_INTERRUPTIONS = "https://www.mvg.de/.rest/betriebsaenderungen/api/interruptions";
	private static final String ID_PREFIX = "de:09162:";
	private static final String TAG = "Requests";

	public static Requests instance() {
		return singleton;
	}

	private Requests() {
	}

	private String executeRequest(String rawUrl) {
		try {
			URL url = new URL(rawUrl);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.setRequestProperty("Accept", "application/json");
			connection.setRequestProperty("Accept-Encoding", "identity");

			Log.d(TAG, "REQUEST URL: " + rawUrl);

			try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
				StringBuilder sb = new StringBuilder();

				String line;
				while ((line = reader.readLine()) != null) {
					sb.append(line.trim());
				}

				connection.disconnect();

				Log.d(TAG, "SERVER RESPONSE: " + sb.toString());
				return sb.toString();
			}
		} catch (IOException e) {
			Log.e(TAG, "executeRequest: IOException in connection", e);
			return "";
		}
	}

	public Station getNearestStation(Location location) throws JSONException {
		String url = URL_STATIONS_BY_LOCATION
				.replace("{lat}", Double.toString(location.getLatitude()))
				.replace("{lon}", Double.toString(location.getLongitude()));
		String response = executeRequest(url);

		JSONObject json = new JSONObject(response);

		//capture the first station id
		JSONObject firstStation = json.getJSONArray("locations").getJSONObject(0);

		return new Station(firstStation.getString("id"),
						   firstStation.getString("name"),
						   firstStation.getDouble("latitude"),
						   firstStation.getDouble("longitude"));
	}

	public Departure getNextDepartureAtStation(Station station) throws JSONException {
		return getNextDeparturesAtStation(station)[0];
	}

	public Departure getNextDepartureAtStation(Station station, Set<String> exclusions) throws JSONException {
		return getNextDeparturesAtStation(station, exclusions)[0];
	}

	public Departure[] getNextDeparturesAtStation(Station station) throws JSONException {
		return getNextDeparturesAtStation(station, Collections.emptySet());
	}

	public Departure[] getNextDeparturesAtStation(Station station, Set<String> exclusions) throws JSONException {
		String url = URL_DEPARTURE_BY_ID.replace("{id}", station.getId());
		String response = executeRequest(url);

		JSONObject json = new JSONObject(response);

		JSONArray departures = json.getJSONArray("departures");
		int len = departures.length();
		Departure[] result = new Departure[len];
		for (int i = 0; i < len; i++) {
			JSONObject single = departures.getJSONObject(i);

			try {
				result[i] = new Departure(
						station,
						single.getString("label"),
						single.getString("destination"),
						single.getLong("departureTime"),
						single.getString("lineBackgroundColor"),
						single.getInt("delay"),
						single.getString("product")
				);
			} catch (JSONException e) {
				result[i] = null;
			}
		}

		return Arrays.stream(result)
				.filter(Objects::nonNull)
				.filter(dep -> exclusions.stream().noneMatch(mean -> dep.getProduct().equals(mean)))
				.filter(dep -> {
					if (exclusions.contains("BUS")) {
						return !dep.getProduct().contains("BUS");
					} else {
						return true;
					}
				})
				.toArray(Departure[]::new);
	}

	public Station getStationByName(String name) throws JSONException {
		String url = URL_STATION_BY_NAME.replace("{name}", name);
		String response = executeRequest(url);

		JSONObject json = new JSONObject(response);
		JSONArray locations = json.getJSONArray("locations");

		JSONObject station = locations.getJSONObject(0);

		return new Station(station.getString("id"),
						   station.getString("name"),
						   station.getDouble("latitude"),
						   station.getDouble("longitude"));
	}

	public Station getStationById(String id) throws JSONException {
		String url = URL_STATION_BY_ID.replace("{id}", id);
		String response = executeRequest(url);

		JSONObject json = new JSONObject(response);
		JSONArray locations = json.getJSONArray("locations");

		JSONObject station = locations.getJSONObject(0);

		return new Station(station.getString("id"),
						   station.getString("name"),
						   station.getDouble("latitude"),
						   station.getDouble("longitude"));
	}

	public List<Interruption> getInterruptions() throws JSONException {
		String url = URL_INTERRUPTIONS;
		String response = executeRequest(url);

		JSONObject json = new JSONObject(response);
		JSONArray interruptions = json.getJSONArray("interruption");

		List<Interruption> result = new ArrayList<>();
		for (int i = 0; i < interruptions.length(); i++) {
			JSONObject singleInterruption = interruptions.getJSONObject(i);
			result.add(Interruption.ofJSON(singleInterruption));
		}

		return result;
	}
}