package de.schmidt.whatsnext.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.ActionBar;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import de.schmidt.mvg.Requests;
import de.schmidt.mvg.route.RouteOptions;
import de.schmidt.mvg.route.RouteStationSelection;
import de.schmidt.mvg.traffic.Station;
import de.schmidt.util.ColorUtils;
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
					.setIcon(R.drawable.ic_warning)
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

		fromInput.setThreshold(1);
		toInput.setThreshold(1);

		fromInput.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (s.length() == 0) return;

				new Thread(() -> {
					String[] suggestions = Requests.instance().getAutocompleteSuggestionsForInput(s.toString(), 5);

					runOnUiThread(() -> fromInput.setAdapter(new ArrayAdapter<>(
							RoutingEntryActivity.this,
							android.R.layout.select_dialog_item,
							suggestions
					)));
				}).start();
			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});

		toInput.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (s.length() == 0) return;

				new Thread(() -> {
					String[] suggestions = Requests.instance().getAutocompleteSuggestionsForInput(s.toString(), 5);

					runOnUiThread(() -> toInput.setAdapter(new ArrayAdapter<>(
							RoutingEntryActivity.this,
							android.R.layout.select_dialog_item,
							suggestions
					)));
				}).start();
			}

			@Override
			public void afterTextChanged(Editable s) {

			}
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
					.setIcon(R.drawable.ic_warning)
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
		int[] primaryAndDark = ColorUtils.extractPrimaryAndDark(getColor(R.color.mvg_1));
		actionBar.setBackgroundDrawable(new ColorDrawable(primaryAndDark[0]));
		getWindow().setStatusBarColor(primaryAndDark[1]);

		navBar.setBackgroundColor(getColor(R.color.white));
		navBar.setItemIconTintList(ColorStateList.valueOf(getColor(R.color.mvg_1)));
		navBar.setItemTextColor(ColorStateList.valueOf(getColor(R.color.mvg_1)));

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
