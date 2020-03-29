package de.schmidt.mvg;

import android.location.Location;
import android.util.Log;
import de.schmidt.mvg.interrupt.Interruption;
import de.schmidt.mvg.route.RouteConnection;
import de.schmidt.mvg.route.RouteOptions;
import de.schmidt.mvg.traffic.Departure;
import de.schmidt.mvg.traffic.Station;
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

	public static final String URL_NETWORK_MAP = "https://www.mvv-muenchen.de/fileadmin/mediapool/03-Plaene_Bahnhoefe/Netzplaene/MVV_Netzplan_S_U_R.pdf";

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

	/**
	 * Executes the API request with the passed URL.
	 * @param rawUrl the request URL (see the fields in this class)
	 * @return the string returned by the server
	 */
	private String executeRequest(String rawUrl) {
		try {
			//open connection
			URL url = new URL(rawUrl);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.setRequestProperty("Accept", "application/json");
			connection.setRequestProperty("Accept-Encoding", "identity");

			Log.d(TAG, "REQUEST URL: " + rawUrl);

			//read and return the response
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

	/**
	 * Get the nearest station to a specific Location.
	 * @param location The location to search for
	 * @return closest station to that location
	 * @throws JSONException on invalid response
	 */
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

	/**
	 * Return only the next Departure at a Station.
	 * @param station the Station to scan
	 * @return next departure
	 * @throws JSONException on invalid response
	 */
	public Departure getNextDepartureAtStation(Station station) throws JSONException {
		return getNextDeparturesAtStation(station)[0];
	}

	/**
	 * Return only the next Departure at a Station.
	 * @param station the Station to scan
	 * @param exclusions The transport means to exclude
	 * @return next departure
	 * @throws JSONException on invalid response
	 */
	public Departure getNextDepartureAtStation(Station station, Set<String> exclusions) throws JSONException {
		return getNextDeparturesAtStation(station, exclusions)[0];
	}

	/**
	 * Return the next Departures at a Station.
	 * @param station the Station to scan
	 * @return next departures
	 * @throws JSONException on invalid response
	 */
	public Departure[] getNextDeparturesAtStation(Station station) throws JSONException {
		return getNextDeparturesAtStation(station, Collections.emptySet());
	}

	/**
	 * Return the next Departures at a Station.
	 * @param station the Station to scan
	 * @param exclusions The transport means to exclude
	 * @return next departures
	 * @throws JSONException on invalid response
	 */
	public Departure[] getNextDeparturesAtStation(Station station, Set<String> exclusions) throws JSONException {
		String url = URL_DEPARTURE_BY_ID.replace("{id}", station.getId());
		String response = executeRequest(url);

		JSONObject json = new JSONObject(response);

		//get and parse the departure array
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
						single.getString("product"),
						single.getString("departureId"),
						single.getString("platform")
				);
			} catch (JSONException e) {
				result[i] = null;
			}
		}

		//filter out null objects, handle exclusions
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

	/**
	 * Get the Station object by name.
	 * @param name the Station name
	 * @return station object for that name
	 * @throws JSONException on invalid response
	 */
	public Station getStationByName(String name) throws JSONException {
		String url = URL_STATION_BY_NAME.replace("{name}", name)
				.replace(" ", "%20");
		String response = executeRequest(url);

		JSONObject json = new JSONObject(response);
		JSONArray locations = json.getJSONArray("locations");

		JSONObject station = locations.getJSONObject(0);

		return new Station(station.getString("id"),
						   station.getString("name"),
						   station.getDouble("latitude"),
						   station.getDouble("longitude"));
	}

	/**
	 * Get a number of autocomplete suggestions for a given input Station name.
	 * @param input partial station name input
	 * @param count number of suggestions to return
	 * @return String[] of suggestions
	 */
	public String[] getAutocompleteSuggestionsForInput(String input, int count) {
		//request stations by name
		String url = URL_STATION_BY_NAME.replace("{name}", input)
				.replace(" ", "%20");
		String response = executeRequest(url);

		try {
			JSONObject json = new JSONObject(response);
			JSONArray locations = json.getJSONArray("locations");

			//take count many objects from the passed locations and add them to the list
			List<String> result = new ArrayList<>();
			for (int i = 0; i < locations.length() && i < count; i++) {
				try {
					JSONObject station = locations.getJSONObject(i);
					result.add(station.getString("name"));
				} catch (JSONException ignored) {
					//intentionally left blank
				}
			}

			//convert the list to an array
			return result.toArray(new String[0]);
		} catch (JSONException e) {
			Log.e(TAG, "getAutocompleteSuggestionsForInput: json error", e);
			//upon exception: fail silently and return no suggestions
			return new String[0];
		}
	}

	/**
	 * Get Station object by passed ID.
	 * @param id ID String as used by the MVG API.
	 * @return the Station object for that ID
	 * @throws JSONException on invalid response
	 */
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

	/**
	 * Gets the current List of Interruptions as given by the API.
	 * @return list of live interruptions
	 * @throws JSONException on invalid response
	 */
	public List<Interruption> getInterruptions() throws JSONException {
		String response = executeRequest(URL_INTERRUPTIONS);

		JSONObject json = new JSONObject(response);
		JSONArray interruptions = json.getJSONArray("interruption");

		//parse list of interruptions
		List<Interruption> result = new ArrayList<>();
		for (int i = 0; i < interruptions.length(); i++) {
			JSONObject singleInterruption = interruptions.getJSONObject(i);
			result.add(Interruption.ofJSON(singleInterruption));
		}

		return result;
	}

	/**
	 * Get the route planning list of alternative connections for a given journey in the network.
	 * @param options The RouteOptions (start/end/time) for the routing
	 * @return a list of RouteConnections
	 * @throws JSONException on invalid response
	 */
	public List<RouteConnection> getRoute(RouteOptions options) throws JSONException {
		String url = URL_ROUTING + options.getParameterString();
		String response = executeRequest(url);

		JSONObject json = new JSONObject(response);
		JSONArray jConnections = json.getJSONArray("connectionList");

		//parse connections
		List<RouteConnection> connections = new ArrayList<>();
		for (int i = 0; i < jConnections.length(); i++) {
			connections.add(i, RouteConnection.fromJSON(jConnections.getJSONObject(i)));
		}

		return Collections.unmodifiableList(connections);
	}
}
