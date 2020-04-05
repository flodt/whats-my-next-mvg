package de.schmidt.whatsnext.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import de.schmidt.mvg.route.RouteConnection;
import de.schmidt.whatsnext.viewsupport.alternatives.AlternativesDisplayView;
import de.schmidt.whatsnext.viewsupport.alternatives.AlternativesRouteView;

import java.util.List;
import java.util.Objects;

public class AlternativesListViewAdapter extends BaseAdapter {
	private final Activity context;
	private final List<AlternativesDisplayView> connections;

	public AlternativesListViewAdapter(Activity context, List<AlternativesDisplayView> connections) {
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
	public int getItemViewType(int position) {
		return connections.get(position).getViewType();
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@SuppressLint({"ViewHolder", "SetTextI18n"})
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final AlternativesDisplayView element = connections.get(position);
		convertView = LayoutInflater.from(context).inflate(element.getLayoutId(), null);

		//color the duration orange when it is higher than 1.5x the average duration of all connections
		double average = connections
				.stream()
				.filter(AlternativesDisplayView::hasRouteConnection)
				.map(adv -> (AlternativesRouteView) adv)
				.map(AlternativesRouteView::getRouteConnection)
				.filter(Objects::nonNull)
				.mapToLong(RouteConnection::getDurationInMinutes)
				.average()
				.orElse(Double.POSITIVE_INFINITY);

		//dynamically inflate based on element type
		return element.inflate(convertView, element, position, average);
	}
}
