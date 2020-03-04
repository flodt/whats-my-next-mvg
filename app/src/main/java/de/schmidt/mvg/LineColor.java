package de.schmidt.mvg;

public class LineColor {
	private final String primary;
	private final String secondary;

	private LineColor(String primary, String secondary) {
		this.primary = primary;
		this.secondary = secondary;
	}

	public String getPrimary() {
		return primary;
	}

	public String getSecondary() {
		return secondary;
	}

	public static LineColor ofAPIValue(String rawData) {
		String[] colors = rawData.split(",");

		return new LineColor(
				colors[0],
				(colors.length == 2) ? colors[1] : colors[0]
		);
	}
}
