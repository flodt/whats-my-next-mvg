package de.schmidt.util.network;

import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;
import androidx.annotation.Nullable;
import de.schmidt.whatsnext.activities.DepartureListActivity;
import de.schmidt.mvg.Departure;
import de.schmidt.mvg.Requests;
import de.schmidt.mvg.Station;
import de.schmidt.whatsnext.R;

import java.lang.ref.WeakReference;
import java.util.Arrays;
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
		DepartureCache.getInstance().setCache(Arrays.asList(departures));
		act.get().handleUIUpdate(Arrays.asList(departures));
	}

	@Override
	protected Departure[] doInBackground(Location... locations) {
		//handle updates
		Location loc = locations[0];

		String[] keys = act.get().getResources().getStringArray(R.array.station_keys);

		if (keys[stationMenuIndex].equals("LOCATION")) {
			Requests requests = Requests.instance();
			try {
				Station nearest = requests.getNearestStation(loc);
				return requests.getNextDeparturesAtStation(nearest, exclusions);
			} catch (Exception e) {
				Log.e(TAG, "onCreate: network access", e);
				return null;
			}
		} else if (keys[stationMenuIndex].equals("BY_NAME")) {
			Requests requests = Requests.instance();
			try {
				Station byName = requests.getStationByName(stationMenuName);
				return requests.getNextDeparturesAtStation(byName, exclusions);
			} catch (Exception e) {
				Log.e(TAG, "onCreate: network access", e);
				return null;
			}
		} else {
			Requests requests = Requests.instance();
			try {
				Station byId = requests.getStationById(keys[stationMenuIndex]);
				return requests.getNextDeparturesAtStation(byId, exclusions);
			} catch (Exception e) {
				Log.e(TAG, "onCreate: network access", e);
				return null;
			}
		}
	}
}
