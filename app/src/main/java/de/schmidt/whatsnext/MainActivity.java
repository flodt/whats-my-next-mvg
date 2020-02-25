package de.schmidt.whatsnext;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;
import androidx.annotation.ColorInt;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.constraintlayout.widget.ConstraintLayout;
import de.schmidt.mvg.Departure;
import de.schmidt.mvg.Request;
import de.schmidt.mvg.Station;

// TODO: 25.02.20 add a refresh button (by adding a menu)
// TODO: 25.02.20 add a ProgressDialog, move Network access to AsyncTask
public class MainActivity extends AppCompatActivity {
	private static final String TAG = "MainActivityLog";
	private TextView line, direction, inMinutes;
	private ConstraintLayout layoutBackground;
	private ActionBar actionBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		//setup layout
		inMinutes = findViewById(R.id.inMinutes);
		direction = findViewById(R.id.direction);
		line = findViewById(R.id.line);
		layoutBackground = findViewById(R.id.background);
		actionBar = getSupportActionBar();
	}

	@Override
	protected void onResume() {
		super.onResume();
		refresh();
	}

	private void refresh() {
		//handle updates
		Location loc = getLocation();

		Thread network = new Thread(() -> {
			Request request = new Request();
			try {
				Station nearest = request.getNearestStation(loc);
				Departure next = request.getNextDeparturesAtStation(nearest)[0];
				handleUIUpdate(next, false);
			} catch (Exception e) {
				Log.e(TAG, "onCreate: network access", e);
				handleUIUpdate(null, true);
			}
		});
		network.start();
	}

	@SuppressLint("SetTextI18n")
	private void handleUIUpdate(Departure dept, boolean empty) {
		if (empty) {
			runOnUiThread(() -> {
				inMinutes.setText("");
				direction.setText("No departures found");
				line.setText("");
				layoutBackground.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
				actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimary)));
				getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
			});
		} else {
			runOnUiThread(() -> {
				inMinutes.setText("" + dept.getDeltaInMinutes());
				direction.setText(dept.getDirection());
				line.setText(dept.getLine());
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

	@ColorInt
	private int modifyColor(@ColorInt int color, float value) {
		float[] hsv = new float[3];
		Color.colorToHSV(color, hsv);
		hsv[2] *= value;
		return Color.HSVToColor(hsv);
	}

	private Location getLocation() {
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

			//set the location to Munich Hbf
			location = new Location("dummyprovider");
			location.setLatitude(48.14003);
			location.setLongitude(11.56107);
		} else {
			assert provider != null;
			location = locationManager.getLastKnownLocation(provider);
		}
		return location;
	}


}
