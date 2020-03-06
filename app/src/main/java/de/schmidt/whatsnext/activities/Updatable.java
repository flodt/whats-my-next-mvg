package de.schmidt.whatsnext.activities;

import java.util.List;

public interface Updatable<T> {
	void handleUIUpdate(List<T> dataSet);
}
