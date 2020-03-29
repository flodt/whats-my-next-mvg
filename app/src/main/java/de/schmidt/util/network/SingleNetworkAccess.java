package de.schmidt.util.network;

import android.os.AsyncTask;
import android.util.Log;
import de.schmidt.whatsnext.viewsupport.list.SwitchStationListItem;
import de.schmidt.mvg.traffic.Departure;
import de.schmidt.mvg.Requests;
import de.schmidt.mvg.traffic.Station;
import de.schmidt.util.caching.DepartureCache;
import de.schmidt.util.managers.LocationManager;
import de.schmidt.whatsnext.activities.DepartureSingleActivity;
import org.json.JSONException;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

public class SingleNetworkAccess extends AsyncTask<Void, Void, Departure> {
	private static final String TAG = "NetworkAccessLog";
	private final WeakReference<DepartureSingleActivity> act;
	private final SwitchStationListItem selectedStation;
	private final Set<String> exclusions;


	public SingleNetworkAccess(DepartureSingleActivity context, SwitchStationListItem selectedStation, Set<String> excludableTransportMeans) {
		this.act = new WeakReference<>(context);
		this.selectedStation = selectedStation;
		this.exclusions = excludableTransportMeans;
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
	protected Departure doInBackground(Void... voids) {
		Station station;

		//if location is selected, get that, else based on selected station
		if (selectedStation.isCurrentLocation()) {
			try {
				//get station based on current location
				station = Requests.instance().getNearestStation(LocationManager.getInstance().getLocation(act.get()));
			} catch (JSONException e) {
				Log.e(TAG, "doInBackground: exception in json parsing with location", e);
				return null;
			}
		} else {
			//get station from selection
			station = selectedStation.getFixedStation();
		}

		try {
			//pull departures
			Departure[] next = Requests.instance().getNextDeparturesAtStation(station, exclusions);

			//set cache accordingly
			DepartureCache.getInstance().setCache(Arrays.asList(next));

			//return the next departure
			return next[0];
		} catch (JSONException e) {
			Log.e(TAG, "doInBackground: exception in pulling departures", e);
			return null;
		}
	}
}
