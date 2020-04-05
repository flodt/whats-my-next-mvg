package de.schmidt.util.managers;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.view.Window;
import androidx.annotation.ColorRes;
import androidx.appcompat.app.ActionBar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import de.schmidt.util.ColorUtils;
import de.schmidt.whatsnext.R;

public class ThemeManager {
	private static ThemeManager instance = new ThemeManager();

	public ThemeManager() {}

	public static ThemeManager getInstance() {
		return instance;
	}

	public boolean isInDarkMode(Context context) {
		switch (context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
			case Configuration.UI_MODE_NIGHT_YES:
				return true;
			case Configuration.UI_MODE_NIGHT_NO:
			default:
				return false;
		}
	}

	public void initializeActionBar(Context context, ActionBar bar, Window window) {
		int[] primaryAndDark = ColorUtils.extractPrimaryAndDark(context.getColor(R.color.actionBar));
		bar.setBackgroundDrawable(new ColorDrawable(primaryAndDark[0]));
		window.setStatusBarColor(primaryAndDark[1]);
	}

	public void initializeActionBarWithColor(Context context, ActionBar bar, Window window, @ColorRes int color) {
		int[] primaryAndDark = ColorUtils.extractPrimaryAndDark(
				context.getColor(isInDarkMode(context) ? R.color.actionBar : color));
		bar.setBackgroundDrawable(new ColorDrawable(primaryAndDark[0]));
		window.setStatusBarColor(primaryAndDark[1]);
	}

	public void initializeNavBarWithAccent(Context context, BottomNavigationView navBar, @ColorRes int accent) {
		navBar.setBackgroundColor(context.getColor(R.color.navBarBackground));
		navBar.setItemIconTintList(ColorStateList.valueOf(context.getColor(accent)));
		navBar.setItemTextColor(ColorStateList.valueOf(context.getColor(accent)));
	}
}
