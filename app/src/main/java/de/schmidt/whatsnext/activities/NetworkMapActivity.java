package de.schmidt.whatsnext.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.util.Log;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import android.os.Bundle;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import de.schmidt.mvg.Requests;
import de.schmidt.util.managers.NavBarManager;
import de.schmidt.util.ThemeUtils;
import de.schmidt.whatsnext.R;
import de.schmidt.whatsnext.base.ActionBarBaseActivity;
import de.schmidt.whatsnext.base.Shortcutable;

import java.util.Objects;

public class NetworkMapActivity extends ActionBarBaseActivity implements Shortcutable {
	private static final String TAG = "NetworkMapActivity";
	private WebView webView;
	private BottomNavigationView navBar;
	private ActionBar actionBar;

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_network_map);

		setTitle(R.string.network_map_title);

		actionBar = Objects.requireNonNull(getSupportActionBar());
		actionBar.setDisplayHomeAsUpEnabled(true);

		navBar = findViewById(R.id.bottom_nav_bar_network_map);
		NavBarManager.getInstance().initialize(navBar, this);

		webView = findViewById(R.id.network_map_webview);
		webView.setWebViewClient(new WebViewClient());
		webView.getSettings().setSupportZoom(true);
		webView.getSettings().setJavaScriptEnabled(true);
	}

	@Override
	protected void onResume() {
		//set colors
		ThemeUtils.getInstance().initializeActionBar(this, actionBar, getWindow());
		ThemeUtils.getInstance().initializeNavBarWithAccentResource(this, navBar, R.color.mvg_1);

		super.onResume();
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
		//load the network map
		final String googleViewer = "https://docs.google.com/gview?embedded=true&url=";
		final String url = googleViewer + Requests.URL_NETWORK_MAP;
		Log.d(TAG, "refresh: loading " + url);
		webView.loadUrl(url);
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
		Intent launchIntent = new Intent(getApplicationContext(), NetworkMapActivity.class);
		launchIntent.setAction(Intent.ACTION_MAIN);

		final String label = getString(R.string.network_map_title);
		final @DrawableRes int icon = R.mipmap.ic_subway_map_shortcut_round;

		//request shortcut in launcher
		Shortcutable.requestShortcut(this, launchIntent, label, icon);
	}
}
