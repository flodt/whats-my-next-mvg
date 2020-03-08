package de.schmidt.util.network;

import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;
import androidx.annotation.Nullable;
import de.schmidt.mvg.traffic.Departure;
import de.schmidt.mvg.Requests;
import de.schmidt.mvg.traffic.Station;
import de.schmidt.util.caching.DepartureCache;
import de.schmidt.whatsnext.R;
import de.schmidt.whatsnext.activities.DepartureSingleActivity;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

public class SingleNetworkAccess extends AsyncTask<Location, Void, Departure> {
	private static final String TAG = "NetworkAccessLog";
	private final WeakReference<DepartureSingleActivity> act;
	private final int stationMenuIndex;
	private final String stationMenuName;
	private final Set<String> exclusions;


	public SingleNetworkAccess(Context context, int stationMenuIndex,
							   @Nullable String stationMenuName, Set<String> exclusions) {
		this.act = new WeakReference<>((DepartureSingleActivity) context);
		this.stationMenuIndex = stationMenuIndex;
		this.stationMenuName = stationMenuName;
		this.exclusions = exclusions;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}

	@Override
	protected void onPostExecute(Departure departure) {
		//handle UI update
		super.onPostExecute(departure);
		act.get().handleUIUpdate(
				(departure == null) ? Collections.emptyList() : Collections.singletonList(departure)
		);
	}

	@Override
	protected Departure doInBackground(Location... locations) {
		//handle request based on selected station
		Location loc = locations[0];

		String[] keys = act.get().getResources().getStringArray(R.array.station_keys);

		if (keys[stationMenuIndex].equals("LOCATION")) {
			Requests requests = Requests.instance();
			try {
				Station nearest = requests.getNearestStation(loc);
				Departure[] next = requests.getNextDeparturesAtStation(nearest, exclusions);
				DepartureCache.getInstance().setCache(Arrays.asList(next));
				return next[0];
			} catch (Exception e) {
				Log.e(TAG, "onCreate: network access", e);
				return null;
			}
		} else if (keys[stationMenuIndex].equals("BY_NAME")) {
			Requests requests = Requests.instance();
			try {
				Station byName = requests.getStationByName(stationMenuName);
				Departure[] next = requests.getNextDeparturesAtStation(byName, exclusions);
				DepartureCache.getInstance().setCache(Arrays.asList(next));
				return next[0];
			} catch (Exception e) {
				Log.e(TAG, "onCreate: network access", e);
				return null;
			}
		} else {
			Requests requests = Requests.instance();
			try {
				Station byId = requests.getStationById(keys[stationMenuIndex]);
				Departure[] next = requests.getNextDeparturesAtStation(byId, exclusions);
				DepartureCache.getInstance().setCache(Arrays.asList(next));
				return next[0];
			} catch (Exception e) {
				Log.e(TAG, "onCreate: network access", e);
				return null;
			}
		}
	}
}
