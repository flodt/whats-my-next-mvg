package de.schmidt.mvg;

public class LineColor {
	private final String primary;
	private final String secondary;
	private final String textColor;

	private LineColor(String primary, String secondary, String textColor) {
		this.primary = primary;
		this.secondary = secondary;
		this.textColor = textColor;
	}

	public String getPrimary() {
		return primary;
	}

	public String getSecondary() {
		return secondary;
	}

	public String getTextColor() {
		return textColor;
	}

	public static LineColor ofAPIValue(String rawData, String line) {
		//U7 and U8 have 2 colors, the API returns them comma separated
		String[] colors = rawData.split(",");

		String primary = colors[0];
		String secondary = (line.equals("S8")) ? "#F0AA00" : ((colors.length == 2) ? colors[1] : colors[0]);
		String text = (line.equals("S8")) ? "#F0AA00" : "#FFFFFF";

		return new LineColor(primary, secondary, text);
	}
}
