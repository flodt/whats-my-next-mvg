package de.schmidt.whatsnext.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import de.schmidt.mvg.route.RouteConnection;
import de.schmidt.mvg.route.RouteConnectionPart;
import de.schmidt.whatsnext.R;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AlternativesListViewAdapter extends BaseAdapter {
	private final Activity context;
	private final List<RouteConnection> connections;

	public AlternativesListViewAdapter(Activity context, List<RouteConnection> connections) {
		this.context = context;
		this.connections = connections;
	}

	@Override
	public int getCount() {
		return connections.size();
	}

	@Override
	public Object getItem(int position) {
		return connections.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@SuppressLint({"ViewHolder", "SetTextI18n"})
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		convertView = LayoutInflater.from(context).inflate(R.layout.alternative_list_item, null);

		TextView means = convertView.findViewById(R.id.alternative_transport_means);
		TextView delta = convertView.findViewById(R.id.alternative_delta);
		TextView duration = convertView.findViewById(R.id.alternative_duration);
		TextView timeRange = convertView.findViewById(R.id.alternative_time_range);

		RouteConnection connection = connections.get(position);

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

			final int maxNumberOfCharsForTextView = 22;
			if (sum >= maxNumberOfCharsForTextView) {
				showAll = false;
				break;
			}
		}

		//i is now the number of lines we can show on screen
		//get that many items, concatenate … when we don't get to show the entirety
		String lines = IntStream.range(0, i)
				.mapToObj(connection.getConnectionParts()::get)
				.map(cp -> "<font color=" + cp.getColor().getPrimary() + ">" + cp.getLine() + "</font>")
				.collect(Collectors.joining(", ")) +
				(showAll ? "" : ", …");
		means.setText((lines.length() == 0) ? "Walking" : Html.fromHtml(lines));

		delta.setText("in " + connection.getDeltaToDepartureInMinutes() + " min.");

		duration.setText("(" + connection.getDurationInMinutes() + " min.)");

		@SuppressLint("SimpleDateFormat") SimpleDateFormat df = new SimpleDateFormat("HH:mm");
		timeRange.setText(
				df.format(connection.getDepartureTime()) + " - " + df.format(connection.getArrivalTime())
		);

		//set the color to interchanging grey and light grey
		convertView.setBackgroundColor(
				(position % 2 == 0) ? context.getColor(R.color.light_gray) : context.getColor(R.color.lighter_gray)
		);

		return convertView;
	}
}
