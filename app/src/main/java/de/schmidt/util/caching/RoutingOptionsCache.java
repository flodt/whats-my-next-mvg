package de.schmidt.util.caching;

import de.schmidt.mvg.route.RouteOptions;

import java.util.Objects;

/**
 * This class saves the RouteOptions for the alternatives view
 * for correct back button behaviour in the itinerary and alternatives
 * display activities.
 */
public class RoutingOptionsCache {
	private static final RoutingOptionsCache instance = new RoutingOptionsCache();
	private RouteOptions options;

	private RoutingOptionsCache() {
		options = null;
	}

	public static RoutingOptionsCache getInstance() {
		return instance;
	}

	public RouteOptions getCache() {
		return Objects.requireNonNull(options);
	}

	public void setCache(RouteOptions options) {
		this.options = options;
	}

	public void clearCache() {
		//this should be cleared every time it is known invalid, as the purpose of this cache is solely
		//to keep the options upon reloading of the activity for the correct behaviour of the back button
		//in the action bar!
		this.options = null;
	}

	public boolean isPresent() {
		return this.options != null;
	}
}
