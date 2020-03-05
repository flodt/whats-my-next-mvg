package de.schmidt.whatsnext.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.widget.ListView;
import androidx.appcompat.app.ActionBar;
import android.os.Bundle;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import de.schmidt.mvg.Departure;
import de.schmidt.mvg.LineColor;
import de.schmidt.util.*;
import de.schmidt.util.network.ListableNetworkAccess;
import de.schmidt.whatsnext.adapters.DepartureListViewAdapter;
import de.schmidt.whatsnext.R;

import java.util.ArrayList;
import java.util.List;

import static de.schmidt.util.ColorUtils.modifyColor;

public class DepartureListActivity extends ActionBarBaseActivity {
	private static final String TAG = "DepartureList";
	private SwipeRefreshLayout swipeRefresh;
	private ListView listView;

	private List<Departure> departures;
	private DepartureListViewAdapter adapter;
	private String customName;
	private ActionBar actionBar;

	@Override
	public void switchActivity() {
		Intent switchIntent = new Intent(this, SingleDepartureActivity.class);
		startActivity(switchIntent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_departure_list);

		actionBar = getSupportActionBar();

		//refresh view
		swipeRefresh = findViewById(R.id.pull_to_refresh_list);
		swipeRefresh.setColorSchemeColors(ColorUtils.getSpriteColors(this));
		swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				refresh();
				swipeRefresh.setRefreshing(false);
			}
		});

		departures = new ArrayList<>();

		customName = getSharedPreferences(PreferenceManager.PREFERENCE_KEY, Context.MODE_PRIVATE).getString(getResources().getString(R.string.selection_custom_station_entry),
																											getResources().getString(R.string.default_custom_station_name));

		listView = findViewById(R.id.departure_list);
		adapter = new DepartureListViewAdapter(this, departures);
		listView.setAdapter(adapter);
		listView.setClickable(false);
	}

	@Override
	protected void onResume() {
		super.onResume();
		refresh();
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

	public void handleUIUpdate(Departure[] array) {
		if (array == null) return;

		runOnUiThread(() -> {
			this.departures.clear();
			for (int i = 0; i < array.length; i++) {
				this.departures.add(i, array[i]);
			}

			if (departures.isEmpty()) {
				setTitle(R.string.app_name);
				listView.setBackgroundColor(getColor(R.color.colorPrimary));
				actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimary)));
				getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
			} else {
				//adapt status bar to first departure
				setTitle(departures.get(0).getStation().getName());
				listView.setBackgroundColor(getColor(R.color.white));
				LineColor topDeparture = LineColor.ofAPIValue(departures.get(0).getLineBackgroundColor());
				actionBar.setBackgroundDrawable(new ColorDrawable(
						modifyColor(Color.parseColor(topDeparture.getSecondary()), 1.00f)
				));
				getWindow().setStatusBarColor(
						modifyColor(Color.parseColor(topDeparture.getSecondary()), 0.80f)
				);
			}

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
}
