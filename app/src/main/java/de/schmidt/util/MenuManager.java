package de.schmidt.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import de.schmidt.whatsnext.activities.ActionBarBaseActivity;
import de.schmidt.whatsnext.R;
import de.schmidt.whatsnext.activities.InterruptionsActivity;

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
			case R.id.show_interruptions_button:
				showInterruptions(context);
				break;
		}
	}

	private void showInterruptions(Context context) {
		Intent intent = new Intent(context, InterruptionsActivity.class);
		context.startActivity(intent);
	}

	private void switchActivity(Context context) {
		if (context instanceof ActionBarBaseActivity) {
			((ActionBarBaseActivity) context).switchActivity();
		}
	}

	private void refresh(Context context) {
		if (context instanceof ActionBarBaseActivity) {
			((ActionBarBaseActivity) context).refresh();
		}
	}
}
