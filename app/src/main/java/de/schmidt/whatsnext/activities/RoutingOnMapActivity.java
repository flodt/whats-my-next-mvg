package de.schmidt.whatsnext.activities;

import android.graphics.Color;
import androidx.fragment.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.*;
import de.schmidt.mvg.route.RouteConnection;
import de.schmidt.mvg.route.RouteConnectionPart;
import de.schmidt.mvg.route.RouteIntermediateStop;
import de.schmidt.mvg.route.RoutePathLocation;
import de.schmidt.mvg.traffic.Station;
import de.schmidt.whatsnext.R;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RoutingOnMapActivity extends FragmentActivity implements OnMapReadyCallback {
	private GoogleMap mMap;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_routing_on_map);
		// Obtain the SupportMapFragment and get notified when the map is ready to be used.
		SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map);

		//noinspection ConstantConditions
		mapFragment.getMapAsync(this);
	}


	/**
	 * Manipulates the map once available.
	 * This callback is triggered when the map is ready to be used.
	 * This is where we can add markers or lines, add listeners or move the camera. In this case,
	 * we just add a marker near Sydney, Australia.
	 * If Google Play services is not installed on the device, the user will be prompted to install
	 * it inside the SupportMapFragment. This method will only be triggered once the user has
	 * installed Google Play services and returned to the app.
	 */
	@Override
	public void onMapReady(GoogleMap googleMap) {
		mMap = googleMap;

		//grab the route connection from the intent
		RouteConnection routeConnection = (RouteConnection) getIntent().getSerializableExtra(getString(R.string.key_route_map));
		List<RouteConnectionPart> connectionParts = Objects.requireNonNull(routeConnection).getConnectionParts();

		//drop pins at all of the interchange stations
		connectionParts
				.stream()
				.flatMap(rcp -> Stream.of(rcp.getFrom(), rcp.getTo()))
				.distinct()
				.map(station -> new MarkerOptions()
						.position(station.getLatLongForMaps())
						.title(station.getName()))
				.forEach(mMap::addMarker);

		//add polylines for each path segment in the routeconnection (stroke pattern?, tag?)
		connectionParts
				.stream()
				.map(rcp -> new PolylineOptions()
						.addAll(
								rcp.getPath()
										.stream()
										.map(RoutePathLocation::getLatLongForMaps)
										.collect(Collectors.toList())
						)
						.color(Color.parseColor(rcp.getColor().getPrimary()))
						.width(10.0f)
						.zIndex(0.0f)
						.pattern(
								rcp.getLine().equals("Walking") ? Arrays.asList(new Dot(), new Gap(10.0f)) : null
						)
						.clickable(false))
				.forEach(mMap::addPolyline);

		//add polylines for all interchange paths
		connectionParts
				.stream()
				.map(rcp -> new PolylineOptions()
						.addAll(
								rcp.getInterchangePath()
										.stream()
										.map(RoutePathLocation::getLatLongForMaps)
										.collect(Collectors.toList())
						)
						.color(getResources().getColor(R.color.colorPrimaryDark))
						.width(10.0f)
						.zIndex(0.0f)
						.pattern(
								Arrays.asList(new Dot(), new Gap(10.0f))
						)
						.clickable(false))
				.forEach(mMap::addPolyline);

		//add point for every intermediate stop
		connectionParts
				.stream()
				.map(RouteConnectionPart::getStops)
				.flatMap(List::stream)
				.map(RouteIntermediateStop::getStation)
				.map(Station::getLatLongForMaps)
				.map(loc -> new CircleOptions()
						.center(loc)
						.radius(15.0d)
						.strokeWidth(5.0f)
						.fillColor(getColor(R.color.white))
						.zIndex(1.0f)
						.clickable(false))
				.forEach(mMap::addCircle);


		mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(routeConnection.getTo().getLatLongForMaps(), 11.0f));
	}
}
