package de.schmidt.whatsnext.viewsupport.route;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.TextView;
import de.schmidt.mvg.route.RouteIntermediateStop;
import de.schmidt.mvg.traffic.LineColor;
import de.schmidt.mvg.traffic.Station;
import de.schmidt.whatsnext.R;

import java.util.List;

public class RunningView extends ConnectionDisplayView {
	private final LineColor color;
	private final List<RouteIntermediateStop> stops;
	private final long durationInMinutes;

	public RunningView(LineColor color, List<RouteIntermediateStop> stops, long durationInMinutes) {
		this.color = color;
		this.stops = stops;
		this.durationInMinutes = durationInMinutes;
	}

	public LineColor getColor() {
		return color;
	}

	public List<RouteIntermediateStop> getStops() {
		return stops;
	}

	public long getDurationInMinutes() {
		return durationInMinutes;
	}

	@Override
	public int getLayoutId() {
		return R.layout.connection_list_item_running;
	}

	@Override
	public String toString() {
		return "RunningView{" +
				"color=" + color +
				", stops=" + stops +
				'}';
	}

	@SuppressLint("SetTextI18n")
	@Override
	public View inflate(View view, ConnectionDisplayView content) {
		RunningView running = (RunningView) content;

		View bar = view.findViewById(R.id.running_bar);
		TextView info = view.findViewById(R.id.running_line_info);

		bar.setBackground(new ColorDrawable(Color.parseColor(running.getColor().getSecondary())));

		String infoText = "(" +
				(running.getStops().isEmpty() ? "" : running.getStops().size() + view.getResources().getString(R.string.running_stops_infix)) +
				running.getDurationInMinutes() + " min." +
				")";
		info.setText(infoText);

		return view;
	}

	@Override
	public int getViewType() {
		return 1;
	}

	@Override
	public boolean hasStationForMap() {
		return false;
	}

	@Override
	public Station getStationForMap() {
		return null;
	}
}
