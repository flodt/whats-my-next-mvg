package de.schmidt.whatsnext.activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.widget.ListView;
import androidx.appcompat.app.ActionBar;
import android.os.Bundle;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import de.schmidt.mvg.route.RouteConnection;
import de.schmidt.mvg.traffic.Station;
import de.schmidt.util.ColorUtils;
import de.schmidt.util.managers.NavBarManager;
import de.schmidt.util.managers.NotificationManager;
import de.schmidt.whatsnext.R;
import de.schmidt.whatsnext.adapters.ItineraryListViewAdapter;
import de.schmidt.whatsnext.base.ActionBarBaseActivity;
import de.schmidt.whatsnext.base.Notifyable;
import de.schmidt.whatsnext.base.Updatable;
import de.schmidt.whatsnext.viewsupport.route.ConnectionDisplayView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RoutingItineraryDisplayActivity extends ActionBarBaseActivity implements Updatable<ConnectionDisplayView>, Notifyable {
	private static final String TAG = "ItineraryDisplayActivity";
	private BottomNavigationView navBar;
	private SwipeRefreshLayout swipeRefresh;
	private ListView listView;
	private List<ConnectionDisplayView> views;
	private ItineraryListViewAdapter adapter;
	private RouteConnection routeConnection;
	private ActionBar actionBar;
	private FloatingActionButton fab;
	private boolean expanded = false;
	private double average;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_routing_itinerary_display);

		navBar = findViewById(R.id.bottom_nav_bar_routing_itinerary);
		NavBarManager.getInstance().initialize(navBar, this);

		setTitle(getString(R.string.itinerary_title));

		views = new ArrayList<>();

		actionBar = getSupportActionBar();

		routeConnection = (RouteConnection) getIntent().getSerializableExtra(getResources().getString(R.string.key_itinerary));

		//add back arrow to action bar if we should
		Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(
				getIntent().getBooleanExtra(getResources().getString(R.string.key_back_button_action_bar), false)
		);

		expanded = getIntent().getBooleanExtra(getResources().getString(R.string.key_display_expanded), false);
		average = getIntent().getDoubleExtra(getString(R.string.key_average_duration), Double.POSITIVE_INFINITY);

		swipeRefresh = findViewById(R.id.pull_to_refresh_itinerary);
		swipeRefresh.setColorSchemeColors(ColorUtils.getSpriteColors(this));
		swipeRefresh.setOnRefreshListener(() -> {
			refresh();
			swipeRefresh.setRefreshing(false);
		});

		listView = findViewById(R.id.itinerary_list);
		adapter = new ItineraryListViewAdapter(this, views);
		listView.setAdapter(adapter);
		listView.setClickable(true);
		listView.setOnItemClickListener((parent, view, position, id) -> {
			//flip expansion of list, then refresh
			expanded = !expanded;
			refresh();
		});
		listView.setLongClickable(true);
		listView.setOnItemLongClickListener((parent, view, position, id) -> {
			ConnectionDisplayView tapped = views.get(position);

			if (tapped.hasStationForMap()) {
				Station station = tapped.getStationForMap();
				Intent intent = new Intent(RoutingItineraryDisplayActivity.this, RoutingOnMapActivity.class);
				intent.putExtra(getResources().getString(R.string.key_route_map), routeConnection);
				intent.putExtra(getResources().getString(R.string.key_route_station), station);
				intent.putExtra(getResources().getString(R.string.key_show_station_detail), true);
				startActivity(intent);
				return true;
			} else {
				return false;
			}
		});

		fab = findViewById(R.id.fab_show_on_map);
		fab.setOnClickListener(v -> {
			Intent intent = new Intent(RoutingItineraryDisplayActivity.this, RoutingOnMapActivity.class);
			intent.putExtra(getResources().getString(R.string.key_route_map), routeConnection);
			startActivity(intent);
		});
	}

	@Override
	public void refresh() {
		swipeRefresh.setRefreshing(true);
		handleUIUpdate(ConnectionDisplayView.getViewListFromRouteConnection(routeConnection, expanded, average, this));
	}

	@Override
	public int getNavButtonItemId() {
		return 0;
	}

	@Override
	public BottomNavigationView getNavBar() {
		return navBar;
	}

	@Override
	public void handleUIUpdate(List<ConnectionDisplayView> dataSet) {
		if (dataSet == null) return;

		this.views.clear();
		this.views.addAll(dataSet);

		runOnUiThread(() -> {
			int[] primaryAndDark = ColorUtils.extractPrimaryAndDark(routeConnection.getFirstColor());
			actionBar.setBackgroundDrawable(new ColorDrawable(primaryAndDark[0]));
			getWindow().setStatusBarColor(primaryAndDark[1]);

			navBar.setBackgroundColor(getColor(R.color.white));
			navBar.setItemIconTintList(ColorStateList.valueOf(routeConnection.getLastColor()));
			navBar.setItemTextColor(ColorStateList.valueOf(routeConnection.getLastColor()));

			//refresh the list view
			adapter.notifyDataSetChanged();
			listView.invalidateViews();
			listView.refreshDrawableState();
			swipeRefresh.setRefreshing(false);
		});
	}

	@Override
	public void sendToNotifcations() {
		NotificationManager.getInstance().sendItinerary(routeConnection, this);
	}
}
