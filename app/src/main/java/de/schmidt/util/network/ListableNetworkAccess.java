package de.schmidt.util.network;

import android.os.AsyncTask;
import android.util.Log;
import de.schmidt.mvg.*;
import de.schmidt.whatsnext.viewsupport.list.SwitchStationListItem;
import de.schmidt.mvg.traffic.Departure;
import de.schmidt.mvg.traffic.Station;
import de.schmidt.util.caching.DepartureCache;
import de.schmidt.util.managers.LocationManager;
import de.schmidt.whatsnext.activities.DepartureListActivity;
import org.json.JSONException;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Set;

public class ListableNetworkAccess extends AsyncTask<Void, Void, Departure[]> {
	private static final String TAG = "ListableNetworkAccessLog";
	private final WeakReference<DepartureListActivity> act;
	private final SwitchStationListItem selectedStation;
	private final Set<String> exclusions;

	public ListableNetworkAccess(DepartureListActivity context, SwitchStationListItem selectedStation, Set<String> excludableTransportMeans) {
		this.act = new WeakReference<>(context);
		this.selectedStation = selectedStation;
		this.exclusions = excludableTransportMeans;
	}

	@Override
	protected void onPostExecute(Departure[] departures) {
		//save to cache and trigger UI update
		super.onPostExecute(departures);
		DepartureCache.getInstance().setCache(Arrays.asList(departures));
		if (act.get() != null) act.get().handleUIUpdate(Arrays.asList(departures));
	}

	@Override
	protected Departure[] doInBackground(Void... voids) {
		Station station;

		//if current location is selected, get nearest station, else based on selection
		if (selectedStation.isCurrentLocation()) {
			try {
				//get nearest to location
				station = Requests.instance().getNearestStation(LocationManager.getInstance().getLocation(act.get()));
			} catch (JSONException e) {
				Log.e(TAG, "doInBackground: exception in json parsing with location", e);
				return new Departure[0];
			}
		} else {
			//else the station is fixed with the selection
			station = selectedStation.getFixedStation();
		}

		try {
			return Requests.instance().getNextDeparturesAtStation(station, exclusions);
		} catch (JSONException e) {
			Log.e(TAG, "doInBackground: exception in network access", e);
			return new Departure[0];
		}
	}
}
