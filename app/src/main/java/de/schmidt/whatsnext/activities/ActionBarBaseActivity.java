package de.schmidt.whatsnext.activities;

import android.view.Menu;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import de.schmidt.util.MenuManager;

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

	public abstract void switchActivity();

	public void setCustomName(String customName) {
		//default intentionally left blank
	}

	public abstract void refresh();
}
