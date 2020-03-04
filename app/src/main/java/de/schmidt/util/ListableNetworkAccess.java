package de.schmidt.util;

import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;
import androidx.annotation.Nullable;
import de.schmidt.whatsnext.DepartureListActivity;
import de.schmidt.mvg.Departure;
import de.schmidt.mvg.Request;
import de.schmidt.mvg.Station;
import de.schmidt.whatsnext.R;

import java.lang.ref.WeakReference;
import java.util.Set;

public class ListableNetworkAccess extends AsyncTask<Location, Void, Departure[]> {
	private static final String TAG = "ListableNetworkAccessLog";
	private final WeakReference<DepartureListActivity> act;
	private final int stationMenuIndex;
	private final String stationMenuName;
	private final Set<String> exclusions;


	public ListableNetworkAccess(Context context, int stationMenuIndex,
								 @Nullable String stationMenuName, Set<String> exclusions) {
		this.act = new WeakReference<>((DepartureListActivity) context);
		this.stationMenuIndex = stationMenuIndex;
		this.stationMenuName = stationMenuName;
		this.exclusions = exclusions;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}

	@Override
	protected void onPostExecute(Departure[] departures) {
		super.onPostExecute(departures);
		act.get().handleUIUpdate(departures);
	}

	@Override
	protected Departure[] doInBackground(Location... locations) {
		//handle updates
		Location loc = locations[0];

		String[] keys = act.get().getResources().getStringArray(R.array.station_keys);

		if (keys[stationMenuIndex].equals("LOCATION")) {
			Request request = Request.instance();
			try {
				Station nearest = request.getNearestStation(loc);
				return request.getNextDeparturesAtStation(nearest, exclusions);
			} catch (Exception e) {
				Log.e(TAG, "onCreate: network access", e);
				return null;
			}
		} else if (keys[stationMenuIndex].equals("BY_NAME")) {
			Request request = Request.instance();
			try {
				Station byName = request.getStationByName(stationMenuName);
				return request.getNextDeparturesAtStation(byName, exclusions);
			} catch (Exception e) {
				Log.e(TAG, "onCreate: network access", e);
				return null;
			}
		} else {
			Request request = Request.instance();
			try {
				Station byId = request.getStationById(keys[stationMenuIndex]);
				return request.getNextDeparturesAtStation(byId, exclusions);
			} catch (Exception e) {
				Log.e(TAG, "onCreate: network access", e);
				return null;
			}
		}
	}
}
