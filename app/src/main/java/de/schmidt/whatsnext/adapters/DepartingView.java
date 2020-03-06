package de.schmidt.whatsnext.adapters;

import de.schmidt.mvg.traffic.LineColor;
import de.schmidt.mvg.traffic.Station;
import de.schmidt.whatsnext.R;

import java.util.Date;

public class DepartingView extends ConnectionDisplayView {
	private final Station from;
	private final Date departure;
	private final String departurePlatform;
	private final int delay;
	private final String direction;
	private final LineColor color;

	public DepartingView(Station from, Date departure, String departurePlatform, int delay, String direction, LineColor color) {
		this.from = from;
		this.departure = departure;
		this.departurePlatform = departurePlatform;
		this.delay = delay;
		this.direction = direction;
		this.color = color;
	}

	public Station getFrom() {
		return from;
	}

	public Date getDeparture() {
		return departure;
	}

	public String getDeparturePlatform() {
		return departurePlatform;
	}

	public int getDelay() {
		return delay;
	}

	public String getDirection() {
		return direction;
	}

	public LineColor getColor() {
		return color;
	}

	@Override
	public int getLayoutId() {
		return R.layout.connection_list_item_departure;
	}

	@Override
	public boolean isArrival() {
		return false;
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
		return true;
	}

	@Override
	public String toString() {
		return "DepartingView{" +
				"from=" + from +
				", departure=" + departure +
				", departurePlatform='" + departurePlatform + '\'' +
				", delay=" + delay +
				", direction='" + direction + '\'' +
				", color=" + color +
				'}';
	}
}
