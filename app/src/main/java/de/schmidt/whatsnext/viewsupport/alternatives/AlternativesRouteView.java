package de.schmidt.whatsnext.viewsupport.alternatives;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Html;
import android.view.View;
import android.widget.TextView;
import de.schmidt.mvg.route.RouteConnection;
import de.schmidt.mvg.route.RouteConnectionPart;
import de.schmidt.util.ColorUtils;
import de.schmidt.util.ThemeUtils;
import de.schmidt.whatsnext.R;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AlternativesRouteView extends AlternativesDisplayView {
	private final RouteConnection connection;

	public AlternativesRouteView(RouteConnection connection) {
		this.connection = connection;
	}

	public RouteConnection getRouteConnection() {
		return connection;
	}

	@Override
	public int getLayoutId() {
		return R.layout.alternative_list_item;
	}

	@Override
	public int getViewType() {
		return 0;
	}

	@Override
	public boolean isTimeShiftButton() {
		return false;
	}

	@Override
	public boolean hasRouteConnection() {
		return true;
	}

	@SuppressLint("SetTextI18n")
	@Override
	public View inflate(View convertView, AlternativesDisplayView content, int position, double average) {
		TextView means = convertView.findViewById(R.id.alternative_transport_means);
		TextView delta = convertView.findViewById(R.id.alternative_delta);
		TextView duration = convertView.findViewById(R.id.alternative_duration);
		TextView timeRange = convertView.findViewById(R.id.alternative_time_range);

		RouteConnection connection = ((AlternativesRouteView) content).getRouteConnection();

		//determine the amount of lines we can fit on the screen (max 20 chars)
		List<Integer> lengths = connection.getConnectionParts()
				.stream()
				.map(RouteConnectionPart::getLine)
				.map(String::length)
				.collect(Collectors.toList());

		int i, sum = 0;
		boolean showAll = true;
		for (i = 0; i < lengths.size(); i++) {
			sum += lengths.get(i);

			final int maxNumberOfCharsForTextView = 17;
			if (sum >= maxNumberOfCharsForTextView) {
				showAll = false;
				break;
			}
		}

		//i is now the number of lines we can show on screen
		//get that many items, concatenate … when we don't get to show the entirety
		String lines = IntStream.range(0, i)
				.mapToObj(connection.getConnectionParts()::get)
				.map(cp -> "<font color=" + cp.getColor().getSecondary() + ">" + cp.getLine() + "</font>")
				.collect(Collectors.joining(", ")) +
				(showAll ? "" : ", …");
		means.setText((lines.length() == 0) ? "Walking" : Html.fromHtml(lines));

		delta.setText("in " + connection.getDeltaToDepartureInMinutes() + " min.");

		//color the duration orange when it is higher than 1.5x the average duration of all connections
		final boolean isInDarkMode = ThemeUtils.getInstance().isInDarkMode(convertView.getContext());
		String highlightColor = isInDarkMode ? "#ef5350" : "#C20831";
		String defaultColor = isInDarkMode ? "#FFFFFF" : "#000000";
		String color = connection.getDurationInMinutes() > 1.50 * average ? highlightColor : defaultColor;
		String text = "(" + connection.getDurationInMinutes() + " min.)";
		duration.setText(Html.fromHtml(ColorUtils.getHtmlColored(text, color)));

		@SuppressLint("SimpleDateFormat") SimpleDateFormat df = new SimpleDateFormat("HH:mm");
		timeRange.setText(
				df.format(connection.getDepartureTime()) + " - " + df.format(connection.getArrivalTime())
		);

		//set the color to interchanging grey and light grey
		Context context = convertView.getContext();
		convertView.setBackgroundColor(
				(position % 2 == 0) ? context.getColor(R.color.listItemPrimary) : context.getColor(R.color.listItemSecondary)
		);

		return convertView;
	}
}
