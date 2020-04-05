package de.schmidt.util.managers;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.view.Window;
import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.appcompat.app.ActionBar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import de.schmidt.util.ColorUtils;
import de.schmidt.whatsnext.R;

public class ThemeManager {
	//language=JSON
	public static final String MAP_STYLE_DARK = "[ { \"elementType\": \"geometry\", \"stylers\": [ { \"color\": \"#212121\" } ] }, { \"elementType\": \"labels.icon\", \"stylers\": [ { \"visibility\": \"off\" } ] }, { \"elementType\": \"labels.text.fill\", \"stylers\": [ { \"color\": \"#757575\" } ] }, { \"elementType\": \"labels.text.stroke\", \"stylers\": [ { \"color\": \"#212121\" } ] }, { \"featureType\": \"administrative\", \"elementType\": \"geometry\", \"stylers\": [ { \"color\": \"#757575\" } ] }, { \"featureType\": \"administrative.country\", \"elementType\": \"labels.text.fill\", \"stylers\": [ { \"color\": \"#9e9e9e\" } ] }, { \"featureType\": \"administrative.land_parcel\", \"stylers\": [ { \"visibility\": \"off\" } ] }, { \"featureType\": \"administrative.locality\", \"elementType\": \"labels.text.fill\", \"stylers\": [ { \"color\": \"#bdbdbd\" } ] }, { \"featureType\": \"poi\", \"elementType\": \"labels.text.fill\", \"stylers\": [ { \"color\": \"#757575\" } ] }, { \"featureType\": \"poi.park\", \"elementType\": \"geometry\", \"stylers\": [ { \"color\": \"#181818\" } ] }, { \"featureType\": \"poi.park\", \"elementType\": \"labels.text.fill\", \"stylers\": [ { \"color\": \"#616161\" } ] }, { \"featureType\": \"poi.park\", \"elementType\": \"labels.text.stroke\", \"stylers\": [ { \"color\": \"#1b1b1b\" } ] }, { \"featureType\": \"road\", \"elementType\": \"geometry.fill\", \"stylers\": [ { \"color\": \"#2c2c2c\" } ] }, { \"featureType\": \"road\", \"elementType\": \"labels.text.fill\", \"stylers\": [ { \"color\": \"#8a8a8a\" } ] }, { \"featureType\": \"road.arterial\", \"elementType\": \"geometry\", \"stylers\": [ { \"color\": \"#373737\" } ] }, { \"featureType\": \"road.highway\", \"elementType\": \"geometry\", \"stylers\": [ { \"color\": \"#3c3c3c\" } ] }, { \"featureType\": \"road.highway.controlled_access\", \"elementType\": \"geometry\", \"stylers\": [ { \"color\": \"#4e4e4e\" } ] }, { \"featureType\": \"road.local\", \"elementType\": \"labels.text.fill\", \"stylers\": [ { \"color\": \"#616161\" } ] }, { \"featureType\": \"transit\", \"elementType\": \"labels.text.fill\", \"stylers\": [ { \"color\": \"#757575\" } ] }, { \"featureType\": \"water\", \"elementType\": \"geometry\", \"stylers\": [ { \"color\": \"#000000\" } ] }, { \"featureType\": \"water\", \"elementType\": \"labels.text.fill\", \"stylers\": [ { \"color\": \"#3d3d3d\" } ] } ]";

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
