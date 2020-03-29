package de.schmidt.whatsnext.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import de.schmidt.whatsnext.viewsupport.list.RouteStationSelection;
import de.schmidt.whatsnext.R;

import java.util.List;

public class RecentsListViewAdapter extends BaseAdapter {
	private final Activity context;
	private final List<RouteStationSelection> recents;

	public RecentsListViewAdapter(Activity context, List<RouteStationSelection> recents) {
		this.context = context;
		this.recents = recents;
	}

	@Override
	public int getCount() {
		return recents.size();
	}

	@Override
	public Object getItem(int position) {
		return recents.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@SuppressLint({"ViewHolder", "SetTextI18n"})
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		convertView = LayoutInflater.from(context).inflate(R.layout.recents_list_item, null);

		TextView from = convertView.findViewById(R.id.recents_from_station);
		TextView to = convertView.findViewById(R.id.recents_to_station);

		RouteStationSelection element = recents.get(position);
		from.setText(element.getStart().getName());
		to.setText(element.getDestination().getName());

		return convertView;
	}
}
