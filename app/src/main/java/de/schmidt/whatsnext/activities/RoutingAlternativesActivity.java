package de.schmidt.whatsnext.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import de.schmidt.mvg.route.RouteConnection;
import de.schmidt.mvg.route.RouteOptions;
import de.schmidt.util.ColorUtils;
import de.schmidt.util.managers.NavBarManager;
import de.schmidt.util.network.RoutingNetworkAccess;
import de.schmidt.whatsnext.R;
import de.schmidt.whatsnext.adapters.AlternativesListViewAdapter;
import de.schmidt.whatsnext.base.ActionBarBaseActivity;
import de.schmidt.whatsnext.base.Updatable;

import java.util.ArrayList;
import java.util.List;

public class RoutingAlternativesActivity extends ActionBarBaseActivity implements Updatable<RouteConnection> {
	private BottomNavigationView navBar;
	private SwipeRefreshLayout swipeRefresh;
	private ListView listView;
	private List<RouteConnection> connections;
	private AlternativesListViewAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_routing_alternatives);

		navBar = findViewById(R.id.bottom_nav_bar_routing_alternatives);
		NavBarManager.getInstance().initialize(navBar, this);

		setTitle(getString(R.string.alternatives_title));

		connections = new ArrayList<>();

		swipeRefresh = findViewById(R.id.pull_to_refresh_alternatives);
		swipeRefresh.setColorSchemeColors(ColorUtils.getSpriteColors(this));
		swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				refresh();
				swipeRefresh.setRefreshing(false);
			}
		});

		listView = findViewById(R.id.alternative_list);
		adapter = new AlternativesListViewAdapter(this, connections);
		listView.setAdapter(adapter);
		listView.setClickable(true);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Toast.makeText(RoutingAlternativesActivity.this, "Clicked " + position, Toast.LENGTH_SHORT).show();
			}
		});
	}

	@Override
	public void refresh() {
		swipeRefresh.setRefreshing(true);
		RouteOptions options = (RouteOptions) getIntent().getSerializableExtra(getString(R.string.key_parameters));
		new RoutingNetworkAccess(this, options).execute();
	}

	@Override
	public void handleUIUpdate(List<RouteConnection> dataSet) {
		if (dataSet == null) return;

		this.connections.clear();
		this.connections.addAll(dataSet);

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
