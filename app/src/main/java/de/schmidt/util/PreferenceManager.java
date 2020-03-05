package de.schmidt.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.text.InputType;
import android.widget.EditText;
import de.schmidt.whatsnext.ActionBarBaseActivity;
import de.schmidt.whatsnext.R;

import java.util.HashSet;
import java.util.Set;

public class PreferenceManager {
	public static final String PREFERENCE_KEY = "WhatsMyNext";
	private static final PreferenceManager instance = new PreferenceManager();

	private PreferenceManager() {

	}

	public static PreferenceManager getInstance() {
		return instance;
	}

	public void updateExclusions(Context context) {
		//0: bus, 1: u, 2: s, 3: tram, 4: bahn
		String[] keys = context.getResources().getStringArray(R.array.transport_keys);
		String[] readable = context.getResources().getStringArray(R.array.transport_means_readable);
		boolean[] selected = new boolean[keys.length];

		//read preferences
		SharedPreferences prefs = context.getSharedPreferences(PREFERENCE_KEY, Context.MODE_PRIVATE);
		for (int i = 0; i < selected.length; i++) {
			selected[i] = prefs.getBoolean(keys[i], true);
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle("Select means of transport…");
		builder.setIcon(R.drawable.ic_excluded_black);
		builder.setMultiChoiceItems(readable, selected, new DialogInterface.OnMultiChoiceClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which, boolean isChecked) {
				selected[which] = isChecked;
			}
		});
		builder.setCancelable(true);
		builder.setPositiveButton(context.getResources().getString(R.string.save_settings), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//save to preferences
				SharedPreferences.Editor editor = prefs.edit();
				for (int i = 0; i < keys.length; i++) {
					editor.putBoolean(keys[i], selected[i]);
				}
				editor.apply();
				refresh(context);
			}
		});
		builder.setNegativeButton(context.getResources().getString(R.string.dismiss_settings), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				refresh(context);
			}
		});

		AlertDialog dialog = builder.create();
		dialog.show();
	}

	public Set<String> getExcludableTransportMeans(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(PREFERENCE_KEY, Context.MODE_PRIVATE);
		Set<String> exclusions = new HashSet<>();

		String[] keys = context.getResources().getStringArray(R.array.transport_keys);
		for (String key : keys) {
			boolean included = prefs.getBoolean(key, true);
			if (!included) exclusions.add(key);
		}

		return exclusions;
	}

	public void updateStationSelection(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(PREFERENCE_KEY, Context.MODE_PRIVATE);

		String[] keys = context.getResources().getStringArray(R.array.station_keys);
		String[] readable = context.getResources().getStringArray(R.array.station_readable);
		int checked = prefs.getInt(context.getResources().getString(R.string.selection_station_in_menu), 1);

		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(R.string.select_station_title);
		builder.setIcon(R.drawable.ic_station_selection_black);
		builder.setSingleChoiceItems(readable, checked, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				prefs.edit().putInt(context.getResources().getString(R.string.selection_station_in_menu), which).apply();

				//handle custom name here
				if (keys[which].equals("BY_NAME")) {
					getUserInputForCustomStationName(dialog, context);
					return;
				}

				dialog.cancel();
				refresh(context);
			}
		});
		builder.setNeutralButton(R.string.dismiss_settings, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				refresh(context);
			}
		});

		AlertDialog dialog = builder.create();
		dialog.show();
	}

	public void getUserInputForCustomStationName(DialogInterface parent, Context context) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(context.getResources().getString(R.string.custom_station_name_title));

		EditText input = new EditText(context);
		input.setInputType(InputType.TYPE_CLASS_TEXT);
		builder.setView(input);

		builder.setPositiveButton(R.string.save_settings, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String userInput = input.getText().toString().trim();
				setCustomNameFieldInContext(userInput, context);
				context.getSharedPreferences(PREFERENCE_KEY, Context.MODE_PRIVATE)
						.edit()
						.putString(
								context.getResources().getString(R.string.selection_custom_station_entry),
								userInput
						)
						.apply();

				parent.cancel();
				dialog.cancel();
				refresh(context);
			}
		});

		builder.setCancelable(false);

		AlertDialog dialog = builder.create();
		dialog.show();
	}

	private void setCustomNameFieldInContext(String userInput, Context context) {
		if (context instanceof ActionBarBaseActivity) {
			((ActionBarBaseActivity) context).setCustomName(userInput);
		}
	}

	private void refresh(Context context) {
		if (context instanceof ActionBarBaseActivity) {
			((ActionBarBaseActivity) context).refresh();
		}
	}
}
