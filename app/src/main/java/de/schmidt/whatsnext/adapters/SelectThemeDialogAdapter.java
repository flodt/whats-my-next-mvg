package de.schmidt.whatsnext.adapters;

import static de.schmidt.util.ThemeUtils.*;

public class SelectThemeDialogAdapter {
	public static int getSelectionByIndex(int index) {
		switch (index) {
			case 0: return THEME_FOLLOW_SYSTEM;
			case 1: return THEME_LIGHT;
			case 2: return THEME_DARK;
			default: throw new IllegalArgumentException("Illegal index");
		}
	}

	public static int getAsIndex(int property) {
		switch (property) {
			case THEME_FOLLOW_SYSTEM: return 0;
			case THEME_LIGHT: return 1;
			case THEME_DARK: return 2;
			default: throw new IllegalArgumentException("Illegal property value");
		}
	}
}
