package de.schmidt.util.network;

import android.os.AsyncTask;
import android.util.Log;
import de.schmidt.mvg.Requests;
import de.schmidt.mvg.route.RouteConnection;
import de.schmidt.mvg.route.RouteOptions;
import de.schmidt.whatsnext.activities.RoutingAlternativesActivity;
import org.json.JSONException;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;

public class RoutingNetworkAccess extends AsyncTask<Void, Void, List<RouteConnection>> {
	private static final String TAG = "RoutingAlternativesActivity";
	private final WeakReference<RoutingAlternativesActivity> act;
	private final RouteOptions options;


	public RoutingNetworkAccess(RoutingAlternativesActivity context, RouteOptions options) {
		this.act = new WeakReference<>(context);
		this.options = options;
	}

	@Override
	protected List<RouteConnection> doInBackground(Void... voids) {
		//do routing request
		try {
			return Requests.instance().getRoute(options);
		} catch (JSONException e) {
			Log.e(TAG, "doInBackground: json exception in network access", e);
			return Collections.emptyList();
		}
	}

	@Override
	protected void onPostExecute(List<RouteConnection> routeConnections) {
		//trigger UI update
		super.onPostExecute(routeConnections);
		if (act.get() != null) {
			act.get().handleUIUpdate(routeConnections);
		}
	}
}
