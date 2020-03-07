package de.schmidt.whatsnext.activities;

import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.ActionBar;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import de.schmidt.mvg.route.RouteConnection;
import de.schmidt.mvg.route.RouteIntermediateStop;
import de.schmidt.mvg.traffic.Station;
import de.schmidt.util.ColorUtils;
import de.schmidt.util.managers.NavBarManager;
import de.schmidt.whatsnext.R;
import de.schmidt.whatsnext.adapters.ItineraryListViewAdapter;
import de.schmidt.whatsnext.base.ActionBarBaseActivity;
import de.schmidt.whatsnext.base.Updatable;
import de.schmidt.whatsnext.viewsupport.ConnectionDisplayView;
import de.schmidt.whatsnext.viewsupport.RunningView;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RoutingItineraryDisplayActivity extends ActionBarBaseActivity implements Updatable<ConnectionDisplayView> {
	private static final String TAG = "ItineraryDisplayActivity";
	private BottomNavigationView navBar;
	private SwipeRefreshLayout swipeRefresh;
	private ListView listView;
	private List<ConnectionDisplayView> views;
	private ItineraryListViewAdapter adapter;
	private RouteConnection routeConnection;
	private ActionBar actionBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_itinerary_display);

		navBar = findViewById(R.id.bottom_nav_bar_routing_itinerary);
		NavBarManager.getInstance().initialize(navBar, this);

		setTitle(getString(R.string.itinerary_title));

		views = new ArrayList<>();

		actionBar = getSupportActionBar();

		routeConnection = (RouteConnection) getIntent().getSerializableExtra(getResources().getString(R.string.key_itinerary));

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
			List<ConnectionDisplayView> views = ConnectionDisplayView.getViewListFromRouteConnection(routeConnection);
			if (views.get(position).isRunning()) {
				RunningView running = (RunningView) views.get(position);
				String stops = running.getStops()
						.stream()
						.map(RouteIntermediateStop::getStation)
						.map(Station::getName)
						.map(str -> "- " + str)
						.collect(Collectors.joining("\n"));

				if (stops.length() != 0) {
					new AlertDialog.Builder(this)
							.setTitle(getResources().getString(R.string.intermediate_stops_title))
							.setMessage(stops)
							.setPositiveButton("OK", null)
							.setIcon(getResources().getDrawable(R.drawable.ic_stops))
							.show();
				}
			}
		});
	}

	@Override
	public void refresh() {
		swipeRefresh.setRefreshing(true);
		handleUIUpdate(ConnectionDisplayView.getViewListFromRouteConnection(routeConnection));
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
}
