package de.schmidt.whatsnext.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.widget.TextView;
import androidx.appcompat.app.ActionBar;
import android.os.Bundle;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import de.schmidt.mvg.traffic.Departure;
import de.schmidt.mvg.traffic.LineColor;
import de.schmidt.util.*;
import de.schmidt.util.managers.FabManager;
import de.schmidt.util.managers.LocationManager;
import de.schmidt.util.managers.NavBarManager;
import de.schmidt.util.managers.PreferenceManager;
import de.schmidt.util.caching.DepartureCache;
import de.schmidt.util.network.SingleNetworkAccess;
import de.schmidt.whatsnext.R;
import de.schmidt.whatsnext.base.ActionBarBaseActivity;
import de.schmidt.whatsnext.base.Updatable;

import java.util.List;

import static de.schmidt.util.ColorUtils.modifyColor;

public class DepartureSingleActivity extends ActionBarBaseActivity implements Updatable<Departure> {
	private static final String TAG = "SingleDepartureActivity";
	private TextView line, direction, inMinutes, minutesFixedLabel;
	private ConstraintLayout layoutBackground;
	private ActionBar actionBar;
	private SwipeRefreshLayout pullToRefresh;

	private String customName;
	private BottomNavigationView navBar;
	private FloatingActionButton fab;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_departure_single);

		//setup layout
		inMinutes = findViewById(R.id.inMinutes);
		minutesFixedLabel = findViewById(R.id.minutesTextBox);
		direction = findViewById(R.id.direction);
		line = findViewById(R.id.line);
		layoutBackground = findViewById(R.id.background);
		actionBar = getSupportActionBar();

		navBar = findViewById(R.id.bottom_nav_bar_single);
		NavBarManager.getInstance().initialize(navBar, this);

		customName = getSharedPreferences(PreferenceManager.PREFERENCE_KEY, Context.MODE_PRIVATE).getString(getResources().getString(R.string.selection_custom_station_entry),
																											getResources().getString(R.string.default_custom_station_name));

		fab = findViewById(R.id.fab_single_switch_station);
		FabManager.getInstance().initializeForStationSelection(fab, this);

		pullToRefresh = findViewById(R.id.pull_to_refresh);
		pullToRefresh.setColorSchemeColors(ColorUtils.getSpriteColors(this));
		pullToRefresh.setOnRefreshListener(() -> {
			refresh();
			pullToRefresh.setRefreshing(false);
		});
	}

	@Override
	public void refresh() {
		pullToRefresh.setRefreshing(true);

		//get station menu index from preferences, default to Hbf
		SharedPreferences prefs = getSharedPreferences(PreferenceManager.PREFERENCE_KEY, Context.MODE_PRIVATE);
		int stationIndex = prefs.getInt(getResources().getString(R.string.selection_station_in_menu), 1);

		new SingleNetworkAccess(
				this,
				stationIndex,
				customName,
				PreferenceManager.getInstance().getExcludableTransportMeans(this)
		).execute(
				LocationManager.getInstance().getLocation(this)
		);
	}

	@Override
	public void setCustomName(String customName) {
		this.customName = customName;
	}

	@Override
	@SuppressLint("SetTextI18n")
	public void handleUIUpdate(List<Departure> dataSet) {
		//refresh UI based on result from network
		if (dataSet.isEmpty()) {
			runOnUiThread(() -> {
				setTitle(R.string.app_name);
				inMinutes.setText("");
				direction.setText(getResources().getString(R.string.no_departures_found));
				line.setText("");
				minutesFixedLabel.setText("");
				direction.setTextColor(getColor(R.color.white));
				layoutBackground.setBackgroundColor(getColor(R.color.colorPrimary));
				actionBar.setBackgroundDrawable(new ColorDrawable(getColor(R.color.colorPrimary)));
				getWindow().setStatusBarColor(getColor(R.color.colorPrimaryDark));
				navBar.setItemTextColor(ColorStateList.valueOf(getColor(R.color.colorPrimary)));

				pullToRefresh.setRefreshing(false);
			});
		} else {
			runOnUiThread(() -> {
				Departure dept = dataSet.get(0);
				setTitle(dept.getStation().getName());
				inMinutes.setText("" + dept.getDeltaInMinutes());
				direction.setText(dept.getDirection());
				line.setText(dept.getLine());
				minutesFixedLabel.setText(R.string.minutes);

				//manage colors
				//U7 and 8 have two colors in the line bullet - handle this here
				LineColor color = LineColor.ofAPIValue(dept.getLineBackgroundColor(),
													   dept.getLine());
				layoutBackground.setBackground(new ColorDrawable(
						modifyColor(Color.parseColor(color.getPrimary()), 1.20f)
				));
				actionBar.setBackgroundDrawable(new ColorDrawable(
						modifyColor(Color.parseColor(color.getSecondary()), 1.00f)
				));
				getWindow().setStatusBarColor(
						modifyColor(Color.parseColor(color.getSecondary()), 0.80f)
				);

				navBar.setBackgroundColor(
						modifyColor(Color.parseColor(color.getPrimary()), 1.20f)
				);
				inMinutes.setTextColor(Color.parseColor(color.getTextColor()));
				direction.setTextColor(Color.parseColor(color.getTextColor()));
				line.setTextColor(Color.parseColor(color.getTextColor()));
				minutesFixedLabel.setTextColor(Color.parseColor(color.getTextColor()));
				navBar.setItemTextColor(ColorStateList.valueOf(getColor(R.color.white)));
				navBar.setItemIconTintList(ColorStateList.valueOf(getColor(R.color.white)));

				pullToRefresh.setRefreshing(false);
			});
		}

	}

	@Override
	public int getNavButtonItemId() {
		return R.id.nav_single_button;
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
