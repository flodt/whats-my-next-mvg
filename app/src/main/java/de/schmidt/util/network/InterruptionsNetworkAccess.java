package de.schmidt.util.network;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import de.schmidt.mvg.interrupt.Interruption;
import de.schmidt.mvg.Requests;
import de.schmidt.mvg.interrupt.InterruptionLine;
import de.schmidt.util.caching.InterruptionsCache;
import de.schmidt.util.managers.PreferenceManager;
import de.schmidt.whatsnext.R;
import de.schmidt.whatsnext.activities.InterruptionsActivity;
import org.json.JSONException;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class InterruptionsNetworkAccess extends AsyncTask<Void, Void, List<Interruption>> {
	private static final String TAG = "InterruptionsNetworkAccess";
	private final WeakReference<InterruptionsActivity> act;

	public InterruptionsNetworkAccess(InterruptionsActivity act) {
		this.act = new WeakReference<>(act);
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}

	@Override
	protected void onPostExecute(List<Interruption> interruptions) {
		//set cache, trigger UI update
		super.onPostExecute(interruptions);
		InterruptionsCache.getInstance().setCache(interruptions);
		act.get().handleUIUpdate(interruptions);
	}

	@Override
	protected List<Interruption> doInBackground(Void... voids) {
		//handle network request here
		Requests requests = Requests.instance();
		List<Interruption> result = Collections.emptyList();

		try {
			result = requests.getInterruptions();
		} catch (JSONException e) {
			Log.e(TAG, "doInBackground: JSON error retrieving the interruptions", e);
		}

		//get filter for interruptions from the preferences
		Set<String> filter = Arrays.stream(
				act.get()
						.getSharedPreferences(PreferenceManager.PREFERENCE_KEY, Context.MODE_PRIVATE)
						.getString(act.get().getResources().getString(R.string.pref_key_inter_filter), "")
						.split(","))
				.map(String::trim)
				.map(String::toUpperCase)
				.filter(s -> s.length() != 0)
				.collect(Collectors.toSet());

		Log.d(TAG, "doInBackground: filter set contents: " + filter + " (size " + filter.size() + ")");

		if (filter.isEmpty()) {
			//if the filter set is empty, return all interruptions
			Log.d(TAG, "doInBackground: filter empty, returning all " + result);
			return result;
		} else {
			//return the results filtered according to the preferences
			return result.stream()
					.filter(i -> i.getLines().stream().map(InterruptionLine::getLine).anyMatch(filter::contains))
					.collect(Collectors.toList());
		}
	}
}
