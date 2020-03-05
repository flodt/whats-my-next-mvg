package de.schmidt.whatsnext.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import de.schmidt.util.MenuManager;
import de.schmidt.whatsnext.R;

public abstract class ActionBarBaseActivity extends AppCompatActivity {
	protected BottomNavigationView navBar;

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
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_departure_list);

		// TODO: 05.03.20 Move this initialisation to the subclasses. Then call a navbar manager method that sets the listener once.
		navBar = findViewById(R.id.bottom_nav_bar);
		navBar.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
			@Override
			public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
				Log.d("Base", "onNavigationItemSelected: menuItem selected");
				Intent intent;

				switch (menuItem.getItemId()) {
					case R.id.nav_single_button:
						intent = new Intent(ActionBarBaseActivity.this, SingleDepartureActivity.class);
						break;
					case R.id.nav_list_button:
						intent = new Intent(ActionBarBaseActivity.this, DepartureListActivity.class);
						break;
					case R.id.nav_inter_button:
						intent = new Intent(ActionBarBaseActivity.this, InterruptionsActivity.class);
						break;
					default:
						return false;
				}

				startActivity(intent);
				return false;
			}
		});
	}

	public abstract void switchActivity();

	public void setCustomName(String customName) {
		//default intentionally left blank
	}

	public abstract void refresh();

	public abstract int getNavButtonItemId();
}
