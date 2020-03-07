package de.schmidt.whatsnext.viewsupport;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.TextView;
import de.schmidt.mvg.route.RouteIntermediateStop;
import de.schmidt.mvg.traffic.LineColor;
import de.schmidt.whatsnext.R;

import java.util.List;

public class RunningView extends ConnectionDisplayView {
	private final LineColor color;
	private final List<RouteIntermediateStop> stops;

	public RunningView(LineColor color, List<RouteIntermediateStop> stops) {
		this.color = color;
		this.stops = stops;
	}

	public LineColor getColor() {
		return color;
	}

	public List<RouteIntermediateStop> getStops() {
		return stops;
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

		bar.setBackground(new ColorDrawable(Color.parseColor(running.getColor().getPrimary())));

		String stopsText = running.getStops().isEmpty() ? "" : "(" + running.getStops().size() + " stops)";
		info.setText(stopsText);

		return view;
	}

	@Override
	public boolean isRunning() {
		return true;
	}
}
