package de.schmidt.util.managers;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.text.InputType;
import android.widget.EditText;
import de.schmidt.mvg.adapters.RouteStationSelection;
import de.schmidt.mvg.adapters.SwitchStationListItem;
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
		builder.setTitle(context.getResources().getString(R.string.dialog_transport_means_title));
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
		input.setHint(context.getResources().getString(R.string.interrupt_filter_hint));

		//build the dialog
		new AlertDialog.Builder(context)
				.setTitle(context.getResources().getString(R.string.interrupt_filter_title))
				.setMessage(context.getResources().getString(R.string.interrupt_filter_descr))
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

	public List<SwitchStationListItem> getStationList(Context context) {
		//get the raw data from shared preferences and unwrap
		SharedPreferences prefs = context.getSharedPreferences(PREFERENCE_KEY, Context.MODE_PRIVATE);
		final String defaultListValue = "loc";
		String raw = prefs.getString(context.getString(R.string.pref_key_stations), defaultListValue);

		return Arrays.stream(raw.split("\\$"))
				.map(SwitchStationListItem::unwrapFromString)
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
	}

	public void setStationList(Context context, List<SwitchStationListItem> stations) {
		//save the list to shared preferences
		context.getSharedPreferences(PREFERENCE_KEY, Context.MODE_PRIVATE)
				.edit()
				.putString(
						context.getString(R.string.pref_key_stations),
						stations.stream()
								.map(SwitchStationListItem::wrapToString)
								.collect(Collectors.joining("$"))
				)
				.apply();
	}

	public void addToStationList(Context context, SwitchStationListItem item) {
		//read, add, save back to preferences
		final List<SwitchStationListItem> list = getStationList(context);
		list.add(item);
		setStationList(context, list);
	}

	public void removeFromStationList(Context context, SwitchStationListItem item) {
		//read, remove, save back to preferences
		final List<SwitchStationListItem> list = getStationList(context);
		list.remove(item);
		setStationList(context, list);
	}

	public SwitchStationListItem getSelectedStation(Context context) {
		//get the selected station index from preferences, return that element
		final int selectedIndex = context
				.getSharedPreferences(PREFERENCE_KEY, Context.MODE_PRIVATE)
				.getInt(context.getString(R.string.pref_key_selected_station_in_list), 0);

		return getStationList(context).get(selectedIndex);
	}

	public void setSelectedStation(Context context, SwitchStationListItem item) {
		//get the index of our selection
		final int index = getStationList(context).indexOf(item);
		setSelectedStation(context, index);
	}

	public boolean isCurrentlySelected(Context context, SwitchStationListItem item) {
		return Objects.equals(getSelectedStation(context), item);
	}

	public void setSelectedStation(Context context, int index) {
		//save that index to shared preferences
		context.getSharedPreferences(PREFERENCE_KEY, Context.MODE_PRIVATE)
				.edit()
				.putInt(context.getString(R.string.pref_key_selected_station_in_list), index)
				.apply();
	}

	public void setRecents(Context context, List<RouteStationSelection> selections) {
		//limit list size to 10 elements
		selections = selections.stream()
				.limit(10)
				.collect(Collectors.toList());

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
		recents.remove(selection);
		recents.add(0, selection);
		setRecents(context, recents);
	}

	public void removeFromRecents(Context context, RouteStationSelection selection) {
		List<RouteStationSelection> recents = getRecents(context);
		recents.remove(selection);
		setRecents(context, recents);
	}

	public void clearRecents(Context context) {
		setRecents(context, Collections.emptyList());
	}

	private void refresh(Context context) {
		//manually call refresh on the activity
		if (context instanceof ActionBarBaseActivity) {
			((ActionBarBaseActivity) context).refresh();
		}
	}
}
