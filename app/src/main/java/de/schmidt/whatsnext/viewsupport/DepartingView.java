package de.schmidt.whatsnext.viewsupport;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.widget.TextView;
import de.schmidt.mvg.traffic.LineColor;
import de.schmidt.mvg.traffic.Station;
import de.schmidt.whatsnext.R;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DepartingView extends ConnectionDisplayView {
	private final Station from;
	private final Date departure;
	private final String departurePlatform;
	private final int delay;
	private final String direction;
	private final LineColor color;
	private final String line;

	public DepartingView(Station from, Date departure, String departurePlatform, int delay, String direction, LineColor color, String line) {
		this.from = from;
		this.departure = departure;
		this.departurePlatform = departurePlatform;
		this.delay = delay;
		this.direction = direction;
		this.color = color;
		this.line = line;
	}

	public Station getFrom() {
		return from;
	}

	public Date getDeparture() {
		return departure;
	}

	public String getDeparturePlatform() {
		return departurePlatform;
	}

	public int getDelay() {
		return delay;
	}

	public String getDirection() {
		return direction;
	}

	public LineColor getColor() {
		return color;
	}

	public String getLine() {
		return line;
	}

	@Override
	public int getLayoutId() {
		return R.layout.connection_list_item_departure;
	}

	@Override
	public String toString() {
		return "DepartingView{" +
				"from=" + from +
				", departure=" + departure +
				", departurePlatform='" + departurePlatform + '\'' +
				", delay=" + delay +
				", direction='" + direction + '\'' +
				", color=" + color +
				'}';
	}

	@SuppressLint("SetTextI18n")
	@Override
	public View inflate(View view, ConnectionDisplayView content) {
		DepartingView departure = (DepartingView) content;
		View bar1 = view.findViewById(R.id.departure_line_bar_1);
		View bar2 = view.findViewById(R.id.departure_line_bar_2);
		TextView fromLabel = view.findViewById(R.id.departure_line_from_label);
		TextView info = view.findViewById(R.id.departure_line_info);
		TextView destination = view.findViewById(R.id.departure_line_destination);

		bar1.setBackground(new ColorDrawable(Color.parseColor(departure.getColor().getPrimary())));
		bar2.setBackground(new ColorDrawable(Color.parseColor(departure.getColor().getPrimary())));
		fromLabel.setText(departure.getFrom().getName());

		@SuppressLint("SimpleDateFormat") SimpleDateFormat df = new SimpleDateFormat("HH:mm");
		String infoText;
		if (departure.getDeparturePlatform().length() != 0) {
			infoText = String.join(", ",
								   departure.getDeparturePlatform(),
								   df.format(departure.getDeparture()));
		} else {
			infoText = df.format(departure.getDeparture());
		}
		info.setText(infoText);

		Spanned destFromHtml;
		if (departure.getDirection().equals("")) {
			destFromHtml = Html.fromHtml(LineColor.getHtmlColored(departure.getLine()));
		} else {
			destFromHtml = Html.fromHtml(String.join(" ▸ ", LineColor.getHtmlColored(departure.getLine()), departure.getDirection()));
		}
		destination.setText(destFromHtml);


		return view;
	}

	@Override
	public int getViewType() {
		return 0;
	}
}
