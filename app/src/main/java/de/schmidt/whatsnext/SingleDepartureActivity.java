package de.schmidt.whatsnext;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import de.schmidt.mvg.Departure;
import de.schmidt.util.SingleNetworkAccess;

import java.util.HashSet;
import java.util.Set;

import static de.schmidt.util.Utils.modifyColor;

public class SingleDepartureActivity extends AppCompatActivity {
	private static final String TAG = "MainActivityLog";
	private TextView line, direction, inMinutes, minutesFixedLabel;
	private ConstraintLayout layoutBackground;
	private ActionBar actionBar;
	private SwipeRefreshLayout pullToRefresh;

	private String customName = null;


	//ActionBar setup
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.app_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		switch (item.getItemId()) {
			case R.id.refresh_button:
				refresh();
				break;
			case R.id.exclude_button:
				updateExclusions();
				break;
			case R.id.select_station_button:
				updateStationSelection();
				break;
		}

		return super.onOptionsItemSelected(item);
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

		pullToRefresh = findViewById(R.id.pull_to_refresh);
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

	@SuppressWarnings("deprecation")
	private void refresh() {
		//setup progress dialog
		ProgressDialog dialog = new ProgressDialog(this);
		dialog.setIndeterminate(true);
		dialog.setCancelable(false);
		dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		dialog.setMessage("Loading...");
		dialog.show();

		//get station menu index from preferences, default to Hbf
		SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
		int stationIndex = prefs.getInt(getResources().getString(R.string.selection_station), 1);

		new SingleNetworkAccess(this, dialog, stationIndex, customName, getExcludableTransportMeans()).execute(getLocation());
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
			});
		} else {
			runOnUiThread(() -> {
				setTitle(dept.getStation().getName());
				inMinutes.setText("" + dept.getDeltaInMinutes());
				direction.setText(dept.getDirection());
				line.setText(dept.getLine());
				minutesFixedLabel.setText(R.string.minutes);
				layoutBackground.setBackground(new ColorDrawable(
						modifyColor(Color.parseColor(dept.getLineBackgroundColor()), 1.20f)
				));
				actionBar.setBackgroundDrawable(new ColorDrawable(
						modifyColor(Color.parseColor(dept.getLineBackgroundColor()), 1.00f)
				));
				getWindow().setStatusBarColor(
						modifyColor(Color.parseColor(dept.getLineBackgroundColor()), 0.80f)
				);
			});
		}

	}

	private void updateExclusions() {
		//0: bus, 1: u, 2: s, 3: tram, 4: bahn
		String[] keys = getResources().getStringArray(R.array.transport_keys);
		String[] readable = getResources().getStringArray(R.array.transport_means_readable);
		boolean[] selected = new boolean[keys.length];

		//read preferences
		SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
		for (int i = 0; i < selected.length; i++) {
			selected[i] = prefs.getBoolean(keys[i], true);
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Select means of transport…");
		builder.setIcon(R.drawable.ic_excluded);
		builder.setMultiChoiceItems(readable, selected, new DialogInterface.OnMultiChoiceClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which, boolean isChecked) {
				selected[which] = isChecked;
			}
		});
		builder.setCancelable(false);
		builder.setPositiveButton(getResources().getString(R.string.save_settings), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//save to preferences
				SharedPreferences.Editor editor = prefs.edit();
				for (int i = 0; i < keys.length; i++) {
					editor.putBoolean(keys[i], selected[i]);
				}
				editor.apply();
				refresh();
			}
		});
		builder.setNegativeButton(getResources().getString(R.string.dismiss_settings), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				refresh();
			}
		});

		AlertDialog dialog = builder.create();
		dialog.show();
	}

	private Set<String> getExcludableTransportMeans() {
		SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
		Set<String> exclusions = new HashSet<>();

		String[] keys = getResources().getStringArray(R.array.transport_keys);
		for (String key : keys) {
			boolean included = prefs.getBoolean(key, true);
			if (!included) exclusions.add(key);
		}

		return exclusions;
	}

	private void updateStationSelection() {
		SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);

		String[] keys = getResources().getStringArray(R.array.station_keys);
		String[] readable = getResources().getStringArray(R.array.station_readable);
		int checked = prefs.getInt(getResources().getString(R.string.selection_station), 1);

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.select_station_title);
		builder.setIcon(R.drawable.ic_station_selection);
		builder.setSingleChoiceItems(readable, checked, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				prefs.edit().putInt(getResources().getString(R.string.selection_station), which).apply();

				//handle custom name here
				if (keys[which].equals("BY_NAME")) {
					getUserInputForCustomStationName();
					return;
				}

				dialog.cancel();
				refresh();
			}
		});
		builder.setNeutralButton(R.string.dismiss_settings, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				refresh();
			}
		});

		AlertDialog dialog = builder.create();
		dialog.show();
	}

	private void getUserInputForCustomStationName() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getResources().getString(R.string.custom_station_name_title));

		EditText input = new EditText(this);
		input.setInputType(InputType.TYPE_CLASS_TEXT);
		builder.setView(input);

		builder.setPositiveButton(R.string.save_settings, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				SingleDepartureActivity.this.customName = input.getText().toString();
				dialog.cancel();
				refresh();
			}
		});

		builder.setCancelable(false);

		AlertDialog dialog = builder.create();
		dialog.show();
	}

	private Location getLocation() {
		return getCurrentGeoLocation();
	}

	private Location getCurrentGeoLocation() {
		// Get LocationManager object
		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		// Create a criteria object to retrieve provider
		Criteria criteria = new Criteria();

		// Get the name of the best provider
		assert locationManager != null;
		String provider = locationManager.getBestProvider(criteria, true);

		// Get Current Location
		Location location;
		if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			// TODO: Consider calling
			//    Activity#requestPermissions
			// here to request the missing permissions, and then overriding
			//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
			//                                          int[] grantResults)
			// to handle the case where the user grants the permission. See the documentation
			// for Activity#requestPermissions for more details.
			Log.e(TAG, "no location access granted");

			//set the location to dummy coordinates
			location = new Location("dummyprovider");
			location.setLatitude(30.0000);
			location.setLongitude(9.000);
		} else {
			assert provider != null;
			location = locationManager.getLastKnownLocation(provider);
		}
		return location;
	}


}
