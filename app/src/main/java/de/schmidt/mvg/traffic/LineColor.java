package de.schmidt.mvg.traffic;

import java.io.Serializable;

public class LineColor implements Serializable {
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

	public static LineColor getForLine(String line) {
		//returns the LineColor object for a specific line (U-Bahn and S-Bahn values supported), default grey
		switch (line) {
			case "U1": return ofAPIValue("#468447", line);
			case "U2": return ofAPIValue("#dd3d4d", line);
			case "U3": return ofAPIValue("#ef8824", line);
			case "U4": return ofAPIValue("#04af90", line);
			case "U5": return ofAPIValue("#b78730", line);
			case "U6": return ofAPIValue("#0472b3", line);
			case "U7": return ofAPIValue("#468447,#dd3d4d", line);
			case "U8": return ofAPIValue("#f39200,#be1622", line);
			case "S1": return ofAPIValue("#79c6e7", line);
			case "S2": return ofAPIValue("#9bc04c", line);
			case "S3": return ofAPIValue("#942d8d", line);
			case "S4": return ofAPIValue("#d4214d", line);
			case "S6": return ofAPIValue("#03a074", line);
			case "S7": return ofAPIValue("#964438", line);
			case "S8": return ofAPIValue("#000000", line);
			default: return ofAPIValue("#9E9E9E", line);
		}
	}

	public static String getHtmlColored(String line) {
		return "<font color=" + getForLine(line).getSecondary() + ">" + line + "</font>";
	}
}
