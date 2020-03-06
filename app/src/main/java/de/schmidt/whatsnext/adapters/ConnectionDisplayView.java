package de.schmidt.whatsnext.adapters;

import de.schmidt.mvg.route.RouteConnection;
import de.schmidt.mvg.route.RouteConnectionPart;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class ConnectionDisplayView {
	public static List<ConnectionDisplayView> getViewListFromRouteConnection(RouteConnection connection) {
		List<ConnectionDisplayView> views = new ArrayList<>();

		List<RouteConnectionPart> parts = connection.getConnectionParts();
		for (int i = 0; i < parts.size(); i++) {
			RouteConnectionPart part = parts.get(i);

			if (part.getFrom().equals(connection.getFrom())) { //starting element in list
				views.add(new DepartingView(
						part.getFrom(),
						part.getDeparture(),
						part.getDeparturePlatform(),
						part.getDelay(),
						part.getDirection(),
						part.getColor()
				));
				views.add(new RunningView(
						part.getColor(),
						part.getStops()
				));
			} else if (part.getTo().equals(connection.getTo())) { //ending element in list
				views.add(new ArrivingView(
						part.getTo(),
						part.getArrival(),
						part.getArrivalPlatform(),
						part.getColor()
				));
			} else { //interchange element in list
				views.add(new InterchangeView(
						part.getFrom(),
						parts.get(i - 1).getColor(),
						part.getColor(),
						parts.get(i - 1).getArrival(),
						part.getDeparture(),
						parts.get(i - 1).getArrivalPlatform(),
						part.getDeparturePlatform()
				));
				views.add(new RunningView(
						part.getColor(),
						part.getStops()
				));
			}
		}

		return views;
	}

	public abstract int getLayoutId();
	public abstract boolean isArrival();
	public abstract boolean isInterchange();
	public abstract boolean isRunning();
	public abstract boolean isDeparture();
}
