package de.schmidt.whatsnext.adapters;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class AutocompleteSuggestAdapter extends ArrayAdapter<String> implements Filterable {
	private List<String> data;

	public AutocompleteSuggestAdapter(@NonNull Context context, int resource) {
		super(context, resource);
		data = new ArrayList<>();
	}

	public void setData(List<String> newData) {
		data.clear();
		data.addAll(newData);
	}

	@Override
	public int getCount() {
		return data.size();
	}

	@Nullable
	@Override
	public String getItem(int position) {
		return data.get(position);
	}

	@NonNull
	@Override
	public Filter getFilter() {
		return new Filter() {
			@Override
			protected FilterResults performFiltering(CharSequence constraint) {
				//apply filters here
				FilterResults results = new FilterResults();
				if (constraint != null) {
					results.values = data;
					results.count = data.size();
				}
				return results;
			}

			@Override
			protected void publishResults(CharSequence constraint, FilterResults results) {
				if (results != null && results.count > 0) {
					notifyDataSetChanged();
				} else {
					notifyDataSetInvalidated();
				}
			}
		};
	}
}
