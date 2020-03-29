package de.schmidt.util.managers;

import android.content.Context;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import de.schmidt.whatsnext.base.ActionBarBaseActivity;
import de.schmidt.whatsnext.R;
import de.schmidt.whatsnext.base.Shortcutable;

public class MenuManager {
	private static final MenuManager instance = new MenuManager();
	private static final String TAG = "MenuManager";

	private MenuManager() {

	}

	public static MenuManager getInstance() {
		return instance;
	}

	/**
	 * Inflate the passed menu in the relevant context.
	 * @param menu menu object to inflate
	 * @param context in which context
	 */
	public void inflate(Menu menu, ActionBarBaseActivity context) {
		context.getMenuInflater().inflate(R.menu.app_menu, menu);
	}

	/**
	 * Dispatch the pressed menu item in context.
	 * @param item the pressed item
	 * @param context for calling the relevant methods (i.e. refreshing)
	 */
	public void dispatch(MenuItem item, Context context) {
		switch (item.getItemId()) {
			case R.id.refresh_button:
				refresh(context);
				break;
			case R.id.exclude_button:
				PreferenceManager.getInstance().updateExclusions(context);
				break;
			case R.id.create_shortcut_button:
				createShortcut(context);
				break;
		}
	}

	private void refresh(Context context) {
		if (context instanceof ActionBarBaseActivity) {
			((ActionBarBaseActivity) context).refresh();
		}
	}

	private void createShortcut(Context context) {
		//if we can create a shortcut, do so, else show an error message
		if (context instanceof Shortcutable) {
			((Shortcutable) context).createShortcut();
		} else {
			Toast.makeText(context, context.getString(R.string.create_shortcut_error), Toast.LENGTH_SHORT).show();
		}
	}
}
