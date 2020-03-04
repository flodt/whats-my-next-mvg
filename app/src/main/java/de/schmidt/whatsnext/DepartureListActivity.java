package de.schmidt.whatsnext;

import android.Manifest;
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
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import de.schmidt.mvg.Departure;
import de.schmidt.mvg.LineColor;
import de.schmidt.util.ListableNetworkAccess;
import de.schmidt.util.Utils;
import de.schmidt.whatsnext.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
			case R.id.switch_list_button:
				switchActivity();
				break;
		}

		return super.onOptionsItemSelected(item);
	}

	private void switchActivity() {
		//todo switch to the single view here
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_departure_list);

		actionBar = getSupportActionBar();

		//refresh view
		swipeRefresh = findViewById(R.id.pull_to_refresh_list);
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
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				//item clicked - open on map?
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
		SharedPreferences prefs = getSharedPreferences(Utils.PREFERENCE_KEY, Context.MODE_PRIVATE);
		int stationIndex = prefs.getInt(getResources().getString(R.string.selection_station_in_menu), 1);

		new ListableNetworkAccess(this, dialog, stationIndex, customName, getExcludableTransportMeans()).execute(getLocation());
	}

	public void handleUIUpdate(Departure[] array) {
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
		});


	}




	//refactor this! todo duplicates
	private void updateExclusions() {
		//0: bus, 1: u, 2: s, 3: tram, 4: bahn
		String[] keys = getResources().getStringArray(R.array.transport_keys);
		String[] readable = getResources().getStringArray(R.array.transport_means_readable);
		boolean[] selected = new boolean[keys.length];

		//read preferences
		SharedPreferences prefs = getSharedPreferences(Utils.PREFERENCE_KEY, Context.MODE_PRIVATE);
		for (int i = 0; i < selected.length; i++) {
			selected[i] = prefs.getBoolean(keys[i], true);
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Select means of transport…");
		builder.setIcon(R.drawable.ic_excluded_black);
		builder.setMultiChoiceItems(readable, selected, new DialogInterface.OnMultiChoiceClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which, boolean isChecked) {
				selected[which] = isChecked;
			}
		});
		builder.setCancelable(true);
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
		SharedPreferences prefs = getSharedPreferences(Utils.PREFERENCE_KEY, Context.MODE_PRIVATE);
		Set<String> exclusions = new HashSet<>();

		String[] keys = getResources().getStringArray(R.array.transport_keys);
		for (String key : keys) {
			boolean included = prefs.getBoolean(key, true);
			if (!included) exclusions.add(key);
		}

		return exclusions;
	}

	private void updateStationSelection() {
		SharedPreferences prefs = getSharedPreferences(Utils.PREFERENCE_KEY, Context.MODE_PRIVATE);

		String[] keys = getResources().getStringArray(R.array.station_keys);
		String[] readable = getResources().getStringArray(R.array.station_readable);
		int checked = prefs.getInt(getResources().getString(R.string.selection_station_in_menu), 1);

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.select_station_title);
		builder.setIcon(R.drawable.ic_station_selection_black);
		builder.setSingleChoiceItems(readable, checked, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				prefs.edit().putInt(getResources().getString(R.string.selection_station_in_menu), which).apply();

				//handle custom name here
				if (keys[which].equals("BY_NAME")) {
					getUserInputForCustomStationName(dialog);
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

	private void getUserInputForCustomStationName(DialogInterface parent) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getResources().getString(R.string.custom_station_name_title));

		EditText input = new EditText(this);
		input.setInputType(InputType.TYPE_CLASS_TEXT);
		builder.setView(input);

		builder.setPositiveButton(R.string.save_settings, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				DepartureListActivity.this.customName = input.getText().toString().trim();
				getSharedPreferences(Utils.PREFERENCE_KEY, Context.MODE_PRIVATE)
						.edit()
						.putString(
								getResources().getString(R.string.selection_custom_station_entry),
								customName
						)
						.apply();

				parent.cancel();
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
