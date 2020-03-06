package de.schmidt.mvg;

import org.json.JSONObject;

import java.util.Date;
import java.util.List;

public class RouteConnectionPart {
	private Station from;
	private Station to;
	private List<RoutePathLocation> path;
	private Date departure;
	private Date arrival;
	private int delay;
	private String line;
	private String direction;
	private String departurePlatform;
	private String arrivalPlatform;

	public static RouteConnectionPart fromJSON(JSONObject json) {
		// TODO: 06.03.20
		return null;
	}
}
