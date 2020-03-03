package de.schmidt.util;

import android.graphics.Color;
import androidx.annotation.ColorInt;

public class Utils {
	@ColorInt
	public static int modifyColor(@ColorInt int color, float value) {
		float[] hsv = new float[3];
		Color.colorToHSV(color, hsv);
		hsv[2] *= value;
		return Color.HSVToColor(hsv);
	}
}
