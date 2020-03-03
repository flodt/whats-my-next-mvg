package de.schmidt.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;
import de.schmidt.mvg.Departure;
import de.schmidt.mvg.Request;
import de.schmidt.mvg.Station;
import de.schmidt.whatsnext.MainActivity;

import java.lang.ref.WeakReference;

public class NetworkAccess extends AsyncTask<Location, Void, Departure> {
	private static final String TAG = "NetworkAccessLog";
	private final WeakReference<MainActivity> act;
	private ProgressDialog dialog;

	public NetworkAccess(Context context, ProgressDialog dialog) {
		this.act = new WeakReference<>((MainActivity) context);
		this.dialog = dialog;
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

		Request request = Request.instance();
		try {
			Station nearest = request.getNearestStation(loc);
			return request.getNextDeparturesAtStation(nearest)[0];
		} catch (Exception e) {
			Log.e(TAG, "onCreate: network access", e);
			return null;
		}
	}
}
