package de.schmidt.mvg.adapters;

import android.content.Context;
import androidx.annotation.Nullable;
import de.schmidt.mvg.traffic.Station;
import de.schmidt.whatsnext.R;

public class GeoLocationSwitchStationListItem extends SwitchStationListItem {
	@Override
	public int getDrawable() {
		return R.drawable.ic_current_location;
	}

	@Override
	public String getTitle(Context context) {
		return context.getResources().getString(R.string.current_location);
	}

	@Override
	public boolean isCurrentLocation() {
		return true;
	}

	@Override
	public Station getFixedStation() {
		return null;
	}

	@Override
	public String wrapToString() {
		return "loc";
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof GeoLocationSwitchStationListItem;
	}
}
