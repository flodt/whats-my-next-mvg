package de.schmidt.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;
import androidx.annotation.Nullable;
import de.schmidt.mvg.Departure;
import de.schmidt.mvg.Request;
import de.schmidt.mvg.Station;
import de.schmidt.whatsnext.R;
import de.schmidt.whatsnext.SingleDepartureActivity;

import java.lang.ref.WeakReference;
import java.util.Set;

public class SingleNetworkAccess extends AsyncTask<Location, Void, Departure> {
	private static final String TAG = "NetworkAccessLog";
	private final WeakReference<SingleDepartureActivity> act;
	private ProgressDialog dialog;
	private final int stationMenuIndex;
	private final String stationMenuName;
	private final Set<String> exclusions;


	public SingleNetworkAccess(Context context, ProgressDialog dialog, int stationMenuIndex,
							   @Nullable String stationMenuName, Set<String> exclusions) {
		this.act = new WeakReference<>((SingleDepartureActivity) context);
		this.dialog = dialog;
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
		dialog.dismiss();
	}

	@Override
	protected Departure doInBackground(Location... locations) {
		//handle updates
		Location loc = locations[0];

		String[] keys = act.get().getResources().getStringArray(R.array.station_keys);

		if (keys[stationMenuIndex].equals("LOCATION")) {
			Request request = Request.instance();
			try {
				Station nearest = request.getNearestStation(loc);
				return request.getNextDeparturesAtStation(nearest, exclusions)[0];
			} catch (Exception e) {
				Log.e(TAG, "onCreate: network access", e);
				return null;
			}
		} else if (keys[stationMenuIndex].equals("BY_NAME")) {
			Request request = Request.instance();
			try {
				Station byName = request.getStationByName(stationMenuName);
				return request.getNextDeparturesAtStation(byName, exclusions)[0];
			} catch (Exception e) {
				Log.e(TAG, "onCreate: network access", e);
				return null;
			}
		} else {
			Request request = Request.instance();
			try {
				Station byId = request.getStationById(keys[stationMenuIndex]);
				return request.getNextDeparturesAtStation(byId, exclusions)[0];
			} catch (Exception e) {
				Log.e(TAG, "onCreate: network access", e);
				return null;
			}
		}
	}
}
