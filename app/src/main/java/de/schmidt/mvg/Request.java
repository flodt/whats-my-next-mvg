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
import java.util.Arrays;
import java.util.Objects;

public class Request {
	protected static final String API_KEY = "5af1beca494712ed38d313714d4caff6";

	protected static final String query_url_name = "https://www.mvg.de/api/fahrinfo/location/queryWeb?q={name}";
	protected static final String query_url_id = "https://www.mvg.de/api/fahrinfo/location/query?q={id}";
	protected static final String departure_url = "https://www.mvg.de/api/fahrinfo/departure/{id}?footway=0";
	protected static final String nearby_url = "https://www.mvg.de/api/fahrinfo/location/nearby?latitude={lat}&longitude={lon}";
	protected static final String routing_url = "https://www.mvg.de/api/fahrinfo/routing/?";
	protected static final String interruptions_url = "https://www.mvg.de/.rest/betriebsaenderungen/api/interruptions";
	protected static final String id_prefix = "de:09162:";
	private static final String TAG = "Request";

	public String executeRequest(String rawUrl) {
		try {
			URL url = new URL(rawUrl);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.setRequestProperty("Accept", "application/json");
			connection.setRequestProperty("Accept-Encoding", "identity");
			//connection.setRequestProperty("X-MVG-Authorization-Key", API_KEY);
			//connection.setDoOutput(true);

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

	public Departure[] getNextDeparturesAtStation(Station station) throws JSONException {
		String url = departure_url.replace("{id}", station.getId());
		String response = executeRequest(url);

		JSONObject json = new JSONObject(response);

		JSONArray departures = json.getJSONArray("departures");
		int len = departures.length();
		Departure[] result = new Departure[len];
		for (int i = 0; i < len; i++) {
			JSONObject single = departures.getJSONObject(i);

			try {
				result[i] = new Departure(
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
				.filter(dep -> !dep.getProduct().contains("BUS"))
				.toArray(Departure[]::new);
	}

	public Station getNearestStation(Location location) throws JSONException {
		String url = nearby_url
				.replace("{lat}", Double.toString(location.getLatitude()))
				.replace("{lon}", Double.toString(location.getLongitude()));
		String response = executeRequest(url);

		JSONObject json = new JSONObject(response);

		//capture the first station id
		JSONObject firstStation = json.getJSONArray("locations").getJSONObject(0);

		return new Station(firstStation.getString("id"), firstStation.getString("name"));
	}
}
