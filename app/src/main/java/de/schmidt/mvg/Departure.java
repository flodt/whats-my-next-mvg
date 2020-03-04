package de.schmidt.mvg;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class Departure {
	private final Station station;
	private final String line;
	private final String direction;
	private final long departureTime;
	private final String lineBackgroundColor;
	private final int delay;
	private final String product;

	public Departure(Station atStation, String line, String direction, long departureTime, String lineBackgroundColor, int delay, String product) {
		this.station = atStation;
		this.line = line;
		this.direction = direction;
		this.departureTime = departureTime;
		this.lineBackgroundColor = lineBackgroundColor;
		this.delay = delay;
		this.product = product;
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
		Duration diff = Duration.between(
				LocalDateTime.now(),
				LocalDateTime.ofInstant(getDepartureTime().toInstant(), ZoneId.systemDefault()).plusMinutes(delay)
		);

		return diff.toMinutes();
	}

	public String getProduct() {
		return product;
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
}
