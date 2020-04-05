package de.schmidt.whatsnext.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import de.schmidt.whatsnext.viewsupport.list.SwitchStationListItem;
import de.schmidt.util.managers.PreferenceManager;
import de.schmidt.whatsnext.R;

import java.util.List;

public class StationSelectionListViewAdapter extends BaseAdapter {
	private final Activity context;
	private final List<SwitchStationListItem> stations;

	public StationSelectionListViewAdapter(Activity context, List<SwitchStationListItem> stations) {
		this.context = context;
		this.stations = stations;
	}

	@Override
	public int getCount() {
		return stations.size();
	}

	@Override
	public Object getItem(int position) {
		return stations.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@SuppressLint({"ViewHolder", "SetTextI18n"})
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		convertView = LayoutInflater.from(context).inflate(R.layout.station_list_item, null);

		ImageView icon = convertView.findViewById(R.id.station_icon);
		TextView name = convertView.findViewById(R.id.station_name);

		//get the list item at that position and set fields accordingly
		final SwitchStationListItem item = stations.get(position);
		icon.setImageResource(item.getDrawable());
		name.setText(item.getTitle(context));

		//if selected, show as bold in list
		if (PreferenceManager.getInstance().isCurrentlySelected(context, item)) {
			name.setTypeface(name.getTypeface(), Typeface.BOLD);
		}

		//set the color to interchanging grey and light grey
		convertView.setBackgroundColor(
				(position % 2 == 0) ? context.getColor(R.color.listItemPrimary) : context.getColor(R.color.listItemSecondary)
		);

		return convertView;
	}
}
