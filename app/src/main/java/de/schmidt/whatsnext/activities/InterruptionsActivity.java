package de.schmidt.whatsnext.activities;

import android.content.res.ColorStateList;
import android.widget.ListView;
import android.os.Bundle;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import de.schmidt.mvg.interrupt.Interruption;
import de.schmidt.util.ColorUtils;
import de.schmidt.util.managers.NavBarManager;
import de.schmidt.util.caching.InterruptionsCache;
import de.schmidt.util.network.InterruptionsNetworkAccess;
import de.schmidt.whatsnext.adapters.InterruptionsListViewAdapter;
import de.schmidt.whatsnext.R;
import de.schmidt.whatsnext.base.ActionBarBaseActivity;
import de.schmidt.whatsnext.base.Updatable;

import java.util.ArrayList;
import java.util.List;

public class InterruptionsActivity extends ActionBarBaseActivity implements Updatable<Interruption> {

	private SwipeRefreshLayout swipeRefresh;
	private ListView listView;
	private List<Interruption> interruptions;
	private InterruptionsListViewAdapter adapter;
	private BottomNavigationView navBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_interruptions);

		interruptions = new ArrayList<>();

		setTitle(getString(R.string.interruptions_title));

		navBar = findViewById(R.id.bottom_nav_bar_inter);
		NavBarManager.getInstance().initialize(navBar, this);

		swipeRefresh = findViewById(R.id.pull_to_refresh_interruptions);
		swipeRefresh.setColorSchemeColors(ColorUtils.getSpriteColors(this));
		swipeRefresh.setOnRefreshListener(() -> {
			refresh();
			swipeRefresh.setRefreshing(false);
		});

		listView = findViewById(R.id.interruption_list);
		adapter = new InterruptionsListViewAdapter(this, interruptions);
		listView.setAdapter(adapter);
		listView.setClickable(false);
	}

	@Override
	public void refresh() {
		swipeRefresh.setRefreshing(true);
		new InterruptionsNetworkAccess(this).execute();
	}

	@Override
	public void handleUIUpdate(List<Interruption> interruptions) {
		if (interruptions == null) return;

		runOnUiThread(() -> {
			//copy data to field
			InterruptionsActivity.this.interruptions.clear();
			InterruptionsActivity.this.interruptions.addAll(interruptions);

			//set colors accordingly
			navBar.setBackgroundColor(getColor(R.color.white));
			navBar.setItemIconTintList(ColorStateList.valueOf(getColor(R.color.colorPrimaryDark)));
			navBar.setItemTextColor(ColorStateList.valueOf(getColor(R.color.colorPrimaryDark)));

			//refresh the list view
			adapter.notifyDataSetChanged();
			listView.invalidateViews();
			listView.refreshDrawableState();
			swipeRefresh.setRefreshing(false);
		});
	}

	@Override
	public int getNavButtonItemId() {
		return R.id.nav_inter_button;
	}

	@Override
	public BottomNavigationView getNavBar() {
		return navBar;
	}

	@Override
	public void updateFromCache() {
		handleUIUpdate(InterruptionsCache.getInstance().getCache());
	}
}
