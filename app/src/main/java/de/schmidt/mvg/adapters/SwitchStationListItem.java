package de.schmidt.mvg.adapters;

import android.content.Context;
import android.util.Log;
import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import de.schmidt.mvg.traffic.Station;
import de.schmidt.whatsnext.R;

public abstract class SwitchStationListItem {
	public abstract @DrawableRes int getDrawable();
	public abstract String getTitle(Context context);
	public abstract boolean isCurrentLocation();
	public abstract Station getFixedStation();
	public abstract String wrapToString();

	public static SwitchStationListItem unwrapFromString(String str) {
		if (str.length() == 0) return null;
		if (str.equals("loc")) return new GeoLocationSwitchStationListItem();

		Log.d("SwitchStationListItem", "unwrapFromString: " + str);

		String[] split = str.split("%");
		return new FixedSwitchStationListItem(
				new Station(
						split[0], split[1], Double.parseDouble(split[2]), Double.parseDouble(split[3])
				)
		);
	}
}
