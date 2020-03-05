package de.schmidt.whatsnext.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import de.schmidt.mvg.Departure;
import de.schmidt.util.MenuManager;
import de.schmidt.util.network.DepartureCache;
import de.schmidt.whatsnext.R;

import java.util.Collections;
import java.util.List;

public abstract class ActionBarBaseActivity extends AppCompatActivity {
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
		handleUIUpdate(DepartureCache.getInstance().getCache());
		refresh();
	}

	public abstract void switchActivity();

	public void setCustomName(String customName) {
		//default intentionally left blank
	}

	public abstract void refresh();

	public abstract int getNavButtonItemId();
	public abstract BottomNavigationView getNavBar();
	public abstract void handleUIUpdate(List<Departure> dataSet);
}
