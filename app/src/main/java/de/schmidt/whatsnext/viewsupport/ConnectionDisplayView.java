package de.schmidt.whatsnext.viewsupport;

import android.view.View;
import de.schmidt.mvg.route.RouteConnection;
import de.schmidt.mvg.route.RouteConnectionPart;

import java.util.ArrayList;
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
						part.getColor(),
						part.getLine()
				));
			} else { //interchange element in list
				views.add(new InterchangeView(
						part.getFrom(),
						parts.get(i - 1).getColor(),
						part.getColor(),
						parts.get(i - 1).getArrival(),
						part.getDeparture(),
						parts.get(i - 1).getArrivalPlatform(),
						part.getDeparturePlatform(),
						part.getLine(),
						part.getDirection()
				));
			}

			views.add(new RunningView(
					part.getColor(),
					part.getStops()
			));

			if (part.getTo().equals(connection.getTo())) { //ending element in list
				views.add(new ArrivingView(
						part.getTo(),
						part.getArrival(),
						part.getArrivalPlatform(),
						part.getColor()
				));
			}
		}

		return views;
	}

	public abstract int getLayoutId();

	/**
	 * This needs to be different than the layoutId, as the ListView uses a list with indices [0..4) internally!
	 * @return view type id int
	 */
	public abstract int getViewType();
	public abstract View inflate(View view, ConnectionDisplayView content);
	public abstract boolean isRunning();
}
