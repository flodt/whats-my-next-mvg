package de.schmidt.util.managers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import de.schmidt.whatsnext.activities.StationSelectionActivity;

public class FabManager {
	private static final FabManager instance = new FabManager();
	private static final String TAG = "FabManager";

	private FabManager() {

	}

	public static FabManager getInstance() {
		return instance;
	}

	/**
	 * Initialize the passed fab to update station selection.
	 * @param button the fab object to initialize
	 * @param context the context to initialize in
	 */
	public void initializeForStationSelection(FloatingActionButton button, Context context) {
		button.setOnClickListener(v -> context.startActivity(new Intent(context, StationSelectionActivity.class)));
	}

	/**
	 * Initialize the passed fab to update the interruptions filter.
	 * @param button the fab object to initialize
	 * @param context the context to initialize in
	 */
	public void initializeForInterruptionsFilter(FloatingActionButton button, Context context) {
		button.setOnClickListener(v -> PreferenceManager.getInstance().updateInterruptionsFilter(context));
	}

	/**
	 * Initialize the passed fab to add a station to the list.
	 * @param button the fab object to initialize
	 * @param context the context to initialize in
	 */
	public void initializeForStationAddition(FloatingActionButton button, Activity context) {
		button.setOnClickListener(v -> PreferenceManager.getInstance().getUserInputForStationAddition(context));
	}
}
