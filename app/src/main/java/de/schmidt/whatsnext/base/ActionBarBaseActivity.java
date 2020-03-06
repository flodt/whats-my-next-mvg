package de.schmidt.whatsnext.base;

import android.view.Menu;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import de.schmidt.util.managers.MenuManager;

public abstract class ActionBarBaseActivity extends AppCompatActivity implements Cacheable {
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuManager.getInstance().inflate(menu, this);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		MenuManager.getInstance().dispatch(item, this);
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onResume() {
		super.onResume();
		getNavBar().setSelectedItemId(getNavButtonItemId());
		updateFromCache();
		refresh();
	}

	public void setCustomName(String customName) {
		//default intentionally left blank
	}

	public abstract void refresh();
	public abstract int getNavButtonItemId();
	public abstract BottomNavigationView getNavBar();
}
