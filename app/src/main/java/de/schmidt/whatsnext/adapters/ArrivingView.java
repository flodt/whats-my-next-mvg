package de.schmidt.whatsnext.adapters;

import de.schmidt.mvg.traffic.LineColor;
import de.schmidt.mvg.traffic.Station;
import de.schmidt.whatsnext.R;

import java.util.Date;

public class ArrivingView extends ConnectionDisplayView {
	private final Station to;
	private final Date arrival;
	private final String arrivalPlatform;
	private final LineColor color;

	public ArrivingView(Station to, Date arrival, String arrivalPlatform, LineColor color) {
		this.to = to;
		this.arrival = arrival;
		this.arrivalPlatform = arrivalPlatform;
		this.color = color;
	}

	public Station getTo() {
		return to;
	}

	public Date getArrival() {
		return arrival;
	}

	public String getArrivalPlatform() {
		return arrivalPlatform;
	}

	public LineColor getColor() {
		return color;
	}

	@Override
	public int getLayoutId() {
		return R.layout.connection_list_item_arrival;
	}

	@Override
	public boolean isArrival() {
		return true;
	}

	@Override
	public boolean isInterchange() {
		return false;
	}

	@Override
	public boolean isRunning() {
		return false;
	}

	@Override
	public boolean isDeparture() {
		return false;
	}
}
