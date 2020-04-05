package de.schmidt.whatsnext.activities;

import android.content.Intent;
import android.widget.ListView;
import android.os.Bundle;
import androidx.annotation.DrawableRes;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import de.schmidt.mvg.interrupt.Interruption;
import de.schmidt.util.ColorUtils;
import de.schmidt.util.managers.FabManager;
import de.schmidt.util.managers.NavBarManager;
import de.schmidt.util.caching.InterruptionsCache;
import de.schmidt.util.managers.ThemeManager;
import de.schmidt.util.network.InterruptionsNetworkAccess;
import de.schmidt.whatsnext.adapters.InterruptionsListViewAdapter;
import de.schmidt.whatsnext.R;
import de.schmidt.whatsnext.base.ActionBarBaseActivity;
import de.schmidt.whatsnext.base.Shortcutable;
import de.schmidt.whatsnext.base.Updatable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class InterruptionsActivity extends ActionBarBaseActivity implements Updatable<Interruption>, Shortcutable {
	private SwipeRefreshLayout swipeRefresh;
	private ListView listView;
	private List<Interruption> interruptions;
	private InterruptionsListViewAdapter adapter;
	private BottomNavigationView navBar;
	private FloatingActionButton fab;

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

		fab = findViewById(R.id.fab_interruptions_filter);
		FabManager.getInstance().initializeForInterruptionsFilter(fab, this);

		listView = findViewById(R.id.interruption_list);
		adapter = new InterruptionsListViewAdapter(this, interruptions);
		listView.setAdapter(adapter);
		listView.setClickable(false);
	}

	@Override
	protected void onResume() {
		super.onResume();
		//set colors
		ThemeManager.getInstance().initializeActionBarWithColorResource(this,
																		Objects.requireNonNull(getSupportActionBar()),
																		getWindow(),
																		R.color.mvg_3);
		ThemeManager.getInstance().initializeNavBarWithAccentResource(this, navBar, R.color.mvg_3);
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

	@Override
	public void createShortcut() {
		//build the intent that's called on tap
		Intent launchIntent = new Intent(getApplicationContext(), InterruptionsActivity.class);
		launchIntent.setAction(Intent.ACTION_MAIN);

		final String label = getString(R.string.interruptions_title);
		final @DrawableRes int icon = R.mipmap.ic_interruptions_shortcut_round;

		//request shortcut in launcher
		Shortcutable.requestShortcut(this, launchIntent, label, icon);
	}
}
