package de.schmidt.util.managers;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import de.schmidt.whatsnext.R;

public class LocationManager {
	private static final LocationManager instance = new LocationManager();
	private static final String TAG = "LocationManager";
	public static final int LOCATION_PERMISSION_REQUEST_CODE = 9909;

	private LocationManager() {}

	public static LocationManager getInstance() {
		return instance;
	}

	public Location getLocation(Activity context) {
		checkLocationPermission(context);
		return getCurrentGeoLocation(context);
	}

	public boolean checkLocationPermission(Activity context) {
		//if we already asked for permission, don't ask again
		if (PreferenceManager.getInstance().getLocationPermissionAlreadyRequested(context)) return false;

		//check whether we already have the permission
		if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			//show an explanation to the user why this location info is needed
			if (ActivityCompat.shouldShowRequestPermissionRationale(context, Manifest.permission.ACCESS_FINE_LOCATION)) {
				context.runOnUiThread(() -> {
					new AlertDialog.Builder(context)
							.setTitle(R.string.location_rationale_title)
							.setMessage(R.string.location_rationale_message)
							.setPositiveButton(R.string.continue_dialog, (dialog, which) -> {
								//user has received explanation, now request permission
								ActivityCompat.requestPermissions(
										context,
										new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
										LOCATION_PERMISSION_REQUEST_CODE
								);

								//store the request in the preferences to avoid asking again
								PreferenceManager.getInstance().storeLocationPermissionAlreadyRequested(context, true);
							})
							.create()
							.show();
				});
			} else {
				//we don't need to show any rationale, so just request permission
				ActivityCompat.requestPermissions(
						context,
						new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
						LOCATION_PERMISSION_REQUEST_CODE
				);
			}

			//we don't have the permission right now
			return false;
		} else {
			//we have the permission right now
			return true;
		}
	}

	/**
	 * Get current location a single time.
	 *
	 * @param context context for request
	 * @return the requested location object
	 */
	private Location getCurrentGeoLocation(Context context) {
		// Get LocationManager object
		android.location.LocationManager locationManager = (android.location.LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

		// Create a criteria object to retrieve provider
		Criteria criteria = new Criteria();

		// Get the name of the best provider
		assert locationManager != null;
		String provider = locationManager.getBestProvider(criteria, true);

		// Get Current Location
		Location location;
		if (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			Log.e(TAG, "no location access granted");

			//set the location to dummy coordinates
			location = new Location("dummyprovider");
			location.setLatitude(30.0000);
			location.setLongitude(9.000);
			((Activity) context).runOnUiThread(() -> Toast.makeText(context, context.getString(R.string.location_permission_denied), Toast.LENGTH_SHORT).show());
		} else {
			assert provider != null;
			location = locationManager.getLastKnownLocation(provider);
		}
		return location;
	}
}
