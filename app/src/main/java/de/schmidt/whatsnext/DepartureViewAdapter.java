package de.schmidt.whatsnext;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import de.schmidt.mvg.Departure;
import de.schmidt.mvg.LineColor;

import java.util.List;

import static de.schmidt.util.Utils.modifyColor;

public class DepartureViewAdapter extends BaseAdapter {
	private final Activity context;
	private final List<Departure> departures;

	public DepartureViewAdapter(Activity context, List<Departure> departures) {
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
		TextView destination = convertView.findViewById(R.id.list_item_destination);

		Departure departure = departures.get(position);
		LineColor color = LineColor.ofAPIValue(departure.getLineBackgroundColor());

		line.setText(departure.getLine());
		minutes.setText("" + departure.getDeltaInMinutes());
		destination.setText(departure.getDirection());

		convertView.setBackgroundColor(modifyColor(Color.parseColor(color.getPrimary()), 1.20f));

		return convertView;
	}
}
