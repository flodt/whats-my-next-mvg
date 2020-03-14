package de.schmidt.whatsnext.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.ActionBar;
import android.os.Bundle;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import de.schmidt.mvg.Requests;
import de.schmidt.mvg.route.RouteConnection;
import de.schmidt.mvg.route.RouteConnectionPart;
import de.schmidt.mvg.route.RouteIntermediateStop;
import de.schmidt.mvg.route.RouteOptions;
import de.schmidt.mvg.traffic.Departure;
import de.schmidt.mvg.traffic.LineColor;
import de.schmidt.mvg.traffic.Station;
import de.schmidt.util.*;
import de.schmidt.util.managers.FabManager;
import de.schmidt.util.managers.LocationManager;
import de.schmidt.util.managers.NavBarManager;
import de.schmidt.util.managers.PreferenceManager;
import de.schmidt.util.caching.DepartureCache;
import de.schmidt.util.network.ListableNetworkAccess;
import de.schmidt.whatsnext.adapters.DepartureListViewAdapter;
import de.schmidt.whatsnext.R;
import de.schmidt.whatsnext.base.ActionBarBaseActivity;
import de.schmidt.whatsnext.base.Updatable;
import de.schmidt.whatsnext.viewsupport.ArrivingView;
import de.schmidt.whatsnext.viewsupport.ConnectionDisplayView;
import de.schmidt.whatsnext.viewsupport.DepartingView;
import de.schmidt.whatsnext.viewsupport.StopView;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static de.schmidt.util.ColorUtils.modifyColor;

public class DepartureListActivity extends ActionBarBaseActivity implements Updatable<Departure> {
	private static final String TAG = "DepartureList";
	private SwipeRefreshLayout swipeRefresh;
	private ListView listView;

