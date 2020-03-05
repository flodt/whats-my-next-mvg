package de.schmidt.util;

import android.app.Activity;
import android.graphics.Color;
import androidx.annotation.ColorInt;
import de.schmidt.whatsnext.R;

public class ColorUtils {
	@ColorInt
	public static int modifyColor(@ColorInt int color, float value) {
		float[] hsv = new float[3];
		Color.colorToHSV(color, hsv);
		hsv[2] *= value;
		return Color.HSVToColor(hsv);
	}

	@SuppressWarnings("deprecation")
	public static int[] getSpriteColors(Activity context) {
		return new int[]{
				context.getResources().getColor(R.color.mvg_1),
				context.getResources().getColor(R.color.mvg_2),
				context.getResources().getColor(R.color.mvg_3),
				context.getResources().getColor(R.color.mvg_4),
				context.getResources().getColor(R.color.mvg_5),
				context.getResources().getColor(R.color.mvg_6),
				context.getResources().getColor(R.color.mvg_7),
				context.getResources().getColor(R.color.mvg_8)
		};
	}
}
