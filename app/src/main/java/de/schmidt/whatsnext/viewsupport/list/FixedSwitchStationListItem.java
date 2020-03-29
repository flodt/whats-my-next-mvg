package de.schmidt.whatsnext.viewsupport.list;

import android.content.Context;
import de.schmidt.mvg.traffic.Station;
import de.schmidt.whatsnext.R;

public class FixedSwitchStationListItem extends SwitchStationListItem {
	private final Station station;

	public FixedSwitchStationListItem(Station station) {
		this.station = station;
	}

	@Override
	public int getDrawable() {
		return R.drawable.ic_dark_train;
	}

	@Override
	public String getTitle(Context context) {
		return station.getName();
	}

	@Override
	public boolean isCurrentLocation() {
		return false;
	}

	@Override
	public Station getFixedStation() {
		return station;
	}

	@Override
	public String serialize() {
		final String splitter = "%";
		return station.getId() + splitter + station.getName()
				+ splitter + station.getLatitude() + splitter + station.getLongitude();
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof FixedSwitchStationListItem) {
			return ((FixedSwitchStationListItem) o).getFixedStation().equals(station);
		} else {
			return false;
		}
	}
}
