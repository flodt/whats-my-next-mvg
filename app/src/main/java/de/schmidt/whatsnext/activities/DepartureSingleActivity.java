package de.schmidt.whatsnext.activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.DrawableRes;
import androidx.appcompat.app.ActionBar;
import android.os.Bundle;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import de.schmidt.mvg.traffic.Departure;
import de.schmidt.mvg.traffic.LineColor;
import de.schmidt.util.*;
import de.schmidt.util.managers.*;
import de.schmidt.util.caching.DepartureCache;
import de.schmidt.util.network.DepartureDetailNetworkAccess;
import de.schmidt.util.network.SingleNetworkAccess;
import de.schmidt.whatsnext.R;
import de.schmidt.whatsnext.base.ActionBarBaseActivity;
import de.schmidt.whatsnext.base.Shortcutable;
import de.schmidt.whatsnext.base.Updatable;
import de.schmidt.whatsnext.viewsupport.list.SwitchStationListItem;

import java.util.List;

import static de.schmidt.util.ColorUtils.modifyColor;

public class DepartureSingleActivity extends ActionBarBaseActivity implements Updatable<Departure>, View.OnClickListener, Shortcutable {
	private static final String TAG = "SingleDepartureActivity";
	private TextView line, direction, inMinutes, minutesFixedLabel;
	private ConstraintLayout layoutBackground;
	private ActionBar actionBar;
	private SwipeRefreshLayout pullToRefresh;

	private BottomNavigationView navBar;
	private FloatingActionButton fab;
	private Departure displayed;

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

		fab = findViewById(R.id.fab_single_switch_station);
		FabManager.getInstance().initializeForStationSelection(fab, this);

		pullToRefresh = findViewById(R.id.pull_to_refresh);
		pullToRefresh.setColorSchemeColors(ColorUtils.getSpriteColors(this));
		pullToRefresh.setOnRefreshListener(() -> {
			refresh();
			pullToRefresh.setRefreshing(false);
		});

		//allow tapping of all these components to go to detail view
		inMinutes.setOnClickListener(this);
		direction.setOnClickListener(this);
		line.setOnClickListener(this);
		minutesFixedLabel.setOnClickListener(this);
	}

	@Override
	public void refresh() {
		pullToRefresh.setRefreshing(true);

		displayed = null;

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

		new SingleNetworkAccess(
				this,
				selected,
				PreferenceManager.getInstance().getExcludableTransportMeans(this)
		).execute();
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
				direction.setTextColor(getColor(R.color.textColorHalf));
				layoutBackground.setBackgroundColor(getColor(R.color.background));
				ThemeUtils.getInstance().initializeActionBar(this, actionBar, getWindow());
				ThemeUtils.getInstance().initializeNavBarWithAccentResource(this, navBar, R.color.mvg_1);

				pullToRefresh.setRefreshing(false);
			});
		} else {
			runOnUiThread(() -> {
				Departure dept = dataSet.get(0);
				this.displayed = dept;

				setTitle(dept.getStation().getName());
				inMinutes.setText("" + dept.getDeltaInMinutes());
				direction.setText(dept.getDirection());
				line.setText(dept.getLine());
				minutesFixedLabel.setText(R.string.minutes);

				//manage colors
				//U7 and 8 have two colors in the line bullet - handle this here
				LineColor color = LineColor.ofAPIValue(dept.getLineBackgroundColor(),
													   dept.getLine());

				//set colors depending on dark mode
				ThemeUtils.getInstance().initializeActionBarWithColorRaw(this, actionBar, getWindow(), Color.parseColor(color.getSecondary()));
				if (ThemeUtils.getInstance().isInLightMode(this)) {
					//in light mode, set according to the departure
					layoutBackground.setBackground(new ColorDrawable(
							modifyColor(Color.parseColor(color.getPrimary()), 1.20f)
					));
					navBar.setBackgroundColor(
							modifyColor(Color.parseColor(color.getPrimary()), 1.20f)
					);
					navBar.setItemTextColor(ColorStateList.valueOf(getColor(R.color.white)));
					navBar.setItemIconTintList(ColorStateList.valueOf(getColor(R.color.white)));
					inMinutes.setTextColor(Color.parseColor(color.getTextColor()));
					direction.setTextColor(Color.parseColor(color.getTextColor()));
					line.setTextColor(Color.parseColor(color.getTextColor()));
					minutesFixedLabel.setTextColor(Color.parseColor(color.getTextColor()));
				} else {
					//in dark mode, set background to dark, rest according to departure
					layoutBackground.setBackground(new ColorDrawable(getColor(R.color.background)));
					navBar.setBackgroundColor(getColor(R.color.navBarBackground));
					navBar.setItemTextColor(ColorStateList.valueOf(getColor(R.color.mvg_1)));
					navBar.setItemIconTintList(ColorStateList.valueOf(getColor(R.color.mvg_1)));
					inMinutes.setTextColor(Color.parseColor(color.getSecondary()));
					direction.setTextColor(Color.parseColor(color.getSecondary()));
					line.setTextColor(Color.parseColor(color.getSecondary()));
					minutesFixedLabel.setTextColor(Color.parseColor(color.getSecondary()));
				}

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

	@Override
	public void onClick(View v) {
		if (displayed != null) {
			ProgressDialog dialog = new ProgressDialog(DepartureSingleActivity.this);
			dialog.setMessage(getResources().getString(R.string.loading_progress_dialog));
			dialog.setCancelable(false);
			dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dialog.show();
			new DepartureDetailNetworkAccess(DepartureSingleActivity.this, dialog, displayed).execute();
		}
	}

	@Override
	public void createShortcut() {
		//build the intent that's called on tap
		SwitchStationListItem selected = PreferenceManager.getInstance().getSelectedStation(this);
		Intent launchIntent = new Intent(getApplicationContext(), DepartureSingleActivity.class);
		launchIntent.putExtra(getString(R.string.key_station_from_shortcut), selected.serialize());
		launchIntent.setAction(Intent.ACTION_MAIN);

		final String label = selected.getTitle(this);
		final @DrawableRes int icon = R.mipmap.ic_departure_single_shortcut_round;

		//request shortcut in launcher
		Shortcutable.requestShortcut(this, launchIntent, label, icon);
	}
}
