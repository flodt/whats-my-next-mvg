package de.schmidt.whatsnext.base;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import de.schmidt.util.ThemeUtils;
import de.schmidt.util.managers.LocationManager;
import de.schmidt.util.managers.MenuManager;
import de.schmidt.util.managers.NotificationManager;
import de.schmidt.util.managers.PreferenceManager;
import de.schmidt.whatsnext.R;

public abstract class ActionBarBaseActivity extends AppCompatActivity implements Cacheable {
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

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setup notification channel for the app
		NotificationManager.getInstance().createNotificationChannel(getApplicationContext());

		//make sure the theme settings sticks
		ThemeUtils.getInstance().refreshThemeSetting(this);

		//verify location permission here
		LocationManager.getInstance().checkLocationPermission(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		getNavBar().setSelectedItemId(getNavButtonItemId());
		updateFromCache();
		refresh();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);

		//check if request code is ours
		if (requestCode == LocationManager.LOCATION_PERMISSION_REQUEST_CODE) {
			//check if we have our permission
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				//permission was granted
				Toast.makeText(this, getString(R.string.location_permission_granted), Toast.LENGTH_SHORT).show();
			} else {
				//store the request in preferences to avoid asking again
				PreferenceManager.getInstance().storeLocationPermissionAlreadyRequested(this, true);
			}
		}
	}

	public abstract void refresh();
	public abstract int getNavButtonItemId();
	public abstract BottomNavigationView getNavBar();
}
