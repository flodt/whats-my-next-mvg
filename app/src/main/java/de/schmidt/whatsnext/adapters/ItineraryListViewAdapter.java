package de.schmidt.whatsnext.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import de.schmidt.whatsnext.viewsupport.route.ConnectionDisplayView;

import java.util.List;

public class ItineraryListViewAdapter extends BaseAdapter {
	private final Activity context;
	private final List<ConnectionDisplayView> views;

	public ItineraryListViewAdapter(Activity context, List<ConnectionDisplayView> views) {
		this.context = context;
		this.views = views;
	}

	@Override
	public int getCount() {
		return views.size();
	}

	@Override
	public Object getItem(int position) {
		return views.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public int getItemViewType(int position) {
		return views.get(position).getViewType();
	}

	@Override
	public int getViewTypeCount() {
		return 6;
	}

	@SuppressLint({"ViewHolder", "SetTextI18n"})
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ConnectionDisplayView support = views.get(position);
		convertView = LayoutInflater.from(context).inflate(support.getLayoutId(), null);

		//now inflate the view dynamically with the info passed through the support wrapper objects
		return support.inflate(convertView, support);
	}
}
