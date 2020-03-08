package de.schmidt.util.network;

import android.os.AsyncTask;
import android.util.Log;
import de.schmidt.mvg.interrupt.Interruption;
import de.schmidt.mvg.Requests;
import de.schmidt.util.caching.InterruptionsCache;
import de.schmidt.whatsnext.activities.InterruptionsActivity;
import org.json.JSONException;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;

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

		return result;
	}
}
