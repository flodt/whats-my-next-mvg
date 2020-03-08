package de.schmidt.util.managers;

import android.content.Context;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

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
		button.setOnClickListener(v -> PreferenceManager.getInstance().updateStationSelection(context));
	}

	/**
	 * Initialize the passed fab to update the interruptions filter.
	 * @param button the fab object to initialize
	 * @param context the context to initialize in
	 */
	public void initializeForInterruptionsFilter(FloatingActionButton button, Context context) {
		button.setOnClickListener(v -> PreferenceManager.getInstance().updateInterruptionsFilter(context));
	}
}
