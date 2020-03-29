package de.schmidt.whatsnext.activities;

import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import de.schmidt.mvg.Requests;
import de.schmidt.util.ColorUtils;
import de.schmidt.util.managers.NavBarManager;
import de.schmidt.whatsnext.R;
import de.schmidt.whatsnext.base.ActionBarBaseActivity;

import java.util.Objects;

public class NetworkMapActivity extends ActionBarBaseActivity {
	private WebView webView;
	private BottomNavigationView navBar;
	private ActionBar actionBar;


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
	}

	@Override
	protected void onResume() {
		//set colors
		int[] primaryAndDark = ColorUtils.extractPrimaryAndDark(getColor(R.color.mvg_1));
		actionBar.setBackgroundDrawable(new ColorDrawable(primaryAndDark[0]));
		getWindow().setStatusBarColor(primaryAndDark[1]);

		navBar.setBackgroundColor(getColor(R.color.white));
		navBar.setItemIconTintList(ColorStateList.valueOf(getColor(R.color.mvg_1)));
		navBar.setItemTextColor(ColorStateList.valueOf(getColor(R.color.mvg_1)));

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
		final String googleURL = "https://docs.google.com/gview?embedded=true&url=";
		webView.loadUrl(googleURL + Requests.URL_NETWORK_MAP);
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