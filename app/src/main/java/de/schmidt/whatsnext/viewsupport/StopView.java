package de.schmidt.whatsnext.viewsupport;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.TextView;
import de.schmidt.mvg.route.RouteIntermediateStop;
import de.schmidt.mvg.traffic.LineColor;
import de.schmidt.whatsnext.R;

import java.text.SimpleDateFormat;
import java.util.List;

public class StopView extends ConnectionDisplayView {
	private final LineColor color;
	private final RouteIntermediateStop stop;

	public StopView(LineColor color, RouteIntermediateStop stop) {
		this.color = color;
		this.stop = stop;
	}

	public LineColor getColor() {
		return color;
	}

	public RouteIntermediateStop getStop() {
		return stop;
	}

	@Override
	public int getLayoutId() {
		return R.layout.connection_list_item_stop;
	}

	@Override
	public String toString() {
		return "StopView{" +
				"color=" + color +
				", stop=" + stop +
				'}';
	}

	@SuppressLint("SetTextI18n")
	@Override
	public View inflate(View view, ConnectionDisplayView content) {
		StopView stop = (StopView) content;

		View bar = view.findViewById(R.id.stop_bar);
		View box = view.findViewById(R.id.stop_square);
		TextView info = view.findViewById(R.id.stop_line_info);

		bar.setBackground(new ColorDrawable(Color.parseColor(stop.getColor().getPrimary())));
		box.setBackground(new ColorDrawable(Color.parseColor(stop.getColor().getPrimary())));

		@SuppressLint("SimpleDateFormat")
		String infoText = stop.getStop().getStation().getName()
				+ " (" + new SimpleDateFormat("HH:mm").format(stop.getStop().getTime()) + ")";
		info.setText(infoText);

		return view;
	}

	@Override
	public int getViewType() {
		return 4;
	}
}
