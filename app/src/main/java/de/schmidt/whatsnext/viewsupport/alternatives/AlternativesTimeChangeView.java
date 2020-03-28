package de.schmidt.whatsnext.viewsupport.alternatives;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import de.schmidt.mvg.route.TimeShift;
import de.schmidt.whatsnext.R;

public class AlternativesTimeChangeView extends AlternativesDisplayView {
	private final TimeShift temporalDirection;

	public AlternativesTimeChangeView(TimeShift temporalDirection) {
		this.temporalDirection = temporalDirection;
	}

	public TimeShift getTemporalDirection() {
		return temporalDirection;
	}

	@Override
	public int getLayoutId() {
		return R.layout.alternative_time_shift_item;
	}

	@Override
	public int getViewType() {
		return 1;
	}

	@Override
	public boolean isTimeShiftButton() {
		return true;
	}

	@Override
	public boolean hasRouteConnection() {
		return false;
	}

	@Override
	public View inflate(View view, AlternativesDisplayView content, int position, double average) {
		AlternativesTimeChangeView timeChange = (AlternativesTimeChangeView) content;

		ImageView arrowLeft = view.findViewById(R.id.arrow_icon_1);
		ImageView arrowRight = view.findViewById(R.id.arrow_icon_2);
		TextView timeDescription = view.findViewById(R.id.time_text);

		timeDescription.setText(timeChange.getTemporalDirection().getText(view.getContext()));
		arrowLeft.setImageResource(timeChange.getTemporalDirection().getArrow());
		arrowRight.setImageResource(timeChange.getTemporalDirection().getArrow());

		return view;
	}
}
