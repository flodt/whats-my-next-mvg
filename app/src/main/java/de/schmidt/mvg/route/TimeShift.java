package de.schmidt.mvg.route;

import android.content.Context;
import androidx.annotation.DrawableRes;
import de.schmidt.whatsnext.R;

public enum TimeShift {
	EARLIER, LATER;

	public String getText(Context context) {
		switch (this) {
			case EARLIER:
				return context.getString(R.string.time_earlier);
			case LATER:
				return context.getString(R.string.time_later);
			default:
				return "";
		}
	}

	public @DrawableRes int getArrow() {
		switch (this) {
			case EARLIER:
				return R.drawable.ic_arrow_up;
			case LATER:
				return R.drawable.ic_arrow_down;
			default:
				return -1;
		}
	}

	public int getOperation() {
		switch (this) {
			case EARLIER:
				return -1;
			case LATER:
				return 1;
			default:
				return 0;
		}
	}
}
