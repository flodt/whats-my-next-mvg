package de.schmidt.util.network;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import de.schmidt.mvg.Requests;
import de.schmidt.mvg.route.RouteConnection;
import de.schmidt.mvg.route.RouteOptions;
import de.schmidt.mvg.traffic.Departure;
import de.schmidt.mvg.traffic.Station;
import de.schmidt.whatsnext.R;
import de.schmidt.whatsnext.activities.RoutingItineraryDisplayActivity;
import org.json.JSONException;

import java.lang.ref.WeakReference;
import java.util.Objects;

public class DepartureDetailNetworkAccess extends AsyncTask<Void, Void, RouteConnection> {
	private static final String TAG = "DepartureDetailNetworkAccess";
	private final WeakReference<Activity> context;
	private final ProgressDialog openDialog;
	private final Departure clickedDeparture;

	public DepartureDetailNetworkAccess(Activity context, ProgressDialog openDialog, Departure clickedDeparture) {
		this.context = new WeakReference<>(context);
		this.openDialog = openDialog;
		this.clickedDeparture = clickedDeparture;
	}

	@Override
	protected RouteConnection doInBackground(Void... voids) {
		try {
			Requests requests = Requests.instance();

			//build options and request route for the tapped departure
			Station start = clickedDeparture.getStation();
			Station destination = requests.getStationByName(clickedDeparture.getDirection());
			RouteOptions opt = RouteOptions.getBase()
					.withStart(start)
					.withDestination(destination);

			return requests.getRoute(opt)
					.stream()
					.filter(conn -> conn.getConnectionParts()
							.stream()
							.allMatch(rcp -> Objects.equals(rcp.getDepartureId(), clickedDeparture.getDepartureId())))
					.findFirst()
					.orElseThrow(RuntimeException::new);
		} catch (JSONException e) {
			Log.e(TAG, "onItemClick: json error in getting departure details", e);
			context.get().runOnUiThread(() -> Toast.makeText(context.get(), context.get().getResources().getString(R.string.no_dept_details_avail), Toast.LENGTH_SHORT).show());
		} catch (RuntimeException e) {
			Log.e(TAG, "onItemClick: departure cannot be found", e);
			context.get().runOnUiThread(() -> Toast.makeText(context.get(), context.get().getResources().getString(R.string.no_dept_details_avail), Toast.LENGTH_SHORT).show());
		} finally {
			openDialog.dismiss();
		}

		return null;
	}

	@Override
	protected void onPostExecute(RouteConnection connection) {
		super.onPostExecute(connection);

		//null-check
		if (connection == null) return;

		//start activity with the route connection we got
		Intent intent = new Intent(context.get(), RoutingItineraryDisplayActivity.class);
		intent.putExtra(context.get().getResources().getString(R.string.key_itinerary), connection);
		intent.putExtra(context.get().getResources().getString(R.string.key_back_button_action_bar), false);
		intent.putExtra(context.get().getResources().getString(R.string.key_display_expanded), true);
		context.get().startActivity(intent);
	}
}
