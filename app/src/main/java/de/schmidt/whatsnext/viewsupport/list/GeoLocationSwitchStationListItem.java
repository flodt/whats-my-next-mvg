package de.schmidt.whatsnext.viewsupport.list;

import android.content.Context;
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
	public String serialize() {
		return "loc";
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof GeoLocationSwitchStationListItem;
	}
}
