package de.schmidt.whatsnext.activities;

import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.*;
import androidx.appcompat.app.ActionBar;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import de.schmidt.mvg.Requests;
import de.schmidt.mvg.route.RouteOptions;
import de.schmidt.mvg.traffic.Station;
import de.schmidt.util.ColorUtils;
import de.schmidt.util.caching.RoutingOptionsCache;
import de.schmidt.util.managers.LocationManager;
import de.schmidt.util.managers.NavBarManager;
import de.schmidt.whatsnext.R;
import de.schmidt.whatsnext.base.ActionBarBaseActivity;
import de.schmidt.whatsnext.fragments.TimePickerFragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

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

		goButton.setOnClickListener(v -> new Thread(() -> {
			//show progress dialog while loading data for route options
			ProgressDialog dialog = new ProgressDialog(this);
			dialog.setMessage("Loading…");
			dialog.setCancelable(false);
			dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			runOnUiThread(dialog::show);

			try {
				if (toInput.getText().toString().length() == 0) {
					Toast.makeText(this, "Destination is empty!", Toast.LENGTH_SHORT).show();
					return;
				}

				//initialize start station by field or by location if the field is empty
				Station start = (fromInput.getText().length() != 0) ?
						Requests.instance().getStationByName(fromInput.getText().toString().trim()) :
						Requests.instance().getNearestStation(LocationManager.getInstance().getLocation(this));

				//initialize finish by field content
				Station finish = Requests.instance().getStationByName(toInput.getText().toString().trim());

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
					Intent intent = new Intent(RoutingEntryActivity.this, RoutingAlternativesActivity.class);

					//pass the options through the intent
					intent.putExtra(getString(R.string.key_parameters), finalOptions);

					//dismiss the progress dialog and start the next activity to take over
					dialog.dismiss();
					startActivity(intent);
				});
			} catch (Exception e) {
				runOnUiThread(() -> Toast.makeText(RoutingEntryActivity.this, "Error! Invalid input", Toast.LENGTH_SHORT).show());
				Log.e(TAG, "onClick: error in json parsing for route", e);
			}
		}).start());

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
}
