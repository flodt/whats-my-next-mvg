package de.schmidt.whatsnext.viewsupport.list;

import android.content.Context;
import android.util.Log;
import androidx.annotation.DrawableRes;
import de.schmidt.mvg.traffic.Station;

import java.io.Serializable;

public abstract class SwitchStationListItem implements Serializable {
	public abstract @DrawableRes int getDrawable();
	public abstract String getTitle(Context context);
	public abstract boolean isCurrentLocation();
	public abstract Station getFixedStation();
	public abstract String serialize();
	public abstract boolean equals(Object o);

	public static SwitchStationListItem deserialize(String str) {
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
