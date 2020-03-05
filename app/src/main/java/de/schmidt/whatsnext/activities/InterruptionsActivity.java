package de.schmidt.whatsnext.activities;

import android.content.Intent;
import android.widget.ListView;
import android.os.Bundle;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import de.schmidt.mvg.Interruption;
import de.schmidt.util.ColorUtils;
import de.schmidt.util.network.InterruptionsNetworkAccess;
import de.schmidt.whatsnext.adapters.InterruptionsListViewAdapter;
import de.schmidt.whatsnext.R;

import java.util.ArrayList;
import java.util.List;

public class InterruptionsActivity extends ActionBarBaseActivity {

	private SwipeRefreshLayout swipeRefresh;
	private ListView listView;
	private List<Interruption> interruptions;
	private InterruptionsListViewAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_interruptions);

		interruptions = new ArrayList<>();

		swipeRefresh = findViewById(R.id.pull_to_refresh_interruptions);
		swipeRefresh.setColorSchemeColors(ColorUtils.getSpriteColors(this));
		swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				refresh();
				swipeRefresh.setRefreshing(false);
			}
		});

		listView = findViewById(R.id.interruption_list);
		adapter = new InterruptionsListViewAdapter(this, interruptions);
		listView.setAdapter(adapter);
		listView.setClickable(false);
	}

	@Override
	protected void onResume() {
		super.onResume();
		refresh();
	}

	@Override
	public void refresh() {
		swipeRefresh.setRefreshing(true);
		new InterruptionsNetworkAccess(this).execute();
	}

	@Override
	public void switchActivity() {
		Intent intent = new Intent(this, DepartureListActivity.class);
		startActivity(intent);
	}

	public void handleUIUpdate(List<Interruption> interruptions) {
		if (interruptions == null) return;

		runOnUiThread(() -> {
			InterruptionsActivity.this.interruptions.clear();
			InterruptionsActivity.this.interruptions.addAll(interruptions);

			//refresh the list view
			adapter.notifyDataSetChanged();
			listView.invalidateViews();
			listView.refreshDrawableState();
			swipeRefresh.setRefreshing(false);
		});
	}
}
