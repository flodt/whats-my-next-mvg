package de.schmidt.whatsnext.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import androidx.annotation.NonNull;
import android.os.Bundle;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import de.schmidt.mvg.adapters.SwitchStationListItem;
import de.schmidt.util.ColorUtils;
import de.schmidt.util.managers.NavBarManager;
import de.schmidt.util.managers.PreferenceManager;
import de.schmidt.whatsnext.R;
import de.schmidt.whatsnext.adapters.StationSelectionListViewAdapter;
import de.schmidt.whatsnext.base.ActionBarBaseActivity;
import de.schmidt.whatsnext.base.Updatable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class StationSelectionActivity extends ActionBarBaseActivity implements Updatable<SwitchStationListItem> {
	private BottomNavigationView navBar;
	private List<SwitchStationListItem> stations;
	private SwipeRefreshLayout swipeRefresh;
	private ListView listView;
	private StationSelectionListViewAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_station_selection);

		//set back button
		Objects.requireNonNull(getActionBar()).setDisplayHomeAsUpEnabled(true);

		//initialize nav bar
		navBar = findViewById(R.id.bottom_nav_bar_stations);
		NavBarManager.getInstance().initialize(navBar, this);

		//set the window title
		setTitle(getResources().getString(R.string.select_station_title));

		//setup list view
		stations = new ArrayList<>();
		listView = findViewById(R.id.stations_list);
		adapter = new StationSelectionListViewAdapter(this, stations);
		listView.setAdapter(adapter);
		listView.setClickable(true);
		listView.setOnItemClickListener((parent, view, position, id) -> {
			//set the current selected item in preferences
			PreferenceManager.getInstance().setSelectedStation(StationSelectionActivity.this, position);

			//refresh the view (so as to reflect the bold selection)
			refresh();

			//launch departure list activity
			startActivity(new Intent(StationSelectionActivity.this, DepartureListActivity.class));
		});
		listView.setLongClickable(true);
		listView.setOnItemLongClickListener((parent, view, position, id) -> {
			final SwitchStationListItem clicked = stations.get(position);

			//don't allow long click on current location
			if (clicked.isCurrentLocation()) return false;

			//delete element after asking for confirmation
			//ask for confirmation
			new AlertDialog.Builder(StationSelectionActivity.this)
					.setTitle(getResources().getString(R.string.remove_station_title))
					.setMessage(getResources().getString(R.string.remove_station_message))
					.setIcon(R.drawable.ic_warning)
					.setNegativeButton(getResources().getString(R.string.cancel_dialog), null)
					.setPositiveButton(getResources().getString(R.string.yes_dialog), (dialog, which) -> {
						//remove the element from the list
						PreferenceManager.getInstance().removeFromStationList(StationSelectionActivity.this, clicked);
						dialog.dismiss();
						refresh();
					})
					.create()
					.show();

			return true;
		});

		//initialize swipe to refresh
		swipeRefresh = findViewById(R.id.pull_to_refresh_stations);
		swipeRefresh.setColorSchemeColors(ColorUtils.getSpriteColors(this));
		swipeRefresh.setOnRefreshListener(() -> {
			refresh();
			swipeRefresh.setRefreshing(false);
		});
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		//handle back button in action bar as if user pressed back on nav bar
		if (item.getItemId() == android.R.id.home) {
			onBackPressed();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void refresh() {
		//grab the newest data from preferences, refresh UI with that
		swipeRefresh.setRefreshing(true);
		final List<SwitchStationListItem> read = PreferenceManager.getInstance().getStationList(this);
		handleUIUpdate(read);
	}

	@Override
	public void handleUIUpdate(List<SwitchStationListItem> dataSet) {
		if (dataSet == null) return;

		//copy data to field
		this.stations.clear();
		this.stations.addAll(dataSet);

		runOnUiThread(() -> {
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
