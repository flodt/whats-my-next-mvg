package de.schmidt.whatsnext.activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.widget.ListView;
import androidx.annotation.DrawableRes;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import de.schmidt.mvg.route.RouteConnection;
import de.schmidt.mvg.route.RouteOptions;
import de.schmidt.mvg.route.TimeShift;
import de.schmidt.util.ColorUtils;
import de.schmidt.util.caching.RoutingOptionsCache;
import de.schmidt.util.managers.NavBarManager;
import de.schmidt.util.managers.PreferenceManager;
import de.schmidt.util.network.RoutingNetworkAccess;
import de.schmidt.whatsnext.R;
import de.schmidt.whatsnext.adapters.AlternativesListViewAdapter;
import de.schmidt.whatsnext.base.ActionBarBaseActivity;
import de.schmidt.whatsnext.base.Shortcutable;
import de.schmidt.whatsnext.base.Updatable;
import de.schmidt.whatsnext.viewsupport.alternatives.AlternativesDisplayView;
import de.schmidt.whatsnext.viewsupport.alternatives.AlternativesRouteView;
import de.schmidt.whatsnext.viewsupport.alternatives.AlternativesTimeChangeView;
import de.schmidt.whatsnext.viewsupport.list.SwitchStationListItem;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Stream;

public class RoutingAlternativesActivity extends ActionBarBaseActivity implements Updatable<RouteConnection>, Shortcutable {
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
				boolean wasArrival = Boolean.parseBoolean(options.getProperties().getOrDefault("arrival", "false"));

				//get the first/last object in the currently displayed views as time baseline for the new request
				long raw;
				if (direction == TimeShift.EARLIER) {
					//get first
					raw = views.stream()
							.filter(AlternativesDisplayView::hasRouteConnection)
							.map(adv -> (AlternativesRouteView) adv)
							.map(AlternativesRouteView::getRouteConnection)
							.map(rc -> wasArrival ? rc.getArrivalTime() : rc.getDepartureTime())
							.findFirst()
							.orElse(new Date())
							.getTime();

					//subtract 30 minutes from the returned time
					raw = raw - TimeUnit.MINUTES.toMillis(30);
				} else {
					//get last (views.size() - 3 as we filter out "Earlier"/"Later" buttons and skip all but the last
					raw = views.stream()
							.filter(AlternativesDisplayView::hasRouteConnection)
							.map(adv -> (AlternativesRouteView) adv)
							.map(AlternativesRouteView::getRouteConnection)
							.map(rc -> wasArrival ? rc.getArrivalTime() : rc.getDepartureTime())
							.skip(views.size() - 3)
							.findFirst()
							.orElse(new Date())
							.getTime();
				}

				Date shifted = new Date(raw);
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

		//try getting route options from intent (entry activity)
		RouteOptions options = (RouteOptions) getIntent().getSerializableExtra(getString(R.string.key_parameters));

		//if it's not present, try getting it from the shortcut intent
		String rawOptions = getIntent().getStringExtra(getString(R.string.key_route_options_from_shortcut));
		if (options == null && rawOptions != null) {
			options = RouteOptions.fromParameterString(rawOptions);
		}

		//if we now have a value, write it to the cache
		if (options != null) {
			RoutingOptionsCache.getInstance().setCache(options);
		}

		//now get the value from the cache and execute network access
		RouteOptions cached = RoutingOptionsCache.getInstance().getCache();
		new RoutingNetworkAccess(this, cached).execute();
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
			listView.setSelection(1); //scroll the "Earlier" button out of the screen
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

	@Override
	public void createShortcut() {
		//build the intent that's called on tap
		RouteOptions options = RoutingOptionsCache.getInstance().getCache();
		Intent launchIntent = new Intent(getApplicationContext(), RoutingAlternativesActivity.class);
		launchIntent.putExtra(getString(R.string.key_route_options_from_shortcut), options.getParameterString());
		launchIntent.setAction(Intent.ACTION_MAIN);

		final String label = getString(R.string.navbar_route);
		final @DrawableRes int icon = R.mipmap.ic_route_shortcut_round;

		//request shortcut in launcher
		Shortcutable.requestShortcut(this, launchIntent, label, icon);
	}
}
