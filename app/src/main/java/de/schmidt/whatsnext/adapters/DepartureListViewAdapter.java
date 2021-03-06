package de.schmidt.whatsnext.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import de.schmidt.mvg.traffic.Departure;
import de.schmidt.mvg.traffic.LineColor;
import de.schmidt.util.ThemeUtils;
import de.schmidt.whatsnext.R;

import java.util.List;

import static de.schmidt.util.ColorUtils.modifyColor;

public class DepartureListViewAdapter extends BaseAdapter {
	private final Activity context;
	private final List<Departure> departures;

	public DepartureListViewAdapter(Activity context, List<Departure> departures) {
		this.context = context;
		this.departures = departures;
	}

	@Override
	public int getCount() {
		return departures.size();
	}

	@Override
	public Object getItem(int position) {
		return departures.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@SuppressLint({"ViewHolder", "SetTextI18n"})
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		convertView = LayoutInflater.from(context).inflate(R.layout.departure_list_item, null);

		TextView line = convertView.findViewById(R.id.list_item_line);
		TextView minutes = convertView.findViewById(R.id.list_item_in_minutes);
		TextView minutesLabel = convertView.findViewById(R.id.list_item_minutes_label);
		TextView destination = convertView.findViewById(R.id.list_item_destination);

		Departure departure = departures.get(position);
		LineColor color = LineColor.ofAPIValue(departure.getLineBackgroundColor(),
											   departure.getLine());

		line.setText(departure.getLine());
		minutes.setText("" + departure.getDeltaInMinutes());
		destination.setText(departure.getDirection());

		if (ThemeUtils.getInstance().isInLightMode(convertView.getContext())) {
			line.setTextColor(Color.parseColor(color.getTextColor()));
			minutes.setTextColor(Color.parseColor(color.getTextColor()));
			minutesLabel.setTextColor(Color.parseColor(color.getTextColor()));
			destination.setTextColor(Color.parseColor(color.getTextColor()));
			convertView.setBackgroundColor(modifyColor(Color.parseColor(color.getPrimary()), 1.20f));
		} else {
			line.setTextColor(Color.parseColor(color.getSecondary()));
			minutes.setTextColor(Color.parseColor(color.getSecondary()));
			minutesLabel.setTextColor(Color.parseColor(color.getSecondary()));
			destination.setTextColor(Color.parseColor(color.getSecondary()));
			convertView.setBackgroundColor(convertView.getContext().getColor(R.color.background));
		}

		return convertView;
	}
}
