package de.schmidt.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.util.Log;

public class LocationManager {
	private static final LocationManager instance = new LocationManager();
	private static final String TAG = "LocationManager";

	private LocationManager() {

	}

	public static LocationManager getInstance() {
		return instance;
	}

	public Location getLocation(Context context) {
		return getCurrentGeoLocation(context);
	}

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
