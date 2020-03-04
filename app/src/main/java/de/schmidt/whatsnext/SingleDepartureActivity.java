package de.schmidt.whatsnext;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import de.schmidt.mvg.Departure;
import de.schmidt.mvg.LineColor;
import de.schmidt.util.*;

import static de.schmidt.util.Utils.modifyColor;

public class SingleDepartureActivity extends AppCompatActivity {
	private static final String TAG = "SingleDepartureActivity";
	private TextView line, direction, inMinutes, minutesFixedLabel;
	private ConstraintLayout layoutBackground;
	private ActionBar actionBar;
	private SwipeRefreshLayout pullToRefresh;

	private String customName;

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
		Intent switchIntent = new Intent(this, DepartureListActivity.class);
		startActivity(switchIntent);
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		//setup layout
		inMinutes = findViewById(R.id.inMinutes);
		minutesFixedLabel = findViewById(R.id.minutesTextBox);
		direction = findViewById(R.id.direction);
		line = findViewById(R.id.line);
		layoutBackground = findViewById(R.id.background);
		actionBar = getSupportActionBar();

		customName = getSharedPreferences(Utils.PREFERENCE_KEY, Context.MODE_PRIVATE).getString(getResources().getString(R.string.selection_custom_station_entry),
																								getResources().getString(R.string.default_custom_station_name));

		pullToRefresh = findViewById(R.id.pull_to_refresh);
		pullToRefresh.setColorSchemeColors(Utils.getSpriteColors(this));
		pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				refresh();
				pullToRefresh.setRefreshing(false);
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		refresh();
	}

	public void refresh() {
		pullToRefresh.setRefreshing(true);

		//get station menu index from preferences, default to Hbf
		SharedPreferences prefs = getSharedPreferences(Utils.PREFERENCE_KEY, Context.MODE_PRIVATE);
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

	public void setCustomName(String customName) {
		this.customName = customName;
	}

	@SuppressLint("SetTextI18n")
	public void handleUIUpdate(Departure dept, boolean empty) {
		if (empty) {
			runOnUiThread(() -> {
				setTitle(R.string.app_name);
				inMinutes.setText("");
				direction.setText("No departures found");
				line.setText("");
				minutesFixedLabel.setText("");
				layoutBackground.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
				actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimary)));
				getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));

				pullToRefresh.setRefreshing(false);
			});
		} else {
			runOnUiThread(() -> {
				setTitle(dept.getStation().getName());
				inMinutes.setText("" + dept.getDeltaInMinutes());
				direction.setText(dept.getDirection());
				line.setText(dept.getLine());
				minutesFixedLabel.setText(R.string.minutes);

				//manage colors
				//U7 and 8 have two colors in the line bullet - handle this here
				LineColor color = LineColor.ofAPIValue(dept.getLineBackgroundColor());
				layoutBackground.setBackground(new ColorDrawable(
						modifyColor(Color.parseColor(color.getPrimary()), 1.20f)
				));
				actionBar.setBackgroundDrawable(new ColorDrawable(
						modifyColor(Color.parseColor(color.getSecondary()), 1.00f)
				));
				getWindow().setStatusBarColor(
						modifyColor(Color.parseColor(color.getSecondary()), 0.80f)
				);

				pullToRefresh.setRefreshing(false);
			});
		}

	}
}