	private List<Departure> departures;
	private DepartureListViewAdapter adapter;
	private String customName;
	private ActionBar actionBar;
	private BottomNavigationView navBar;
	private FloatingActionButton fab;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_departure_list);

		navBar = findViewById(R.id.bottom_nav_bar_list);
		NavBarManager.getInstance().initialize(navBar, this);

		actionBar = getSupportActionBar();

		//refresh view
		swipeRefresh = findViewById(R.id.pull_to_refresh_list);
		swipeRefresh.setColorSchemeColors(ColorUtils.getSpriteColors(this));
		swipeRefresh.setOnRefreshListener(() -> {
			refresh();
			swipeRefresh.setRefreshing(false);
		});

		departures = new ArrayList<>();

		customName = getSharedPreferences(PreferenceManager.PREFERENCE_KEY, Context.MODE_PRIVATE).getString(getResources().getString(R.string.selection_custom_station_entry),
																											getResources().getString(R.string.default_custom_station_name));

		fab = findViewById(R.id.fab_list_switch_station);
		FabManager.getInstance().initializeForStationSelection(fab, this);

		listView = findViewById(R.id.departure_list);
		adapter = new DepartureListViewAdapter(this, departures);
		listView.setAdapter(adapter);
		listView.setClickable(true);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				ProgressDialog dialog = new ProgressDialog(DepartureListActivity.this);
				dialog.setMessage(getResources().getString(R.string.loading_progress_dialog));
				dialog.setCancelable(false);
				dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				dialog.show();

				//get the clicked departure
				Departure clicked = departures.get(position);

				//try and get the same departure for viewing it in a different activity
				new Thread(() -> {
					try {
						Requests requests = Requests.instance();

						Station start = clicked.getStation();
						Station destination = requests.getStationByName(clicked.getDirection());
						RouteOptions opt = RouteOptions.getBase()
								.withStart(start)
								.withDestination(destination);

						RouteConnection connection = requests.getRoute(opt)
								.stream()
								.filter(conn -> conn.getConnectionParts()
										.stream()
										.allMatch(rcp -> Objects.equals(rcp.getDepartureId(), clicked.getDepartureId())))
								.findFirst()
								.orElseThrow(RuntimeException::new);

						Intent intent = new Intent(DepartureListActivity.this, RoutingItineraryDisplayActivity.class);
						intent.putExtra(getString(R.string.key_itinerary), connection);
						intent.putExtra(getString(R.string.key_back_button_action_bar), false);
						intent.putExtra(getString(R.string.key_display_expanded), true);
						startActivity(intent);
					} catch (JSONException e) {
						Log.e(TAG, "onItemClick: json error in getting departure details", e);
						runOnUiThread(() -> Toast.makeText(DepartureListActivity.this, getResources().getString(R.string.no_dept_details_avail), Toast.LENGTH_SHORT).show());
					} catch (RuntimeException e) {
						Log.e(TAG, "onItemClick: departure cannot be found", e);
						runOnUiThread(() -> Toast.makeText(DepartureListActivity.this, getResources().getString(R.string.no_dept_details_avail), Toast.LENGTH_SHORT).show());
					} finally {
						dialog.dismiss();
					}
				}).start();
			}
		});
	}

	@Override
	public void refresh() {
		swipeRefresh.setRefreshing(true);

		//get station menu index from preferences, default to Hbf
		SharedPreferences prefs = getSharedPreferences(PreferenceManager.PREFERENCE_KEY, Context.MODE_PRIVATE);
		int stationIndex = prefs.getInt(getResources().getString(R.string.selection_station_in_menu), 1);

		new ListableNetworkAccess(
				this, stationIndex, customName, PreferenceManager.getInstance().getExcludableTransportMeans(this)
		).execute(
				LocationManager.getInstance().getLocation(this)
		);
	}

	@Override
	public void setCustomName(String customName) {
		this.customName = customName;
	}

	@Override
	public void handleUIUpdate(List<Departure> dataSet) {
		if (dataSet == null) return;

		runOnUiThread(() -> {
			//copy data to field
			this.departures.clear();
			this.departures.addAll(dataSet);

			//set colors according to result
			if (departures.isEmpty()) {
				setTitle(R.string.app_name);

				listView.setBackgroundColor(getColor(R.color.colorPrimary));
				actionBar.setBackgroundDrawable(new ColorDrawable(getColor(R.color.colorPrimary)));
				getWindow().setStatusBarColor(getColor(R.color.colorPrimaryDark));
				navBar.setItemTextColor(ColorStateList.valueOf(getColor(R.color.colorPrimary)));
				navBar.setItemIconTintList(ColorStateList.valueOf(getColor(R.color.colorPrimary)));
			} else {
				//adapt status bar to first departure
				setTitle(departures.get(0).getStation().getName());
				listView.setBackgroundColor(getColor(R.color.white));
				LineColor topDeparture = LineColor.ofAPIValue(departures.get(0).getLineBackgroundColor(),
															  departures.get(0).getLine());
				actionBar.setBackgroundDrawable(new ColorDrawable(
						modifyColor(Color.parseColor(topDeparture.getSecondary()), 1.00f)
				));
				getWindow().setStatusBarColor(
						modifyColor(Color.parseColor(topDeparture.getSecondary()), 0.80f)
				);
				navBar.setItemTextColor(ColorStateList.valueOf(Color.parseColor(topDeparture.getSecondary())));
				navBar.setItemIconTintList(ColorStateList.valueOf(Color.parseColor(topDeparture.getSecondary())));
			}

			navBar.setBackgroundColor(getColor(R.color.white));

			//refresh the list view
			adapter.notifyDataSetChanged();
			listView.invalidateViews();
			listView.refreshDrawableState();

			swipeRefresh.setRefreshing(false);
		});
	}

	@Override
	public int getNavButtonItemId() {
		return R.id.nav_list_button;
	}

	@Override
	public BottomNavigationView getNavBar() {
		return navBar;
	}

	@Override
	public void updateFromCache() {
		handleUIUpdate(DepartureCache.getInstance().getCache());
	}
}
