package de.schmidt.mvg.traffic;

import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class Departure {
	private final Station station;
	private final String line;
	private final String direction;
	private final long departureTime;
	private final String lineBackgroundColor;
	private final int delay;
	private final String product;
	private final String departureId;
	private final String platform;

	public Departure(Station atStation, String line, String direction, long departureTime, String lineBackgroundColor, int delay, String product, String departureId, String platform) {
		this.station = atStation;
		this.line = line;
		this.direction = direction;
		this.departureTime = departureTime;
		this.lineBackgroundColor = lineBackgroundColor;
		this.delay = delay;
		this.product = product;
		this.departureId = departureId;
		this.platform = platform;
	}

	public Station getStation() {
		return station;
	}

	public String getLine() {
		return line;
	}

	public String getDirection() {
		return direction;
	}

	public long getDepartureInUnixTime() {
		return departureTime;
	}

	public String getLineBackgroundColor() {
		return lineBackgroundColor;
	}

	public int getDelay() {
		return delay;
	}

	public Date getDepartureTime() {
		return new Date(departureTime);
	}

	public long getDeltaInMinutes() {
		long duration = getDepartureTime().getTime() - System.currentTimeMillis();
		long diff = TimeUnit.MILLISECONDS.toMinutes(duration);
		return Math.max(diff, 0);
	}

	public String getProduct() {
		return product;
	}

	public String getDepartureId() {
		return departureId;
	}

	public String getPlatform() {
		return platform;
	}

	public String toHumanReadable() {
		return line + " -> " + direction + ": " + getDeltaInMinutes() + " mins (" + delay + "min delay)";
	}

	@Override
	public String toString() {
		return "Departure{" +
				"line='" + line + '\'' +
				", direction='" + direction + '\'' +
				", departureTime=" + departureTime +
				", lineBackgroundColor='" + lineBackgroundColor + '\'' +
				", delay=" + delay +
				", product='" + product + '\'' +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Departure departure = (Departure) o;
		return departureTime == departure.departureTime &&
				delay == departure.delay &&
				Objects.equals(station, departure.station) &&
				Objects.equals(line, departure.line) &&
				Objects.equals(direction, departure.direction) &&
				Objects.equals(lineBackgroundColor, departure.lineBackgroundColor) &&
				Objects.equals(product, departure.product) &&
				Objects.equals(departureId, departure.departureId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(station, line, direction, departureTime, lineBackgroundColor, delay, product, departureId);
	}
}
