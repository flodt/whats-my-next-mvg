package de.schmidt.util.managers;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.text.InputType;
import android.widget.EditText;
import de.schmidt.mvg.route.RouteStationSelection;
import de.schmidt.whatsnext.base.ActionBarBaseActivity;
import de.schmidt.whatsnext.R;

import java.util.*;
import java.util.stream.Collectors;

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

		//read preferences for selections
		SharedPreferences prefs = context.getSharedPreferences(PREFERENCE_KEY, Context.MODE_PRIVATE);
		for (int i = 0; i < selected.length; i++) {
			selected[i] = prefs.getBoolean(keys[i], true);
		}

		//ask for exclusions
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle("Select means of transport…");
		builder.setIcon(R.drawable.ic_excluded_black);
		builder.setMultiChoiceItems(readable, selected, (dialog, which, isChecked) -> selected[which] = isChecked);
		builder.setCancelable(true);
		builder.setPositiveButton(context.getResources().getString(R.string.save_settings), (dialog, which) -> {
			//save to preferences
			SharedPreferences.Editor editor = prefs.edit();
			for (int i = 0; i < keys.length; i++) {
				editor.putBoolean(keys[i], selected[i]);
			}
			editor.apply();
			refresh(context);
		});
		builder.setNegativeButton(context.getResources().getString(R.string.dismiss_settings), (dialog, which) -> {
			//dismiss dialog
			dialog.dismiss();
			refresh(context);
		});

		AlertDialog dialog = builder.create();
		dialog.show();
	}

	public Set<String> getExcludableTransportMeans(Context context) {
		//read exclusions from preferences as string of means of transport
		SharedPreferences prefs = context.getSharedPreferences(PREFERENCE_KEY, Context.MODE_PRIVATE);
		Set<String> exclusions = new HashSet<>();

		String[] keys = context.getResources().getStringArray(R.array.transport_keys);
		for (String key : keys) {
			boolean included = prefs.getBoolean(key, true);
			if (!included) exclusions.add(key);
		}

		return exclusions;
	}

	public void updateInterruptionsFilter(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(PREFERENCE_KEY, Context.MODE_PRIVATE);

		//read InterruptionsFilter from prefs
		String filter = prefs.getString(context.getResources().getString(R.string.pref_key_inter_filter), "");

		//EditText for user input
		EditText input = new EditText(context);
		input.setText(filter);
		input.setHint("comma-separated lines…");

		//build the dialog
		new AlertDialog.Builder(context)
				.setTitle("Interruptions filter…")
				.setMessage("Enter lines for interruptions…")
				.setIcon(R.drawable.ic_interruptions_black)
				.setView(input)
				.setPositiveButton(R.string.save_settings, (dialog, which) -> {
					String entry = input.getText().toString();

					//cleanup string
					String clean = Arrays.stream(entry.split(","))
							.map(String::trim)
							.filter(str -> str.length() != 0)
							.distinct()
							.collect(Collectors.joining(", "));

					//save to preferences
					prefs.edit()
							.putString(context.getResources().getString(R.string.pref_key_inter_filter), clean)
							.apply();

					dialog.dismiss();
					refresh(context);
				})
				.setCancelable(true)
				.setNegativeButton(R.string.dismiss_settings, null)
				.setNeutralButton(R.string.reset, (dialog, which) -> {
					input.setText("");
					prefs.edit()
							.putString(context.getResources().getString(R.string.pref_key_inter_filter), "")
							.apply();
					dialog.dismiss();
					refresh(context);
				})
				.create()
				.show();
	}

	public void updateStationSelection(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(PREFERENCE_KEY, Context.MODE_PRIVATE);

		//read selected station from prefs
		String[] keys = context.getResources().getStringArray(R.array.station_keys);
		String[] readable = context.getResources().getStringArray(R.array.station_readable);
		int checked = prefs.getInt(context.getResources().getString(R.string.selection_station_in_menu), 1);

		//show dialog for selection
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(R.string.select_station_title);
		builder.setIcon(R.drawable.ic_station_selection_black);
		builder.setSingleChoiceItems(readable, checked, (dialog, which) -> {
			//save selection to preferences, this is read by the other activities
			prefs.edit().putInt(context.getResources().getString(R.string.selection_station_in_menu), which).apply();

			//handle custom name here
			if (keys[which].equals("BY_NAME")) {
				getUserInputForCustomStationName(dialog, context);
				return;
			}

			dialog.cancel();
			refresh(context);
		});
		builder.setNeutralButton(R.string.dismiss_settings, (dialog, which) -> {
			dialog.dismiss();
			refresh(context);
		});

		AlertDialog dialog = builder.create();
		dialog.show();
	}

	public void getUserInputForCustomStationName(DialogInterface parent, Context context) {
		//input dialog for custom station name
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(context.getResources().getString(R.string.custom_station_name_title));

		EditText input = new EditText(context);
		input.setInputType(InputType.TYPE_CLASS_TEXT);
		builder.setView(input);

		builder.setPositiveButton(R.string.save_settings, (dialog, which) -> {
			String userInput = input.getText().toString().trim();
			setCustomNameFieldInContext(userInput, context);

			//save custom input to preferences (this is read by the other activities)
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
		});

		builder.setCancelable(false);

		AlertDialog dialog = builder.create();
		dialog.show();
	}

	public void setRecents(Context context, List<RouteStationSelection> selections) {
		//parse recents to Strings and save to preferences
		context.getSharedPreferences(PREFERENCE_KEY, Context.MODE_PRIVATE)
				.edit()
				.putString(
						context.getResources().getString(R.string.pref_key_recents),
						selections.stream()
								.map(RouteStationSelection::wrapToString)
								.collect(Collectors.joining("$"))
				)
				.apply();
	}

	public List<RouteStationSelection> getRecents(Context context) {
		String rawRecents = context.getSharedPreferences(PREFERENCE_KEY, Context.MODE_PRIVATE)
				.getString(context.getResources().getString(R.string.pref_key_recents), "");

		//unwrap strings
		return Arrays.stream(rawRecents.split("\\$"))
				.map(RouteStationSelection::unwrapFromString)
				.filter(Objects::nonNull)
				.collect(Collectors.toCollection(ArrayList::new));
	}

	public void addToRecents(Context context, RouteStationSelection selection) {
		List<RouteStationSelection> recents = getRecents(context);
		recents.add(0, selection);
		setRecents(context, recents);
	}

	public void clearRecents(Context context) {
		setRecents(context, Collections.emptyList());
	}

	private void setCustomNameFieldInContext(String userInput, Context context) {
		//sets the custom name field for the activities that need it
		//this is used in the network access to avoid reading the preferences again
		if (context instanceof ActionBarBaseActivity) {
			((ActionBarBaseActivity) context).setCustomName(userInput);
		}
	}

	private void refresh(Context context) {
		//manually call refresh on the activity
		if (context instanceof ActionBarBaseActivity) {
			((ActionBarBaseActivity) context).refresh();
		}
	}
}
