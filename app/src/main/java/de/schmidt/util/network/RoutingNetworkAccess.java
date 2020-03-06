package de.schmidt.util.network;

import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;
import androidx.annotation.Nullable;
import de.schmidt.mvg.Requests;
import de.schmidt.mvg.route.RouteConnection;
import de.schmidt.mvg.route.RouteOptions;
import de.schmidt.mvg.traffic.Departure;
import de.schmidt.mvg.traffic.Station;
import de.schmidt.util.caching.DepartureCache;
import de.schmidt.whatsnext.R;
import de.schmidt.whatsnext.activities.DepartureListActivity;
import de.schmidt.whatsnext.activities.RoutingAlternativesActivity;
import org.json.JSONException;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class RoutingNetworkAccess extends AsyncTask<Void, Void, List<RouteConnection>> {
	private static final String TAG = "RoutingAlternativesActivity";
	private final WeakReference<RoutingAlternativesActivity> act;
	private final RouteOptions options;


	public RoutingNetworkAccess(RoutingAlternativesActivity context, RouteOptions options) {
		this.act = new WeakReference<>(context);
		this.options = options;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}

	@Override
	protected List<RouteConnection> doInBackground(Void... voids) {
		try {
			return Requests.instance().getRoute(options);
		} catch (JSONException e) {
			Log.e(TAG, "doInBackground: json exception in network access", e);
			return Collections.emptyList();
		}
	}

	@Override
	protected void onPostExecute(List<RouteConnection> routeConnections) {
		super.onPostExecute(routeConnections);
		act.get().handleUIUpdate(routeConnections);
	}
}
