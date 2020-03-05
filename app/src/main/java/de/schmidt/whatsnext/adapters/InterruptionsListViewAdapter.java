package de.schmidt.whatsnext.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import de.schmidt.mvg.Departure;
import de.schmidt.mvg.Interruption;
import de.schmidt.mvg.LineColor;
import de.schmidt.whatsnext.R;

import java.util.List;

import static de.schmidt.util.ColorUtils.modifyColor;

public class InterruptionsListViewAdapter extends BaseAdapter {
	private final Activity context;
	private final List<Interruption> interruptions;

	public InterruptionsListViewAdapter(Activity context, List<Interruption> interruptions) {
		this.context = context;
		this.interruptions = interruptions;
	}

	@Override
	public int getCount() {
		return interruptions.size();
	}

	@Override
	public Object getItem(int position) {
		return interruptions.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@SuppressLint({"ViewHolder", "SetTextI18n"})
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		convertView = LayoutInflater.from(context).inflate(R.layout.interruption_list_item, null);

		TextView title = convertView.findViewById(R.id.interruption_item_title);
		TextView text = convertView.findViewById(R.id.interruption_item_text);
		TextView duration = convertView.findViewById(R.id.interruption_item_duration);
		TextView updated = convertView.findViewById(R.id.interruption_item_update);

		Interruption interruption = interruptions.get(position);
		title.setText(interruption.getTitle());
		text.setText(interruption.getDescriptionText());
		duration.setText(interruption.getDurationAsText());
		updated.setText(interruption.getModificationDateAsString());

		//set the color to interchanging grey and light grey
		convertView.setBackgroundColor(
				(position % 2 == 0) ? context.getColor(R.color.light_gray) : context.getColor(R.color.lighter_gray)
		);

		return convertView;
	}
}
