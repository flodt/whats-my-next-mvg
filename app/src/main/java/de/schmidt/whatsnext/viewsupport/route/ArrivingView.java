package de.schmidt.whatsnext.viewsupport.route;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.TextView;
import de.schmidt.mvg.traffic.LineColor;
import de.schmidt.mvg.traffic.Station;
import de.schmidt.whatsnext.R;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ArrivingView extends ConnectionDisplayView {
	private final Station to;
	private final Date arrival;
	private final String arrivalPlatform;
	private final LineColor color;

	public ArrivingView(Station to, Date arrival, String arrivalPlatform, LineColor color) {
		this.to = to;
		this.arrival = arrival;
		this.arrivalPlatform = arrivalPlatform;
		this.color = color;
	}

	public Station getTo() {
		return to;
	}

	public Date getArrival() {
		return arrival;
	}

	public String getArrivalPlatform() {
		return arrivalPlatform;
	}

	public LineColor getColor() {
		return color;
	}

	@Override
	public int getLayoutId() {
		return R.layout.connection_list_item_arrival;
	}

	@Override
	public String toString() {
		return "ArrivingView{" +
				"to=" + to +
				", arrival=" + arrival +
				", arrivalPlatform='" + arrivalPlatform + '\'' +
				", color=" + color +
				'}';
	}

	@SuppressLint("SetTextI18n")
	@Override
	public View inflate(View view, ConnectionDisplayView content) {
		ArrivingView arrival = (ArrivingView) content;
		View bar1 = view.findViewById(R.id.arrival_line_bar_1);
		View bar2 = view.findViewById(R.id.arrival_line_bar_2);
		TextView toLabel = view.findViewById(R.id.arrival_line_to_label);
		TextView info = view.findViewById(R.id.arrival_line_info);

		bar1.setBackground(new ColorDrawable(Color.parseColor(arrival.getColor().getSecondary())));
		bar2.setBackground(new ColorDrawable(Color.parseColor(arrival.getColor().getSecondary())));
		toLabel.setText(arrival.getTo().getName());

		@SuppressLint("SimpleDateFormat") SimpleDateFormat df = new SimpleDateFormat("HH:mm");
		String infoText;
		if (arrival.getArrivalPlatform().length() != 0) {
			infoText = arrival.getArrivalPlatform() + ", " + df.format(arrival.getArrival());
		} else {
			infoText = df.format(arrival.getArrival());
		}
		info.setText(infoText);

		return view;
	}

	@Override
	public int getViewType() {
		return 3;
	}

	@Override
	public boolean hasStationForMap() {
		return true;
	}

	@Override
	public Station getStationForMap() {
		return to;
	}
}
