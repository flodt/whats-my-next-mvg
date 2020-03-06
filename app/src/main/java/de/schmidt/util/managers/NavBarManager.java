package de.schmidt.util.managers;

import android.content.Intent;
import android.util.Log;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import de.schmidt.whatsnext.R;
import de.schmidt.whatsnext.base.ActionBarBaseActivity;
import de.schmidt.whatsnext.activities.DepartureListActivity;
import de.schmidt.whatsnext.activities.InterruptionsActivity;
import de.schmidt.whatsnext.activities.SingleDepartureActivity;

public class NavBarManager {
	private static final NavBarManager instance = new NavBarManager();
	private static final String TAG = "NavBarManager";

	private NavBarManager() {

	}

	public static NavBarManager getInstance() {
		return instance;
	}

	public void initialize(BottomNavigationView view, ActionBarBaseActivity context) {
		view.setOnNavigationItemSelectedListener(menuItem -> {
			Log.d("Base", "onNavigationItemSelected: menuItem selected");

			//if we are already in the activity the button was pressed for, do nothing
			if (context.getNavButtonItemId() == menuItem.getItemId()) return false;

			Intent intent;
			switch (menuItem.getItemId()) {
				case R.id.nav_single_button:
					intent = new Intent(context, SingleDepartureActivity.class);
					break;
				case R.id.nav_list_button:
					intent = new Intent(context, DepartureListActivity.class);
					break;
				case R.id.nav_inter_button:
					intent = new Intent(context, InterruptionsActivity.class);
					break;
				default:
					return false;
			}

			context.startActivity(intent);
			return false;
		});
	}
}
