package de.schmidt.whatsnext.base;

import java.util.List;

public interface Updatable<T> {
	void handleUIUpdate(List<T> dataSet);
}
