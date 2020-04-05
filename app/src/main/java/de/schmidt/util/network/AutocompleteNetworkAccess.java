package de.schmidt.util.network;

import android.os.AsyncTask;
import de.schmidt.mvg.Requests;
import de.schmidt.whatsnext.adapters.AutocompleteSuggestAdapter;

import java.util.List;

public class AutocompleteNetworkAccess extends AsyncTask<Void, Void, List<String>> {
	private final String input;
	private final AutocompleteSuggestAdapter adapter;

	public AutocompleteNetworkAccess(String input, AutocompleteSuggestAdapter adapter) {
		this.input = input;
		this.adapter = adapter;
	}

	@Override
	protected List<String> doInBackground(Void... voids) {
		return Requests.instance().getAutocompleteSuggestionsForInput(input, 5);
	}

	@Override
	protected void onPostExecute(List<String> strings) {
		super.onPostExecute(strings);

		//if the result is not empty, set it as the suggestion data
		if (!strings.isEmpty()) {
			adapter.setData(strings);
			adapter.notifyDataSetChanged();
		}
	}
}
