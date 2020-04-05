package de.schmidt.whatsnext.adapters;

import android.text.Editable;
import android.text.TextWatcher;

@FunctionalInterface
public interface OnTextChangedWatcher extends TextWatcher {
	@Override
	void onTextChanged(CharSequence s, int start, int before, int count);

	@Override
	default void beforeTextChanged(CharSequence s, int start, int count, int after) {

	}

	@Override
	default void afterTextChanged(Editable s) {

	}
}
