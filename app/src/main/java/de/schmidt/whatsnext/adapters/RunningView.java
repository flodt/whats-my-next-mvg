package de.schmidt.whatsnext.adapters;

import de.schmidt.mvg.route.RouteIntermediateStop;
import de.schmidt.mvg.traffic.LineColor;
import de.schmidt.whatsnext.R;

import java.util.List;

public class RunningView extends ConnectionDisplayView {
	private final LineColor color;
	private final List<RouteIntermediateStop> stops;

	public RunningView(LineColor color, List<RouteIntermediateStop> stops) {
		this.color = color;
		this.stops = stops;
	}

	public LineColor getColor() {
		return color;
	}

	public List<RouteIntermediateStop> getStops() {
		return stops;
	}

	@Override
	public int getLayoutId() {
		return R.layout.connection_list_item_running;
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
		return true;
	}

	@Override
	public boolean isDeparture() {
		return false;
	}

	@Override
	public String toString() {
		return "RunningView{" +
				"color=" + color +
				", stops=" + stops +
				'}';
	}
}
