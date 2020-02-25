package de.schmidt.mvg;

import android.location.Location;
import org.json.JSONException;
import org.junit.Test;

public class RequestTest {
	@Test
	public void runRequest() throws JSONException {
		Request request = new Request();

		Location loc = new Location("dummyprovider");
		loc.setLatitude(48.210030);
		loc.setLongitude(11.614880);

		Station nearestStation = request.getNearestStation(loc);
		System.out.println(nearestStation);
	}
}