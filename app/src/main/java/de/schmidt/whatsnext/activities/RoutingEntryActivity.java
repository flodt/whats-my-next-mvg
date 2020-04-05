package de.schmidt.whatsnext.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import de.schmidt.mvg.Requests;
import de.schmidt.mvg.route.RouteOptions;
import de.schmidt.util.ThemeUtils;
import de.schmidt.util.network.AutocompleteNetworkAccess;
import de.schmidt.whatsnext.adapters.AutocompleteSuggestAdapter;
import de.schmidt.whatsnext.adapters.OnTextChangedWatcher;
import de.schmidt.whatsnext.viewsupport.list.RouteStationSelection;
import de.schmidt.mvg.traffic.Station;
import de.schmidt.util.caching.RoutingOptionsCache;
import de.schmidt.util.managers.LocationManager;
import de.schmidt.util.managers.NavBarManager;
import de.schmidt.util.managers.PreferenceManager;
import de.schmidt.whatsnext.R;
import de.schmidt.whatsnext.adapters.RecentsListViewAdapter;
import de.schmidt.whatsnext.base.ActionBarBaseActivity;
import de.schmidt.whatsnext.fragments.TimePickerFragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class RoutingEntryActivity extends ActionBarBaseActivity implements TimePickerDialog.OnTimeSetListener {
	private static final String TAG = "RoutingEntry";
	private static final int AUTOCOMPLETE_FROM = 1000;
	private static final int AUTOCOMPLETE_TO = 1001;

	private BottomNavigationView navBar;
	private ActionBar actionBar;
	private AutoCompleteTextView fromInput;
	private AutoCompleteTextView toInput;
	private Date selectedTime;
	private RadioGroup radio;
	private Button selectTimeButton;
	private Button goButton;
	private TextView selectedTimeLabel;
	private Button resetButton;
	private Button flipButton;
	private ListView recentsList;
	private List<RouteStationSelection> recents;
	private RecentsListViewAdapter adapter;
	private AutocompleteSuggestAdapter fromAdapter;
	private AutocompleteSuggestAdapter toAdapter;
	private Handler handler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_routing_entry);

		setTitle(getString(R.string.navbar_route));
		actionBar = getSupportActionBar();

		navBar = findViewById(R.id.bottom_nav_bar_routing_entry);
		NavBarManager.getInstance().initialize(navBar, this);

		fromInput = findViewById(R.id.from_station_input);
		toInput = findViewById(R.id.to_station_input);
		radio = findViewById(R.id.dept_arr_radio_group);
		selectTimeButton = findViewById(R.id.select_time_button);
		selectedTimeLabel = findViewById(R.id.selected_time_label);
		goButton = findViewById(R.id.go_button);
		resetButton = findViewById(R.id.reset_button);
		flipButton = findViewById(R.id.flip_button);

		recentsList = findViewById(R.id.entry_recents_list);
		recents = PreferenceManager.getInstance().getRecents(this);
		adapter = new RecentsListViewAdapter(this, recents);
		recentsList.setAdapter(adapter);
		recentsList.setClickable(true);
		recentsList.setOnItemClickListener((parent, view, position, id) -> {
			//add input to the EditTexts
			RouteStationSelection selection = recents.get(position);
			runOnUiThread(() -> {
				fromInput.setText(selection.getStart().getName());
				toInput.setText(selection.getDestination().getName());
			});
		});
		recentsList.setLongClickable(true);
		recentsList.setOnItemLongClickListener((parent, view, position, id) -> {
			//ask for confirmation
			new AlertDialog.Builder(RoutingEntryActivity.this)
					.setTitle(getResources().getString(R.string.remove_recent_title))
					.setMessage(getResources().getString(R.string.remove_recent_message))
					.setIcon(R.drawable.ic_dark_delete)
					.setNegativeButton(getResources().getString(R.string.cancel_dialog), null)
					.setPositiveButton(getResources().getString(R.string.yes_dialog), (dialog, which) -> {
						//remove the element from the list
						PreferenceManager.getInstance()
								.removeFromRecents(RoutingEntryActivity.this, recents.get(position));
						refreshRecentsList();
						dialog.dismiss();
					})
					.create()
					.show();

			return true;
		});

		//setup handler for UI updates
		handler = new Handler(new Handler.Callback() {
			@Override
			public boolean handleMessage(@NonNull Message msg) {
				//if we want to autocomplete the from field, do that if it's not empty
				if (msg.what == AUTOCOMPLETE_FROM) {
					if (!TextUtils.isEmpty(fromInput.getText())) {
						new AutocompleteNetworkAccess(fromInput.getText().toString(), fromAdapter).execute();
					}
				} else if (msg.what == AUTOCOMPLETE_TO) {
					if (!TextUtils.isEmpty(toInput.getText())) {
						new AutocompleteNetworkAccess(toInput.getText().toString(), toAdapter).execute();
					}
				}

				return false;
			}
		});

		//set threshold for the start of autocompletion
		fromInput.setThreshold(1);
		toInput.setThreshold(1);

		//create adapters
		fromAdapter = new AutocompleteSuggestAdapter(this, android.R.layout.select_dialog_item);
		fromInput.setAdapter(fromAdapter);
		toAdapter = new AutocompleteSuggestAdapter(this, android.R.layout.select_dialog_item);
		toInput.setAdapter(toAdapter);

		//setup text changed listeners for handling of the autocompletion
		fromInput.addTextChangedListener((OnTextChangedWatcher) (s, start, before, count) -> {
			handler.removeMessages(AUTOCOMPLETE_FROM);
			handler.sendEmptyMessage(AUTOCOMPLETE_FROM);
		});
		toInput.addTextChangedListener((OnTextChangedWatcher) (s, start, before, count) -> {
			handler.removeMessages(AUTOCOMPLETE_TO);
			handler.sendEmptyMessage(AUTOCOMPLETE_TO);
		});

		selectTimeButton.setOnClickListener(v -> {
			DialogFragment timePicker = new TimePickerFragment();
			timePicker.show(getSupportFragmentManager(), "TimePicker");
		});

		resetButton.setOnClickListener(v -> {
			fromInput.setText("");
			toInput.setText("");

			selectedTime = null;
			selectedTimeLabel.setText(R.string.now);
			radio.check(R.id.radio_dept);
		});

		resetButton.setLongClickable(true);
		resetButton.setOnLongClickListener(v -> {
			new AlertDialog.Builder(RoutingEntryActivity.this)
					.setTitle(getResources().getString(R.string.clear_recents_title))
					.setMessage(getResources().getString(R.string.clear_recents_message))
					.setIcon(R.drawable.ic_dark_delete)
					.setNegativeButton(getResources().getString(R.string.cancel_dialog), null)
					.setPositiveButton(getResources().getString(R.string.yes_dialog), (dialog, which) -> {
						PreferenceManager.getInstance().clearRecents(RoutingEntryActivity.this);
						refreshRecentsList();
						dialog.dismiss();
					})
					.create()
					.show();

			return true;
		});

		goButton.setOnClickListener(v -> {
			//show progress dialog while loading data for route options
			ProgressDialog dialog = new ProgressDialog(this);
			dialog.setMessage(getResources().getString(R.string.loading_progress_dialog));
			dialog.setCancelable(false);
			dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dialog.show();

			new Thread(() -> {
				try {
					if (toInput.getText().toString().length() == 0) {
						dialog.dismiss();
						runOnUiThread(() -> Toast.makeText(this, getResources().getString(R.string.toast_nodestination), Toast.LENGTH_SHORT).show());
						return;
					}

					//initialize start station by field or by location if the field is empty
					Station start = (fromInput.getText().length() != 0) ?
							Requests.instance().getStationByName(fromInput.getText().toString().trim()) :
							Requests.instance().getNearestStation(LocationManager.getInstance().getLocation(this));

					//initialize finish by field content
					Station finish = Requests.instance().getStationByName(toInput.getText().toString().trim());

					//set the text field accordingly
					runOnUiThread(() -> {
						fromInput.setText(start.getName());
						toInput.setText(finish.getName());
					});

					//generate route options
					RouteOptions options = RouteOptions.getBase()
							.withStart(start)
							.withDestination(finish);

					if (selectedTime != null) {
						options = options.withTime(
								selectedTime, radio.getCheckedRadioButtonId() == R.id.radio_dept
						);
					}

					RouteOptions finalOptions = options;
					Log.d(TAG, "onClick: request with " + finalOptions);

					//switch activity to AlternativesView which will do the network access based on the options
					runOnUiThread(() -> {
						//add entry to preferences
						PreferenceManager.getInstance().addToRecents(
								RoutingEntryActivity.this, RouteStationSelection.fromRoute(start, finish));
						refreshRecentsList();

						Intent intent = new Intent(RoutingEntryActivity.this, RoutingAlternativesActivity.class);

						//pass the options through the intent
						intent.putExtra(getString(R.string.key_parameters), finalOptions);

						//dismiss the progress dialog and start the next activity to take over
						dialog.dismiss();
						startActivity(intent);
					});
				} catch (Exception e) {
					dialog.dismiss();
					runOnUiThread(() -> Toast.makeText(RoutingEntryActivity.this, getResources().getString(R.string.toast_invalid_input), Toast.LENGTH_SHORT).show());
					Log.e(TAG, "onClick: error in json parsing for route", e);
				}
			}).start();
		});

		flipButton.setOnClickListener(v -> {
			String first = fromInput.getText().toString();
			String second = toInput.getText().toString();
			fromInput.setText(second);
			toInput.setText(first);
		});
	}

	@Override
	protected void onResume() {
		//set colors
		ThemeUtils.getInstance().initializeActionBar(this, actionBar, getWindow());
		ThemeUtils.getInstance().initializeNavBarWithAccentResource(this, navBar, R.color.mvg_1);

		//clear the route options cache, as we are in the process of entering a new input
		RoutingOptionsCache.getInstance().clearCache();

		refreshRecentsList();

		super.onResume();
	}

	@Override
	public void refresh() {

	}

	@Override
	public int getNavButtonItemId() {
		return R.id.nav_route_button;
	}

	@Override
	public BottomNavigationView getNavBar() {
		return navBar;
	}

	@Override
	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
		Calendar cal = Calendar.getInstance();
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH);
		int day = cal.get(Calendar.DAY_OF_MONTH);

		//noinspection deprecation
		selectedTime = new Date(year - 1900, month, day, hourOfDay, minute);
		selectedTimeLabel.setText(new SimpleDateFormat("HH:mm").format(selectedTime));
	}

	public void refreshRecentsList() {
		//refresh contents of recents object
		recents.clear();
		recents.addAll(PreferenceManager.getInstance().getRecents(this));

		//refresh listview
		adapter.notifyDataSetChanged();
		recentsList.invalidateViews();
		recentsList.refreshDrawableState();
	}
}
