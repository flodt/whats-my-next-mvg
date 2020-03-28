package de.schmidt.whatsnext.viewsupport.alternatives;

import android.view.View;
import de.schmidt.mvg.route.RouteConnection;
import de.schmidt.whatsnext.viewsupport.route.ConnectionDisplayView;

public abstract class AlternativesDisplayView {
	public abstract int getLayoutId();

	/**
	 * This needs to be different than the layoutId, as the ListView uses a list with indices [0..2) internally!
	 * @return view type id int
	 */
	public abstract int getViewType();

	public abstract View inflate(View view, AlternativesDisplayView content, int position, double average);
	public abstract boolean hasRouteConnection();
	public abstract boolean isTimeShiftButton();
}
