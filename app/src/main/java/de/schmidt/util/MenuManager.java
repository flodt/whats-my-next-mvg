package de.schmidt.util;

import android.app.Activity;
import android.content.Context;
import android.view.Menu;
import android.view.MenuItem;
import de.schmidt.whatsnext.DepartureListActivity;
import de.schmidt.whatsnext.R;
import de.schmidt.whatsnext.SingleDepartureActivity;

public class MenuManager {
	private static final MenuManager instance = new MenuManager();
	private static final String TAG = "MenuManager";

	private MenuManager() {

	}

	public static MenuManager getInstance() {
		return instance;
	}

	public void inflate(Menu menu, Activity context) {
		context.getMenuInflater().inflate(R.menu.app_menu, menu);
	}

	public void dispatch(MenuItem item, Context context) {
		switch (item.getItemId()) {
			case R.id.refresh_button:
				refresh(context);
				break;
			case R.id.exclude_button:
				PreferenceManager.getInstance().updateExclusions(context);
				break;
			case R.id.select_station_button:
				PreferenceManager.getInstance().updateStationSelection(context);
				break;
			case R.id.switch_list_button:
				switchActivity(context);
				break;
		}
	}

	private void switchActivity(Context context) {
		if (context instanceof SingleDepartureActivity) {
			((SingleDepartureActivity) context).switchActivity();
		} else if (context instanceof DepartureListActivity) {
			((DepartureListActivity) context).switchActivity();
		}
	}

	private void refresh(Context context) {
		if (context instanceof SingleDepartureActivity) {
			((SingleDepartureActivity) context).refresh();
		} else if (context instanceof DepartureListActivity) {
			((DepartureListActivity) context).refresh();
		}
	}
}