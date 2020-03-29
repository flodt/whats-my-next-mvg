package de.schmidt.whatsnext.base;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import androidx.core.content.pm.ShortcutInfoCompat;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.core.graphics.drawable.IconCompat;
import de.schmidt.whatsnext.R;

public interface Shortcutable {
	/**
	 * Method called upon menu item selection.
	 * Implemented in the Activities that support home screen shortcuts.
	 */
	void createShortcut();

	/**
	 * Performs the raw shortcut request to place a shortcut on the launcher.
	 * @param context the context for the request
	 * @param launchIntent the intent to call with the shortcut
	 * @param label the shortcut's label
	 * @param icon the shortcut's icon
	 */
	static void requestShortcut(Context context, Intent launchIntent, String label, int icon) {
		//check if shortcut creation is supported, if yes, send request
		if (ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
			ShortcutInfoCompat shortcut = new ShortcutInfoCompat.Builder(context, label)
					.setIntent(launchIntent)
					.setShortLabel(label)
					.setIcon(IconCompat.createWithResource(context, icon))
					.build();
			ShortcutManagerCompat.requestPinShortcut(context, shortcut, null);
			Toast.makeText(context, context.getString(R.string.create_shortcut_success), Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(context, context.getString(R.string.create_shortcut_not_supported), Toast.LENGTH_SHORT).show();
		}
	}
}
