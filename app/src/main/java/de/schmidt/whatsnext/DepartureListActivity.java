package de.schmidt.whatsnext;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import de.schmidt.mvg.Departure;
import de.schmidt.mvg.LineColor;
import de.schmidt.util.*;
import de.schmidt.whatsnext.R;

import java.util.ArrayList;
import java.util.List;

import static de.schmidt.util.Utils.modifyColor;

public class DepartureListActivity extends AppCompatActivity {
	private static final String TAG = "DepartureList";
	private SwipeRefreshLayout swipeRefresh;
	private ListView listView;

	private List<Departure> departures;
	private DepartureViewAdapter adapter;
	private String customName;
	private ActionBar actionBar;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuManager.getInstance().inflate(menu, this);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		MenuManager.getInstance().dispatch(item, this);
		return super.onOptionsItemSelected(item);
	}

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
		swipeRefresh.setColorSchemeColors(Utils.getSpriteColors(this));
		swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				refresh();
				swipeRefresh.setRefreshing(false);
			}
		});

		departures = new ArrayList<>();

		customName = getSharedPreferences(Utils.PREFERENCE_KEY, Context.MODE_PRIVATE).getString(getResources().getString(R.string.selection_custom_station_entry),
																								getResources().getString(R.string.default_custom_station_name));

		listView = findViewById(R.id.departure_list);
		adapter = new DepartureViewAdapter(this, departures);
		listView.setAdapter(adapter);
		listView.setClickable(false); // TODO: 04.03.20 open destination in map with long press?
	}

	@Override
	protected void onResume() {
		super.onResume();
		refresh();
	}

	public void refresh() {
		swipeRefresh.setRefreshing(true);

		//get station menu index from preferences, default to Hbf
		SharedPreferences prefs = getSharedPreferences(Utils.PREFERENCE_KEY, Context.MODE_PRIVATE);
		int stationIndex = prefs.getInt(getResources().getString(R.string.selection_station_in_menu), 1);

		new ListableNetworkAccess(
				this, stationIndex, customName, PreferenceManager.getInstance().getExcludableTransportMeans(this)
		).execute(
				LocationManager.getInstance().getLocation(this)
		);
	}

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
}
