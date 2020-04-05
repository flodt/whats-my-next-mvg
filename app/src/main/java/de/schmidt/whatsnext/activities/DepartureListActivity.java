package de.schmidt.whatsnext.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.TestLooperManager;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.DrawableRes;
import androidx.appcompat.app.ActionBar;
import android.os.Bundle;
import androidx.core.content.pm.ShortcutInfoCompat;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.core.graphics.drawable.IconCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import de.schmidt.mvg.traffic.Departure;
import de.schmidt.mvg.traffic.LineColor;
import de.schmidt.util.*;
import de.schmidt.util.managers.*;
import de.schmidt.util.caching.DepartureCache;
import de.schmidt.util.network.DepartureDetailNetworkAccess;
import de.schmidt.util.network.ListableNetworkAccess;
import de.schmidt.whatsnext.adapters.DepartureListViewAdapter;
import de.schmidt.whatsnext.R;
import de.schmidt.whatsnext.base.ActionBarBaseActivity;
import de.schmidt.whatsnext.base.Shortcutable;
import de.schmidt.whatsnext.base.Updatable;
import de.schmidt.whatsnext.viewsupport.list.SwitchStationListItem;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static de.schmidt.util.ColorUtils.modifyColor;

public class DepartureListActivity extends ActionBarBaseActivity implements Updatable<Departure>, Shortcutable {
	private static final String TAG = "DepartureList";
	private SwipeRefreshLayout swipeRefresh;
	private ListView listView;

	private List<Departure> departures;
	private DepartureListViewAdapter adapter;
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

		fab = findViewById(R.id.fab_list_switch_station);
		FabManager.getInstance().initializeForStationSelection(fab, this);

		listView = findViewById(R.id.departure_list);
		adapter = new DepartureListViewAdapter(this, departures);
		listView.setAdapter(adapter);
		listView.setClickable(true);
		listView.setOnItemClickListener((parent, view, position, id) -> {
			ProgressDialog dialog = new ProgressDialog(DepartureListActivity.this);
			dialog.setMessage(getResources().getString(R.string.loading_progress_dialog));
			dialog.setCancelable(false);
			dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dialog.show();

			//get the clicked departure
			Departure clicked = departures.get(position);

			//start network access and pass to other activity
			new DepartureDetailNetworkAccess(DepartureListActivity.this, dialog, clicked).execute();
		});
	}

	@Override
	public void refresh() {
		swipeRefresh.setRefreshing(true);

		//get the selected station either from the calling intent (for shortcut), or from SharedPreferences
		SwitchStationListItem selected;
		String fromIntent = getIntent().getStringExtra(getString(R.string.key_station_from_shortcut));

		if (fromIntent != null) {
			//if the intent has the station, get it from there
			selected = SwitchStationListItem.deserialize(fromIntent);
		} else {
			//else get it from SharedPreferences
			selected = PreferenceManager.getInstance().getSelectedStation(this);
		}

		//update selection in SharedPreferences if we have the station
		if (PreferenceManager.getInstance().getStationList(this).contains(selected)) {
			PreferenceManager.getInstance().setSelectedStation(this, selected);
		}

		new ListableNetworkAccess(
				this,
				selected,
				PreferenceManager.getInstance().getExcludableTransportMeans(this)
		).execute();
	}

	@Override
	public void handleUIUpdate(List<Departure> dataSet) {
		if (dataSet == null) return;

		runOnUiThread(() -> {
			//copy data to field
			this.departures.clear();
			this.departures.addAll(dataSet);

			//set the default color values
			ThemeManager.getInstance().initializeActionBar(this, actionBar, getWindow());
			listView.setBackgroundColor(getColor(R.color.background));

			//set colors according to result
			if (departures.isEmpty()) {
				setTitle(R.string.app_name);
			} else {
				//adapt status bar to first departure
				setTitle(departures.get(0).getStation().getName());

				LineColor topDeparture = LineColor.ofAPIValue(departures.get(0).getLineBackgroundColor(),
															  departures.get(0).getLine());

				//set action and status bar colors based on enabled theme
				if (ThemeManager.getInstance().isInLightMode(this)) {
					actionBar.setBackgroundDrawable(new ColorDrawable(
							modifyColor(Color.parseColor(topDeparture.getSecondary()), 1.00f)
					));
					getWindow().setStatusBarColor(
							modifyColor(Color.parseColor(topDeparture.getSecondary()), 0.80f)
					);
				}

				//set navbar color
				ThemeManager.getInstance().initializeNavBarWithAccentRaw(this, navBar, Color.parseColor(topDeparture.getSecondary()));
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

	@Override
	public BottomNavigationView getNavBar() {
		return navBar;
	}

	@Override
	public void updateFromCache() {
		handleUIUpdate(DepartureCache.getInstance().getCache());
	}

	@Override
	public void createShortcut() {
		//build the intent that's called on tap
		SwitchStationListItem selected = PreferenceManager.getInstance().getSelectedStation(this);
		Intent launchIntent = new Intent(getApplicationContext(), DepartureListActivity.class);
		launchIntent.putExtra(getString(R.string.key_station_from_shortcut), selected.serialize());
		launchIntent.setAction(Intent.ACTION_MAIN);

		final String label = selected.getTitle(this);
		final @DrawableRes int icon = R.mipmap.ic_departure_list_shortcut_round;

		//request shortcut in launcher
		Shortcutable.requestShortcut(this, launchIntent, label, icon);
	}
}
