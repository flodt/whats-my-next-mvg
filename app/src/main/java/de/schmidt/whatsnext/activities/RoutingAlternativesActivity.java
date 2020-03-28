package de.schmidt.whatsnext.activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.widget.ListView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import de.schmidt.mvg.route.RouteConnection;
import de.schmidt.mvg.route.RouteOptions;
import de.schmidt.mvg.route.TimeShift;
import de.schmidt.util.ColorUtils;
import de.schmidt.util.caching.RoutingOptionsCache;
import de.schmidt.util.managers.NavBarManager;
import de.schmidt.util.network.RoutingNetworkAccess;
import de.schmidt.whatsnext.R;
import de.schmidt.whatsnext.adapters.AlternativesListViewAdapter;
import de.schmidt.whatsnext.base.ActionBarBaseActivity;
import de.schmidt.whatsnext.base.Updatable;
import de.schmidt.whatsnext.viewsupport.alternatives.AlternativesDisplayView;
import de.schmidt.whatsnext.viewsupport.alternatives.AlternativesRouteView;
import de.schmidt.whatsnext.viewsupport.alternatives.AlternativesTimeChangeView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Stream;

public class RoutingAlternativesActivity extends ActionBarBaseActivity implements Updatable<RouteConnection> {
	private static final String TAG = "RoutingAlternativesActivity";
	private BottomNavigationView navBar;
	private SwipeRefreshLayout swipeRefresh;
	private ListView listView;
	private List<AlternativesDisplayView> views;
	private AlternativesListViewAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_routing_alternatives);

		navBar = findViewById(R.id.bottom_nav_bar_routing_alternatives);
		NavBarManager.getInstance().initialize(navBar, this);

		setTitle(getString(R.string.alternatives_title));

		views = new ArrayList<>();

		swipeRefresh = findViewById(R.id.pull_to_refresh_alternatives);
		swipeRefresh.setColorSchemeColors(ColorUtils.getSpriteColors(this));
		swipeRefresh.setOnRefreshListener(() -> {
			refresh();
			swipeRefresh.setRefreshing(false);
		});

		//add back arrow to action bar
		Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

		listView = findViewById(R.id.alternative_list);
		adapter = new AlternativesListViewAdapter(this, views);
		listView.setAdapter(adapter);
		listView.setClickable(true);
		listView.setOnItemClickListener((parent, view, position, id) -> {
			AlternativesDisplayView tapped = views.get(position);

			if (tapped.isTimeShiftButton()) {
				TimeShift direction = ((AlternativesTimeChangeView) tapped).getTemporalDirection();

				//if the tapped element is a time shift button, we need to refresh the screen with new RouteOptions
				//get the options from the cached value (re-fetched from the intent at every refresh, if present)
				RouteOptions options = RoutingOptionsCache.getInstance().getCache();

				//add or subtract 1.5 hours to/from the request
				long raw = Long.parseLong(
						options
								.getProperties()
								.getOrDefault("time",
											  Long.toString(new Date().getTime())
								)
				);
				boolean wasArrival = Boolean.parseBoolean(options.getProperties().getOrDefault("arrival", "false"));

				Date shifted = new Date(raw + direction.getOperation() * (TimeUnit.MINUTES.toMillis(30)));

				RouteOptions modifiedOptions = options.withTime(shifted, !wasArrival);

				//restart the activity with the new time
				Intent intent = new Intent(RoutingAlternativesActivity.this, RoutingAlternativesActivity.class);
				intent.putExtra(getString(R.string.key_parameters), modifiedOptions);
				startActivity(intent);
			} else {
				//if it's a connection, display details as usual
				//get the tapped connection and show the details on it
				RouteConnection tappedConnection = ((AlternativesRouteView) tapped).getRouteConnection();

				Intent intent = new Intent(RoutingAlternativesActivity.this, RoutingItineraryDisplayActivity.class);
				intent.putExtra(getString(R.string.key_itinerary), tappedConnection);
				intent.putExtra(getString(R.string.key_back_button_action_bar), true);
				double average = views
						.stream()
						.filter(AlternativesDisplayView::hasRouteConnection)
						.map(adv -> (AlternativesRouteView) adv)
						.map(AlternativesRouteView::getRouteConnection)
						.mapToLong(RouteConnection::getDurationInMinutes)
						.mapToInt(l -> (int) l)
						.average()
						.orElse(Double.POSITIVE_INFINITY);
				intent.putExtra(getString(R.string.key_average_duration), average);
				startActivity(intent);
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();

		//set colors
		int[] primaryAndDark = ColorUtils.extractPrimaryAndDark(getColor(R.color.mvg_1));
		Objects.requireNonNull(getSupportActionBar()).setBackgroundDrawable(new ColorDrawable(primaryAndDark[0]));
		getWindow().setStatusBarColor(primaryAndDark[1]);

		navBar.setBackgroundColor(getColor(R.color.white));
		navBar.setItemIconTintList(ColorStateList.valueOf(getColor(R.color.mvg_1)));
		navBar.setItemTextColor(ColorStateList.valueOf(getColor(R.color.mvg_1)));
	}

	@Override
	public void refresh() {
		swipeRefresh.setRefreshing(true);
		RouteOptions options = (RouteOptions) getIntent().getSerializableExtra(getString(R.string.key_parameters));

		//the intent does not contain the extra if the activity is launched over the back button!
		// - so we handle this here by caching the RouteOptions until we next visit the entry activity
		if (options != null) {
			RoutingOptionsCache.getInstance().setCache(options);
			new RoutingNetworkAccess(this, options).execute();
		} else if (RoutingOptionsCache.getInstance().isPresent()) {
			new RoutingNetworkAccess(this, RoutingOptionsCache.getInstance().getCache()).execute();
		}
	}

	@Override
	public void handleUIUpdate(List<RouteConnection> dataSet) {
		if (dataSet == null) return;

		//clear and copy data to field
		this.views.clear();

		//add in the earlier and later buttons, wrap the RouteConnections in view support objects
		//there are some really fishy things going on in this Stream call chain with what I guess is a mixture of
		//	grabbing the first fitting superclass of the two different Stream types in the flatMap
		//	and determining the static type of the object for the consumer in forEach.
		//	It compiles as a lambda (.flatMap(t -> t), but it does not compile for .flatMap(Function.identity()),
		//	as it get's an object and cannot convert that into the AlternativesDisplayView class.
		Stream.of(
				Stream.of(new AlternativesTimeChangeView(TimeShift.EARLIER)),
				dataSet.stream().map(AlternativesRouteView::new),
				Stream.of(new AlternativesTimeChangeView(TimeShift.LATER))
		)
				.flatMap(t -> t)
				.forEach(views::add);

		runOnUiThread(() -> {
			//refresh the list view
			adapter.notifyDataSetChanged();
			listView.invalidateViews();
			listView.refreshDrawableState();
			swipeRefresh.setRefreshing(false);
		});
	}

	@Override
	public int getNavButtonItemId() {
		return 0;
	}

	@Override
	public BottomNavigationView getNavBar() {
		return navBar;
	}
}
