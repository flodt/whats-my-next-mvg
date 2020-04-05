package de.schmidt.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.view.Window;
import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import de.schmidt.util.managers.PreferenceManager;
import de.schmidt.whatsnext.R;
import de.schmidt.whatsnext.activities.RoutingEntryActivity;

public class ThemeUtils {
	private static ThemeUtils instance = new ThemeUtils();

	public static final int THEME_FOLLOW_SYSTEM = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
	public static final int THEME_LIGHT = AppCompatDelegate.MODE_NIGHT_NO;
	public static final int THEME_DARK = AppCompatDelegate.MODE_NIGHT_YES;

	public ThemeUtils() {}

	public static ThemeUtils getInstance() {
		return instance;
	}

	public void updateThemeSetting(Context context, int newThemeSetting) {
		PreferenceManager.getInstance().setThemeSelection(context, newThemeSetting);
		AppCompatDelegate.setDefaultNightMode(newThemeSetting);

		//recreate activity to reflect theming changes
		ActivityCompat.finishAffinity((Activity) context);
		Intent intent = new Intent(context, RoutingEntryActivity.class);
		context.startActivity(intent);
	}

	public boolean isInDarkMode(Context context) {
		switch (PreferenceManager.getInstance().getThemeSelection(context)) {
			default:
			case THEME_FOLLOW_SYSTEM:
				return isInDarkModeBySystem(context);
			case THEME_LIGHT:
				return false;
			case THEME_DARK:
				return true;
		}
	}

	private boolean isInDarkModeBySystem(Context context) {
		switch (context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
			case Configuration.UI_MODE_NIGHT_YES:
				return true;
			case Configuration.UI_MODE_NIGHT_NO:
			default:
				return false;
		}
	}

	public boolean isInLightMode(Context context) {
		return !isInDarkMode(context);
	}

	public void initializeActionBar(Context context, ActionBar bar, Window window) {
		int[] primaryAndDark = ColorUtils.extractPrimaryAndDark(context.getColor(R.color.actionBar));
		bar.setBackgroundDrawable(new ColorDrawable(primaryAndDark[0]));
		window.setStatusBarColor(primaryAndDark[1]);
	}

	public void initializeActionBarWithColorResource(Context context, ActionBar bar, Window window, @ColorRes int color) {
		int[] primaryAndDark = ColorUtils.extractPrimaryAndDark(
				context.getColor(isInDarkMode(context) ? R.color.actionBar : color));
		bar.setBackgroundDrawable(new ColorDrawable(primaryAndDark[0]));
		window.setStatusBarColor(primaryAndDark[1]);
	}

	public void initializeActionBarWithColorRaw(Context context, ActionBar bar, Window window, @ColorInt int color) {
		int[] primaryAndDark = ColorUtils.extractPrimaryAndDark(
				isInDarkMode(context) ? context.getColor(R.color.actionBar) : color);
		bar.setBackgroundDrawable(new ColorDrawable(primaryAndDark[0]));
		window.setStatusBarColor(primaryAndDark[1]);
	}

	public void initializeNavBarWithAccentResource(Context context, BottomNavigationView navBar, @ColorRes int accent) {
		//only allow accent when in light mode, does not look good for some colors
		if (isInDarkMode(context)) accent = R.color.mvg_1;
		navBar.setBackgroundColor(context.getColor(R.color.navBarBackground));
		navBar.setItemIconTintList(ColorStateList.valueOf(context.getColor(accent)));
		navBar.setItemTextColor(ColorStateList.valueOf(context.getColor(accent)));
	}

	public void initializeNavBarWithAccentRaw(Context context, BottomNavigationView navBar, @ColorInt int accent) {
		//only allow accent when in light mode, does not look good for some colors
		if (isInDarkMode(context)) accent = context.getColor(R.color.mvg_1);
		navBar.setBackgroundColor(context.getColor(R.color.navBarBackground));
		navBar.setItemIconTintList(ColorStateList.valueOf(accent));
		navBar.setItemTextColor(ColorStateList.valueOf(accent));
	}
}
