package de.schmidt.whatsnext.adapters;

import de.schmidt.mvg.traffic.LineColor;
import de.schmidt.mvg.traffic.Station;
import de.schmidt.whatsnext.R;

import java.util.Date;

public class InterchangeView extends ConnectionDisplayView {
	private final Station at;
	private final LineColor formerColor;
	private final LineColor nextColor;
	private final Date arrival;
	private final Date departure;
	private final String arrivalPlatform;
	private final String departurePlatform;

	public InterchangeView(Station at, LineColor formerColor, LineColor nextColor, Date arrival, Date departure, String arrivalPlatform, String departurePlatform) {
		this.at = at;
		this.formerColor = formerColor;
		this.nextColor = nextColor;
		this.arrival = arrival;
		this.departure = departure;
		this.arrivalPlatform = arrivalPlatform;
		this.departurePlatform = departurePlatform;
	}

	public Station getAt() {
		return at;
	}

	public LineColor getFormerColor() {
		return formerColor;
	}

	public LineColor getNextColor() {
		return nextColor;
	}

	public Date getArrival() {
		return arrival;
	}

	public Date getDeparture() {
		return departure;
	}

	public String getArrivalPlatform() {
		return arrivalPlatform;
	}

	public String getDeparturePlatform() {
		return departurePlatform;
	}

	@Override
	public int getLayoutId() {
		return R.layout.connection_list_item_interchange;
	}

	@Override
	public boolean isArrival() {
		return false;
	}

	@Override
	public boolean isInterchange() {
		return true;
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
