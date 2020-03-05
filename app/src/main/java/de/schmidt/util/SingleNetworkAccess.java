package de.schmidt.util;

import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;
import androidx.annotation.Nullable;
import de.schmidt.mvg.Departure;
import de.schmidt.mvg.Requests;
import de.schmidt.mvg.Station;
import de.schmidt.whatsnext.R;
import de.schmidt.whatsnext.SingleDepartureActivity;

import java.lang.ref.WeakReference;
import java.util.Set;

public class SingleNetworkAccess extends AsyncTask<Location, Void, Departure> {
	private static final String TAG = "NetworkAccessLog";
	private final WeakReference<SingleDepartureActivity> act;
	private final int stationMenuIndex;
	private final String stationMenuName;
	private final Set<String> exclusions;


	public SingleNetworkAccess(Context context, int stationMenuIndex,
							   @Nullable String stationMenuName, Set<String> exclusions) {
		this.act = new WeakReference<>((SingleDepartureActivity) context);
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
		super.onPostExecute(departure);
		act.get().handleUIUpdate(departure, departure == null);
	}

	@Override
	protected Departure doInBackground(Location... locations) {
		//handle updates
		Location loc = locations[0];

		String[] keys = act.get().getResources().getStringArray(R.array.station_keys);

		if (keys[stationMenuIndex].equals("LOCATION")) {
			Requests requests = Requests.instance();
			try {
				Station nearest = requests.getNearestStation(loc);
				return requests.getNextDepartureAtStation(nearest, exclusions);
			} catch (Exception e) {
				Log.e(TAG, "onCreate: network access", e);
				return null;
			}
		} else if (keys[stationMenuIndex].equals("BY_NAME")) {
			Requests requests = Requests.instance();
			try {
				Station byName = requests.getStationByName(stationMenuName);
				return requests.getNextDepartureAtStation(byName, exclusions);
			} catch (Exception e) {
				Log.e(TAG, "onCreate: network access", e);
				return null;
			}
		} else {
			Requests requests = Requests.instance();
			try {
				Station byId = requests.getStationById(keys[stationMenuIndex]);
				return requests.getNextDepartureAtStation(byId, exclusions);
			} catch (Exception e) {
				Log.e(TAG, "onCreate: network access", e);
				return null;
			}
		}
	}
}
