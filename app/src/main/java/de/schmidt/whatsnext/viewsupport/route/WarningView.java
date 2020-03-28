package de.schmidt.whatsnext.viewsupport.route;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.TextView;
import de.schmidt.mvg.traffic.Station;
import de.schmidt.whatsnext.R;

public class WarningView extends ConnectionDisplayView {
	private final String message;

	public WarningView(String message) {
		this.message = message;
	}

	@Override
	public int getLayoutId() {
		return R.layout.connection_list_item_warning;
	}

	@Override
	public String toString() {
		return "WarningView{" +
				"message='" + message + '\'' +
				'}';
	}

	public String getMessage() {
		return message;
	}

	@SuppressLint("SetTextI18n")
	@Override
	public View inflate(View view, ConnectionDisplayView content) {
		WarningView warning = (WarningView) content;

		TextView infoText = view.findViewById(R.id.warning_info);
		infoText.setText(warning.getMessage());

		return view;
	}

	@Override
	public int getViewType() {
		return 5;
	}

	@Override
	public boolean hasStationForMap() {
		return false;
	}

	@Override
	public Station getStationForMap() {
		return null;
	}
}
