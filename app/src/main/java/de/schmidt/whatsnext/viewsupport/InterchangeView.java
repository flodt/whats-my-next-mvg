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
import de.schmidt.util.ColorUtils;
import de.schmidt.whatsnext.R;

import java.text.SimpleDateFormat;
import java.util.Date;

public class InterchangeView extends ConnectionDisplayView {
	private final Station at;
	private final LineColor formerColor;
	private final LineColor nextColor;
	private final Date arrival;
	private final Date departure;
	private final String arrivalPlatform;
	private final String departurePlatform;
	private final String line;
	private final String direction;
	private final int delay;

	public InterchangeView(Station at, LineColor formerColor, LineColor nextColor, Date arrival, Date departure, String arrivalPlatform, String departurePlatform, String line, String direction, int delay) {
		this.at = at;
		this.formerColor = formerColor;
		this.nextColor = nextColor;
		this.arrival = arrival;
		this.departure = departure;
		this.arrivalPlatform = arrivalPlatform;
		this.departurePlatform = departurePlatform;
		this.line = line;
		this.direction = direction;
		this.delay = delay;
	}

	public Station getAt() {
		return at;
	}

	public LineColor getFormerColor() {
		return formerColor;
	}

	public LineColor getNextColor() {
		return nextColor;
	}

	public Date getArrival() {
		return arrival;
	}

	public Date getDeparture() {
		return departure;
	}

	public String getArrivalPlatform() {
		return arrivalPlatform;
	}

	public String getDeparturePlatform() {
		return departurePlatform;
	}

	public String getLine() {
		return line;
	}

	public String getDirection() {
		return direction;
	}

	public int getDelay() {
		return delay;
	}

	@Override
	public int getLayoutId() {
		return R.layout.connection_list_item_interchange;
	}

	@Override
	public String toString() {
		return "InterchangeView{" +
				"at=" + at +
				", formerColor=" + formerColor +
				", nextColor=" + nextColor +
				", arrival=" + arrival +
				", departure=" + departure +
				", arrivalPlatform='" + arrivalPlatform + '\'' +
				", departurePlatform='" + departurePlatform + '\'' +
				'}';
	}

	@SuppressLint("SetTextI18n")
	@Override
	public View inflate(View view, ConnectionDisplayView content) {
		InterchangeView inter = (InterchangeView) content;

		View bar0 = view.findViewById(R.id.interchg_line_bar_0);
		View bar1 = view.findViewById(R.id.interchg_line_bar_1);
		View bar2 = view.findViewById(R.id.interchg_line_bar_2);
		View bar3 = view.findViewById(R.id.interchg_line_bar_3);

		TextView atLabel = view.findViewById(R.id.interchg_line_at_label);
		TextView info = view.findViewById(R.id.interchg_line_info);
		TextView destination = view.findViewById(R.id.interchg_line_destination);

		bar0.setBackground(new ColorDrawable(Color.parseColor(inter.getFormerColor().getPrimary())));
		bar1.setBackground(new ColorDrawable(Color.parseColor(inter.getFormerColor().getPrimary())));
		bar2.setBackground(new ColorDrawable(Color.parseColor(inter.getNextColor().getPrimary())));
		bar3.setBackground(new ColorDrawable(Color.parseColor(inter.getNextColor().getPrimary())));

		atLabel.setText(inter.getAt().getName());

		@SuppressLint("SimpleDateFormat") SimpleDateFormat df = new SimpleDateFormat("HH:mm");
		String infoText = view.getResources().getString(R.string.inter_arrival_elem) + df.format(inter.getArrival())
				+ view.getResources().getString(R.string.inter_departure_elem);
		if (inter.getDeparturePlatform().length() != 0) {
			infoText += inter.getDeparturePlatform() + ", " + df.format(inter.getDeparture());
		} else {
			infoText += df.format(inter.getDeparture());
		}
		if (inter.getDelay() != 0) {
			infoText += " " + ColorUtils.getHtmlColored("(+" + inter.getDelay() + ")", "#FF0000");
		}
		info.setText(Html.fromHtml(infoText));

		destination.setText(
				Html.fromHtml(LineColor.getHtmlColored(inter.getLine()) + ": " + inter.getDirection())
		);

		Spanned destFromHtml;
		if (inter.getDirection().equals("")) {
			destFromHtml = Html.fromHtml(LineColor.getHtmlColored(inter.getLine()));
		} else {
			destFromHtml = Html.fromHtml(LineColor.getHtmlColored(inter.getLine()) + " ▸ " + inter.getDirection());
		}
		destination.setText(destFromHtml);

		return view;
	}

	@Override
	public int getViewType() {
		return 2;
	}

	@Override
	public boolean hasStationForMap() {
		return true;
	}

	@Override
	public Station getStationForMap() {
		return at;
	}
}
